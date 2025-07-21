package com.lowlatency;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;
import com.lowlatency.core.Event;
import com.lowlatency.core.LowLatencyEventFactory;
import com.lowlatency.core.LowLatencyEventHandler;
import com.lowlatency.pool.ObjectPool;
import com.lowlatency.storage.ChronicleMapStorage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.ThreadFactory;

/**
 * Main low-latency processing engine combining Disruptor, Chronicle Map, and Object Pooling
 */
@Slf4j
public class LowLatencyEngine implements AutoCloseable {
    
    private static final int RING_BUFFER_SIZE = 1024 * 64; // Must be power of 2
    private static final int OBJECT_POOL_SIZE = 1000;
    private static final long STORAGE_ENTRIES = 1_000_000;
    
    private final Disruptor<Event> disruptor;
    private final RingBuffer<Event> ringBuffer;
    private final ObjectPool<StringBuilder> stringBuilderPool;
    private final ChronicleMapStorage<String, String> storage;
    private final LowLatencyEventHandler eventHandler;
    
    public LowLatencyEngine() throws IOException {
        log.info("Initializing LowLatencyEngine with ringBufferSize={}, objectPoolSize={}, storageEntries={}", 
                RING_BUFFER_SIZE, OBJECT_POOL_SIZE, STORAGE_ENTRIES);
        
        // Initialize object pool for StringBuilder reuse
        log.debug("Initializing StringBuilder object pool");
        this.stringBuilderPool = new ObjectPool<>(
            () -> new StringBuilder(256), 
            OBJECT_POOL_SIZE / 2, 
            OBJECT_POOL_SIZE
        );
        
        // Initialize Chronicle Map storage
        log.debug("Initializing Chronicle Map storage");
        this.storage = ChronicleMapStorage.create(
            String.class, 
            String.class, 
            STORAGE_ENTRIES, 
            "low-latency-data.dat"
        );
        
        // Initialize Disruptor
        log.debug("Initializing LMAX Disruptor with ring buffer size: {}", RING_BUFFER_SIZE);
        ThreadFactory threadFactory = DaemonThreadFactory.INSTANCE;
        this.disruptor = new Disruptor<>(new LowLatencyEventFactory(), RING_BUFFER_SIZE, threadFactory);
        
        // Set up event handler
        log.debug("Setting up event handler: MainHandler");
        this.eventHandler = new LowLatencyEventHandler("MainHandler");
        this.disruptor.handleEventsWith(eventHandler);
        
        // Start the disruptor
        log.info("Starting LMAX Disruptor");
        this.disruptor.start();
        this.ringBuffer = disruptor.getRingBuffer();
        
        log.info("LowLatencyEngine initialized successfully");
    }
    
    /**
     * Publish an event to the ring buffer
     */
    public void publishEvent(String symbol, double price, long quantity, Event.EventType type) {
        log.trace("Publishing event: symbol={}, price={}, quantity={}, type={}", symbol, price, quantity, type);
        long sequence = ringBuffer.next();
        try {
            Event event = ringBuffer.get(sequence);
            event.setId(sequence);
            event.setSymbol(symbol);
            event.setPrice(price);
            event.setQuantity(quantity);
            event.setTimestamp(System.nanoTime());
            event.setType(type);
            log.trace("Event published with sequence: {}", sequence);
        } finally {
            ringBuffer.publish(sequence);
        }
    }
    
    /**
     * Store data using Chronicle Map
     */
    public void storeData(String key, String value) {
        log.trace("Storing data: {} -> {}", key, value);
        storage.put(key, value);
    }
    
    /**
     * Retrieve data from Chronicle Map
     */
    public String getData(String key) {
        log.trace("Retrieving data for key: {}", key);
        String value = storage.get(key);
        log.trace("Retrieved value: {} for key: {}", value, key);
        return value;
    }
    
    /**
     * Get a StringBuilder from the object pool
     */
    public StringBuilder acquireStringBuilder() {
        log.trace("Acquiring StringBuilder from pool");
        return stringBuilderPool.acquire();
    }
    
    /**
     * Return a StringBuilder to the object pool
     */
    public void releaseStringBuilder(StringBuilder sb) {
        log.trace("Releasing StringBuilder to pool");
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
        log.info("Shutting down LowLatencyEngine");
        if (disruptor != null) {
            log.debug("Halting and shutting down Disruptor");
            disruptor.halt();
            disruptor.shutdown();
        }
        if (storage != null) {
            log.debug("Closing Chronicle Map storage");
            storage.close();
        }
        log.info("LowLatencyEngine shutdown completed");
    }
    
    /**
     * Example usage and performance test
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        log.info("Starting LowLatencyEngine performance test");
        
        try (LowLatencyEngine engine = new LowLatencyEngine()) {
            
            // Warm up
            log.info("Starting warmup phase with 10,000 events");
            for (int i = 0; i < 10000; i++) {
                engine.publishEvent("AAPL", 150.0 + i * 0.01, 100, Event.EventType.TRADE);
                engine.storeData("key" + i, "value" + i);
            }
            
            Thread.sleep(1000); // Let events process
            log.info("Warmup completed");
            
            // Performance test
            log.info("Starting performance test with 1,000,000 events");
            long startTime = System.nanoTime();
            int eventCount = 1_000_000;
            
            for (int i = 0; i < eventCount; i++) {
                engine.publishEvent("MSFT", 300.0 + i * 0.001, 50, Event.EventType.QUOTE);
                
                if (i % 10000 == 0) {
                    engine.storeData("batch" + i, "data" + i);
                    if (i % 100000 == 0) {
                        log.debug("Processed {} events", i);
                    }
                }
            }
            
            Thread.sleep(2000); // Let all events process
            
            long endTime = System.nanoTime();
            long duration = endTime - startTime;
            
            // Performance results
            double durationMs = duration / 1_000_000.0;
            double throughput = eventCount * 1_000_000_000.0 / duration;
            double avgLatencyNs = (double) duration / eventCount;
            
            log.info("Performance test completed:");
            log.info("- Processed {} events in {:.2f} ms", eventCount, durationMs);
            log.info("- Throughput: {:.0f} events/second", throughput);
            log.info("- Average latency: {:.2f} nanoseconds per event", avgLatencyNs);
            
            // Final statistics
            log.info("Final statistics:");
            log.info("- Total processed events: {}", engine.getProcessedEventCount());
            log.info("- StringBuilder pool size: {}", engine.getStringBuilderPoolSize());
            log.info("- Storage entries: {}", engine.getStorageSize());
            
        } catch (Exception e) {
            log.error("Error during performance test", e);
            throw e;
        }
        
        log.info("Performance test completed successfully");
    }
}