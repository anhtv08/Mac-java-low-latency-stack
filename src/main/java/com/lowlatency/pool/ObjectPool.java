package com.lowlatency.pool;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

/**
 * High-performance object pool to minimize GC pressure
 * Thread-safe implementation using lock-free data structures
 */
public class ObjectPool<T> {
    
    private final ConcurrentLinkedQueue<T> pool;
    private final Supplier<T> factory;
    private final int maxSize;
    private volatile int currentSize;
    
    public ObjectPool(Supplier<T> factory, int initialSize, int maxSize) {
        this.factory = factory;
        this.maxSize = maxSize;
        this.pool = new ConcurrentLinkedQueue<>();
        this.currentSize = 0;
        
        // Pre-populate the pool
        for (int i = 0; i < initialSize; i++) {
            pool.offer(factory.get());
            currentSize++;
        }
    }
    
    public T acquire() {
        T object = pool.poll();
        if (object == null) {
            // Pool is empty, create new object
            object = factory.get();
        } else {
            currentSize--;
        }
        return object;
    }
    
    public void release(T object) {
        if (object != null && currentSize < maxSize) {
            // Reset object state if it implements Resetable
            if (object instanceof Resetable) {
                ((Resetable) object).reset();
            }
            pool.offer(object);
            currentSize++;
        }
    }
    
    public int size() {
        return currentSize;
    }
    
    public int maxSize() {
        return maxSize;
    }
    
    /**
     * Interface for objects that can be reset before being returned to pool
     */
    public interface Resetable {
        void reset();
    }
}