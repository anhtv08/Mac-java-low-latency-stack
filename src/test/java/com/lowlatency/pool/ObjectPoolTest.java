package com.lowlatency.pool;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class ObjectPoolTest {
    
    private ObjectPool<StringBuilder> stringBuilderPool;
    private ObjectPool<TestResetableObject> resetablePool;
    
    @BeforeEach
    void setUp() {
        stringBuilderPool = new ObjectPool<>(
            () -> new StringBuilder(100), 
            5, 
            10
        );
        
        resetablePool = new ObjectPool<>(
            TestResetableObject::new,
            3,
            6
        );
    }
    
    @Test
    void testPoolCreation() {
        assertNotNull(stringBuilderPool);
        assertEquals(5, stringBuilderPool.size());
        assertEquals(10, stringBuilderPool.maxSize());
    }
    
    @Test
    void testAcquireAndRelease() {
        StringBuilder sb = stringBuilderPool.acquire();
        assertNotNull(sb);
        assertEquals(4, stringBuilderPool.size()); // One less in pool
        
        stringBuilderPool.release(sb);
        assertEquals(5, stringBuilderPool.size()); // Back to original size
    }
    
    @Test
    void testAcquireWhenPoolEmpty() {
        // Drain the pool
        List<StringBuilder> acquired = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            acquired.add(stringBuilderPool.acquire());
        }
        assertEquals(0, stringBuilderPool.size());
        
        // Acquire when empty - should create new object
        StringBuilder sb = stringBuilderPool.acquire();
        assertNotNull(sb);
        assertEquals(0, stringBuilderPool.size());
        
        // Release all objects back
        acquired.forEach(stringBuilderPool::release);
        stringBuilderPool.release(sb);
        assertEquals(6, stringBuilderPool.size()); // One more than initial
    }
    
    @Test
    void testMaxSizeLimit() {
        // Fill pool to max capacity
        List<StringBuilder> objects = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            objects.add(stringBuilderPool.acquire());
        }
        
        // Release more than max size
        for (StringBuilder sb : objects) {
            stringBuilderPool.release(sb);
        }
        
        // Pool should not exceed max size
        assertTrue(stringBuilderPool.size() <= stringBuilderPool.maxSize());
    }
    
    @Test
    void testResetableObjects() {
        TestResetableObject obj = resetablePool.acquire();
        obj.setValue("test");
        assertEquals("test", obj.getValue());
        
        resetablePool.release(obj);
        
        TestResetableObject sameObj = resetablePool.acquire();
        assertEquals("", sameObj.getValue()); // Should be reset
    }
    
    @Test
    void testNullRelease() {
        int initialSize = stringBuilderPool.size();
        stringBuilderPool.release(null);
        assertEquals(initialSize, stringBuilderPool.size()); // Size unchanged
    }
    
    @Test
    void testConcurrentAccess() throws InterruptedException {
        int threadCount = 10;
        int operationsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        StringBuilder sb = stringBuilderPool.acquire();
                        sb.append("test");
                        stringBuilderPool.release(sb);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        assertTrue(latch.await(10, TimeUnit.SECONDS));
        executor.shutdown();
        
        // Pool should still be functional
        assertTrue(stringBuilderPool.size() >= 0);
        assertTrue(stringBuilderPool.size() <= stringBuilderPool.maxSize());
    }
    
    // Test helper class
    private static class TestResetableObject implements ObjectPool.Resetable {
        private String value = "";
        
        public void setValue(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
        
        @Override
        public void reset() {
            this.value = "";
        }
    }
}