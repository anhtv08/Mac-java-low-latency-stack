package com.lowlatency.pool;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

/**
 * High-performance object pool to minimize GC pressure
 * Thread-safe implementation using lock-free data structures
 */
@Slf4j
public class ObjectPool<T> {
    
    private final ConcurrentLinkedQueue<T> pool;
    private final Supplier<T> factory;
    @Getter
    private final int maxSize;
    private volatile int currentSize;
    
    public ObjectPool(Supplier<T> factory, int initialSize, int maxSize) {
        this.factory = factory;
        this.maxSize = maxSize;
        this.pool = new ConcurrentLinkedQueue<>();
        this.currentSize = 0;
        
        log.info("Initializing ObjectPool with initialSize: {}, maxSize: {}", initialSize, maxSize);
        
        // Pre-populate the pool
        for (int i = 0; i < initialSize; i++) {
            pool.offer(factory.get());
            currentSize++;
        }
        
        log.debug("ObjectPool initialized with {} objects", currentSize);
    }
    
    public T acquire() {
        T object = pool.poll();
        if (object == null) {
            log.trace("Pool empty, creating new object");
            // Pool is empty, create new object
            object = factory.get();
        } else {
            currentSize--;
            log.trace("Acquired object from pool, remaining: {}", currentSize);
        }
        return object;
    }
    
    public void release(T object) {
        if (object != null && currentSize < maxSize) {
            // Reset object state if it implements Resetable
            if (object instanceof Resetable resetable) {
                log.trace("Resetting object before returning to pool");
                resetable.reset();
            }
            pool.offer(object);
            currentSize++;
            log.trace("Released object to pool, current size: {}", currentSize);
        } else if (object != null && currentSize >= maxSize) {
            log.trace("Pool at max capacity, discarding object");
        }
    }
    
    public int size() {
        return currentSize;
    }
    
    /**
     * Interface for objects that can be reset before being returned to pool
     */
    public interface Resetable {
        void reset();
    }
}