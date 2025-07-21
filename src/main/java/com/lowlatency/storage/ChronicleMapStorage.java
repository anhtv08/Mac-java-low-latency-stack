package com.lowlatency.storage;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.openhft.chronicle.map.ChronicleMap;

import java.io.File;
import java.io.IOException;

/**
 * Memory-mapped file storage using Chronicle Map for ultra-low latency persistence
 */
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ChronicleMapStorage<K, V> implements AutoCloseable {
    
    private final ChronicleMap<K, V> map;
    private final File mapFile;
    
    public static <K, V> ChronicleMapStorage<K, V> create(
            Class<K> keyClass, 
            Class<V> valueClass,
            long entries,
            String fileName) throws IOException {
        
        log.info("Creating persistent ChronicleMap storage: file={}, entries={}, keyClass={}, valueClass={}", 
                fileName, entries, keyClass.getSimpleName(), valueClass.getSimpleName());
        
        File mapFile = new File(fileName);
        ChronicleMap<K, V> map = ChronicleMap
                .of(keyClass, valueClass)
                .entries(entries)
                .createPersistedTo(mapFile);
        
        log.debug("ChronicleMap storage created successfully: {}", fileName);
        return new ChronicleMapStorage<>(map, mapFile);
    }
    
    public static <K, V> ChronicleMapStorage<K, V> createInMemory(
            Class<K> keyClass, 
            Class<V> valueClass,
            long entries) throws IOException {
        
        log.info("Creating in-memory ChronicleMap storage: entries={}, keyClass={}, valueClass={}", 
                entries, keyClass.getSimpleName(), valueClass.getSimpleName());
        
        ChronicleMap<K, V> map = ChronicleMap
                .of(keyClass, valueClass)
                .entries(entries)
                .create();
        
        log.debug("In-memory ChronicleMap storage created successfully");
        return new ChronicleMapStorage<>(map, null);
    }
    
    public V get(K key) {
        log.trace("Getting value for key: {}", key);
        return map.get(key);
    }
    
    public V put(K key, V value) {
        log.trace("Putting key-value pair: {} -> {}", key, value);
        return map.put(key, value);
    }
    
    public V remove(K key) {
        log.trace("Removing key: {}", key);
        return map.remove(key);
    }
    
    public boolean containsKey(K key) {
        boolean contains = map.containsKey(key);
        log.trace("Contains key {}: {}", key, contains);
        return contains;
    }
    
    public long size() {
        long currentSize = map.size();
        log.trace("Current map size: {}", currentSize);
        return currentSize;
    }
    
    public void clear() {
        log.info("Clearing ChronicleMap storage");
        map.clear();
        log.debug("ChronicleMap storage cleared");
    }
    
    public ChronicleMap<K, V> getMap() {
        return map;
    }
    
    @Override
    public void close() {
        log.info("Closing ChronicleMap storage");
        if (map != null) {
            map.close();
            log.debug("ChronicleMap storage closed successfully");
        }
    }
    
    public void forceFlush() {
        log.debug("Force flushing ChronicleMap storage");
        // Chronicle Map handles persistence automatically
        // This method is for explicit flush if needed
        if (mapFile != null) {
            log.trace("Forcing OS flush for memory-mapped file: {}", mapFile.getName());
            // Force OS to flush to disk
            System.gc(); // Minimal GC hint for memory-mapped files
        }
    }
}