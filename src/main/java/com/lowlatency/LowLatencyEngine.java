package com.lowlatency;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;
import com.lowlatency.core.Event;
import com.lowlatency.core.EventFactory;
import com.lowlatency.core.EventHandler;
import com.lowlatency.pool.ObjectPool;
import com.lowlatency.storage.ChronicleMapStorage;

import java.io.IOException;
import java.util.concurrent.ThreadFactory;

/**
 * Main low-latency processing engine combining Disruptor, Chronicle Map, and Object Pooling
 */
public class LowLatencyEngine implements AutoCloseable {
    
    private static final int RING_BUFFER_SIZE = 1024 * 64; // Must be power of 2
    private static final int OBJECT_POOL_SIZE = 1000;
    private static final long STORAGE_ENTRIES = 1_000_000;
    
    private final Disruptor<Event> disruptor;
    private final RingBuffer<Event> ringBuffer;
    private final ObjectPool<StringBuilder> stringBuilderPool;
    private final ChronicleMapStorage<String, String> storage;
    private final EventHandler eventHandler;
    
    public LowLatencyEngine() throws IOException {
        // Initialize object pool for StringBuilder reuse
        this.stringBuilderPool = new ObjectPool<>(
            () -> new StringBuilder(256), 
            OBJECT_POOL_SIZE / 2, 
            OBJECT_POOL_SIZE
        );
        
        // Initialize Chronicle Map storage
        this.storage = ChronicleMapStorage.create(
            String.class, 
            String.class, 
            STORAGE_ENTRIES, 
            "low-latency-data.dat"
        );
        
        // Initialize Disruptor
        ThreadFactory threadFactory = DaemonThreadFactory.INSTANCE;
        this.disruptor = new Disruptor<>(new EventFactory(), RING_BUFFER_SIZE, threadFactory);
        
        // Set up event handler
        this.eventHandler = new EventHandler("MainHandler");
        this.disruptor.handleEventsWith(eventHandler);
        
        // Start the disruptor
        this.disruptor.start();
        this.ringBuffer = disruptor.getRingBuffer();
    }
    
    /**
     * Publish an event to the ring buffer
     */
    public void publishEvent(String symbol, double price, long quantity, Event.EventType type) {
        long sequence = ringBuffer.next();
        try {
            Event event = ringBuffer.get(sequence);
            event.setId(sequence);
            event.setSymbol(symbol);
            event.setPrice(price);
            event.setQuantity(quantity);
            event.setTimestamp(System.nanoTime());
            event.setType(type);
        } finally {
            ringBuffer.publish(sequence);
        }
    }
    
    /**
     * Store data using Chronicle Map
     */
    public void storeData(String key, String value) {
        storage.put(key, value);
    }
    
    /**
     * Retrieve data from Chronicle Map
     */
    public String getData(String key) {
        return storage.get(key);
    }
    
    /**
     * Get a StringBuilder from the object pool
     */
    public StringBuilder acquireStringBuilder() {
        return stringBuilderPool.acquire();
    }
    
    /**
     * Return a StringBuilder to the object pool
     */
    public void releaseStringBuilder(StringBuilder sb) {
        sb.setLength(0); // Clear the buffer
        stringBuilderPool.release(sb);
    }
    
    /**
     * Get processing statistics
     */
    public long getProcessedEventCount() {
        return eventHandler.getProcessedCount();
    }
    
    public int getStringBuilderPoolSize() {
        return stringBuilderPool.size();
    }
    
    public long getStorageSize() {
        return storage.size();
    }
    
    @Override
    public void close() {
        if (disruptor != null) {
            disruptor.halt();
            disruptor.shutdown();
        }
        if (storage != null) {
            storage.close();
        }
    }
    
    /**
     * Example usage and performance test
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        try (LowLatencyEngine engine = new LowLatencyEngine()) {
            
            // Warm up
            System.out.println("Warming up...");
            for (int i = 0; i < 10000; i++) {
                engine.publishEvent("AAPL", 150.0 + i * 0.01, 100, Event.EventType.TRADE);
                engine.storeData("key" + i, "value" + i);
            }
            
            Thread.sleep(1000); // Let events process
            
            // Performance test
            System.out.println("Starting performance test...");
            long startTime = System.nanoTime();
            int eventCount = 1_000_000;
            
            for (int i = 0; i < eventCount; i++) {
                engine.publishEvent("MSFT", 300.0 + i * 0.001, 50, Event.EventType.QUOTE);
                
                if (i % 10000 == 0) {
                    engine.storeData("batch" + i, "data" + i);
                }
            }
            
            Thread.sleep(2000); // Let all events process
            
            long endTime = System.nanoTime();
            long duration = endTime - startTime;
            
            System.out.printf("Processed %d events in %.2f ms%n", 
                eventCount, duration / 1_000_000.0);
            System.out.printf("Throughput: %.0f events/second%n", 
                eventCount * 1_000_000_000.0 / duration);
            System.out.printf("Average latency: %.2f nanoseconds per event%n", 
                (double) duration / eventCount);
            
            System.out.println("Final stats:");
            System.out.println("- Processed events: " + engine.getProcessedEventCount());
            System.out.println("- StringBuilder pool size: " + engine.getStringBuilderPoolSize());
            System.out.println("- Storage entries: " + engine.getStorageSize());
        }
    }
}