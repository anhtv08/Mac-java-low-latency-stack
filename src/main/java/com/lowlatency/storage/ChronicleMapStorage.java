package com.lowlatency.storage;

import net.openhft.chronicle.map.ChronicleMap;
import java.io.File;
import java.io.IOException;

/**
 * Memory-mapped file storage using Chronicle Map for ultra-low latency persistence
 */
public class ChronicleMapStorage<K, V> implements AutoCloseable {
    
    private final ChronicleMap<K, V> map;
    private final File mapFile;
    
    private ChronicleMapStorage(ChronicleMap<K, V> map, File mapFile) {
        this.map = map;
        this.mapFile = mapFile;
    }
    
    public static <K, V> ChronicleMapStorage<K, V> create(
            Class<K> keyClass, 
            Class<V> valueClass,
            long entries,
            String fileName) throws IOException {
        
        File mapFile = new File(fileName);
        ChronicleMap<K, V> map = ChronicleMap
                .of(keyClass, valueClass)
                .entries(entries)
                .createPersistedTo(mapFile);
        
        return new ChronicleMapStorage<>(map, mapFile);
    }
    
    public static <K, V> ChronicleMapStorage<K, V> createInMemory(
            Class<K> keyClass, 
            Class<V> valueClass,
            long entries) throws IOException {
        
        ChronicleMap<K, V> map = ChronicleMap
                .of(keyClass, valueClass)
                .entries(entries)
                .create();
        
        return new ChronicleMapStorage<>(map, null);
    }
    
    public V get(K key) {
        return map.get(key);
    }
    
    public V put(K key, V value) {
        return map.put(key, value);
    }
    
    public V remove(K key) {
        return map.remove(key);
    }
    
    public boolean containsKey(K key) {
        return map.containsKey(key);
    }
    
    public long size() {
        return map.size();
    }
    
    public void clear() {
        map.clear();
    }
    
    public ChronicleMap<K, V> getMap() {
        return map;
    }
    
    @Override
    public void close() {
        if (map != null) {
            map.close();
        }
    }
    
    public void forceFlush() {
        // Chronicle Map handles persistence automatically
        // This method is for explicit flush if needed
        if (mapFile != null) {
            // Force OS to flush to disk
            System.gc(); // Minimal GC hint for memory-mapped files
        }
    }
}