package com.lowlatency;

import com.lowlatency.core.Event;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class LowLatencyEngineTest {
    
    @TempDir
    Path tempDir;
    
    private LowLatencyEngine engine;
    
    @BeforeEach
    void setUp() throws IOException {
        // Change to temp directory to avoid file conflicts
        System.setProperty("user.dir", tempDir.toString());
        engine = new LowLatencyEngine();
    }
    
    @AfterEach
    void tearDown() {
        if (engine != null) {
            engine.close();
        }
    }
    
    @Test
    void testEngineCreation() {
        assertNotNull(engine);
        assertEquals(0, engine.getProcessedEventCount());
        assertTrue(engine.getStringBuilderPoolSize() > 0);
        assertEquals(0, engine.getStorageSize());
    }
    
    @Test
    void testPublishEvent() throws InterruptedException {
        engine.publishEvent("AAPL", 150.0, 100, Event.EventType.TRADE);
        
        // Give some time for event processing
        Thread.sleep(100);
        
        assertTrue(engine.getProcessedEventCount() > 0);
    }
    
    @Test
    void testMultipleEventPublishing() throws InterruptedException {
        int eventCount = 1000;
        
        for (int i = 0; i < eventCount; i++) {
            engine.publishEvent("MSFT", 300.0 + i * 0.01, 50, Event.EventType.QUOTE);
        }
        
        // Wait for processing
        Thread.sleep(500);
        
        assertEquals(eventCount, engine.getProcessedEventCount());
    }
    
    @Test
    void testStorageOperations() {
        String key = "testKey";
        String value = "testValue";
        
        engine.storeData(key, value);
        String retrieved = engine.getData(key);
        
        assertEquals(value, retrieved);
        assertEquals(1, engine.getStorageSize());
    }
    
    @Test
    void testStringBuilderPool() {
        StringBuilder sb1 = engine.acquireStringBuilder();
        assertNotNull(sb1);
        
        sb1.append("test content");
        assertEquals("test content", sb1.toString());
        
        int poolSizeBefore = engine.getStringBuilderPoolSize();
        engine.releaseStringBuilder(sb1);
        int poolSizeAfter = engine.getStringBuilderPoolSize();
        
        // Pool size should increase after release
        assertTrue(poolSizeAfter >= poolSizeBefore);
        
        // StringBuilder should be cleared when acquired again
        StringBuilder sb2 = engine.acquireStringBuilder();
        assertEquals(0, sb2.length());
    }
    
    @Test
    void testConcurrentEventPublishing() throws InterruptedException {
        int threadCount = 5;
        int eventsPerThread = 200;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < eventsPerThread; j++) {
                        engine.publishEvent(
                            "THREAD" + threadId, 
                            100.0 + j, 
                            10 + j, 
                            Event.EventType.values()[j % 3]
                        );
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        assertTrue(latch.await(10, TimeUnit.SECONDS));
        executor.shutdown();
        
        // Wait for all events to be processed
        Thread.sleep(1000);
        
        assertEquals(threadCount * eventsPerThread, engine.getProcessedEventCount());
    }
    
    @Test
    void testConcurrentStorageOperations() throws InterruptedException {
        int threadCount = 3;
        int operationsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        String key = "thread" + threadId + "_key" + j;
                        String value = "thread" + threadId + "_value" + j;
                        engine.storeData(key, value);
                        
                        String retrieved = engine.getData(key);
                        assertEquals(value, retrieved);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        assertTrue(latch.await(10, TimeUnit.SECONDS));
        executor.shutdown();
        
        assertEquals(threadCount * operationsPerThread, engine.getStorageSize());
    }
    
    @Test
    void testMixedOperations() throws InterruptedException {
        // Mix of event publishing, storage operations, and pool usage
        for (int i = 0; i < 50; i++) {
            engine.publishEvent("MIX" + i, 200.0 + i, 25, Event.EventType.ORDER);
            engine.storeData("mixKey" + i, "mixValue" + i);
            
            StringBuilder sb = engine.acquireStringBuilder();
            sb.append("iteration").append(i);
            engine.releaseStringBuilder(sb);
        }
        
        Thread.sleep(500);
        
        assertEquals(50, engine.getProcessedEventCount());
        assertEquals(50, engine.getStorageSize());
        
        // Verify storage data
        for (int i = 0; i < 50; i++) {
            assertEquals("mixValue" + i, engine.getData("mixKey" + i));
        }
    }
    
    @Test
    void testPerformanceBaseline() throws InterruptedException {
        int warmupEvents = 1000;
        int testEvents = 10000;
        
        // Warmup
        for (int i = 0; i < warmupEvents; i++) {
            engine.publishEvent("WARM", 100.0, 10, Event.EventType.TRADE);
        }
        Thread.sleep(200);
        
        // Reset counter (we can't actually reset, so we'll measure delta)
        long startCount = engine.getProcessedEventCount();
        long startTime = System.nanoTime();
        
        // Performance test
        for (int i = 0; i < testEvents; i++) {
            engine.publishEvent("PERF", 150.0 + i * 0.001, 20, Event.EventType.QUOTE);
        }
        
        Thread.sleep(1000); // Wait for processing
        
        long endTime = System.nanoTime();
        long endCount = engine.getProcessedEventCount();
        long processedEvents = endCount - startCount;
        long duration = endTime - startTime;
        
        assertEquals(testEvents, processedEvents);
        
        double throughput = testEvents * 1_000_000_000.0 / duration;
        System.out.printf("Performance test: %.0f events/second%n", throughput);
        
        // Should process at least 100k events per second
        assertTrue(throughput > 100_000, "Throughput too low: " + throughput);
    }
}