package com.lowlatency.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

class ChronicleMapStorageTest {
    
    @TempDir
    Path tempDir;
    
    private ChronicleMapStorage<String, String> storage;
    private ChronicleMapStorage<String, String> inMemoryStorage;
    
    @BeforeEach
    void setUp() throws IOException {
        File testFile = tempDir.resolve("test-storage.dat").toFile();
        storage = ChronicleMapStorage.create(
            String.class, 
            String.class, 
            1000, 
            testFile.getAbsolutePath()
        );
        
        inMemoryStorage = ChronicleMapStorage.createInMemory(
            String.class,
            String.class,
            1000
        );
    }
    
    @AfterEach
    void tearDown() {
        if (storage != null) {
            storage.close();
        }
        if (inMemoryStorage != null) {
            inMemoryStorage.close();
        }
    }
    
    @Test
    void testStorageCreation() {
        assertNotNull(storage);
        assertNotNull(inMemoryStorage);
        assertEquals(0, storage.size());
        assertEquals(0, inMemoryStorage.size());
    }
    
    @Test
    void testPutAndGet() {
        String key = "testKey";
        String value = "testValue";
        
        storage.put(key, value);
        String retrieved = storage.get(key);
        
        assertEquals(value, retrieved);
        assertEquals(1, storage.size());
    }
    
    @Test
    void testInMemoryPutAndGet() {
        String key = "memoryKey";
        String value = "memoryValue";
        
        inMemoryStorage.put(key, value);
        String retrieved = inMemoryStorage.get(key);
        
        assertEquals(value, retrieved);
        assertEquals(1, inMemoryStorage.size());
    }
    
    @Test
    void testContainsKey() {
        String key = "existingKey";
        String value = "existingValue";
        
        assertFalse(storage.containsKey(key));
        
        storage.put(key, value);
        assertTrue(storage.containsKey(key));
        assertFalse(storage.containsKey("nonExistentKey"));
    }
    
    @Test
    void testRemove() {
        String key = "removeKey";
        String value = "removeValue";
        
        storage.put(key, value);
        assertEquals(1, storage.size());
        assertTrue(storage.containsKey(key));
        
        String removed = storage.remove(key);
        assertEquals(value, removed);
        assertEquals(0, storage.size());
        assertFalse(storage.containsKey(key));
    }
    
    @Test
    void testClear() {
        storage.put("key1", "value1");
        storage.put("key2", "value2");
        storage.put("key3", "value3");
        
        assertEquals(3, storage.size());
        
        storage.clear();
        assertEquals(0, storage.size());
        assertFalse(storage.containsKey("key1"));
        assertFalse(storage.containsKey("key2"));
        assertFalse(storage.containsKey("key3"));
    }
    
    @Test
    void testMultipleOperations() {
        // Test bulk operations
        for (int i = 0; i < 100; i++) {
            storage.put("key" + i, "value" + i);
        }
        
        assertEquals(100, storage.size());
        
        // Verify all values
        for (int i = 0; i < 100; i++) {
            assertEquals("value" + i, storage.get("key" + i));
        }
        
        // Remove half
        for (int i = 0; i < 50; i++) {
            storage.remove("key" + i);
        }
        
        assertEquals(50, storage.size());
        
        // Verify remaining values
        for (int i = 50; i < 100; i++) {
            assertEquals("value" + i, storage.get("key" + i));
        }
    }
    
    @Test
    void testGetNonExistentKey() {
        String result = storage.get("nonExistentKey");
        assertNull(result);
    }
    
    @Test
    void testRemoveNonExistentKey() {
        String result = storage.remove("nonExistentKey");
        assertNull(result);
    }
    
    @Test
    void testForceFlush() {
        storage.put("flushKey", "flushValue");
        
        // Should not throw exception
        assertDoesNotThrow(() -> storage.forceFlush());
        
        // Data should still be accessible
        assertEquals("flushValue", storage.get("flushKey"));
    }
    
    @Test
    void testGetMap() {
        assertNotNull(storage.getMap());
        assertNotNull(inMemoryStorage.getMap());
    }
    
    @Test
    void testPersistenceRecreation() throws IOException {
        File testFile = tempDir.resolve("persistence-test.dat").toFile();
        
        // Create storage and add data
        try (ChronicleMapStorage<String, String> firstStorage = 
             ChronicleMapStorage.create(String.class, String.class, 1000, testFile.getAbsolutePath())) {
            
            firstStorage.put("persistKey", "persistValue");
            assertEquals("persistValue", firstStorage.get("persistKey"));
        }
        
        // Recreate storage from same file
        try (ChronicleMapStorage<String, String> secondStorage = 
             ChronicleMapStorage.create(String.class, String.class, 1000, testFile.getAbsolutePath())) {
            
            // Data should be persisted
            assertEquals("persistValue", secondStorage.get("persistKey"));
        }
    }
}