package com.lowlatency;

import com.lowlatency.core.Event;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Utility class for test data generation and common test operations
 */
public class TestUtils {
    
    private static final String[] SYMBOLS = {"AAPL", "MSFT", "GOOGL", "AMZN", "TSLA", "META", "NVDA", "NFLX"};
    private static final Event.EventType[] EVENT_TYPES = Event.EventType.values();
    
    /**
     * Create a random test event
     */
    public static Event createRandomEvent() {
        Event event = new Event();
        event.setId(ThreadLocalRandom.current().nextLong(1, 1000000));
        event.setSymbol(getRandomSymbol());
        event.setPrice(ThreadLocalRandom.current().nextDouble(50.0, 500.0));
        event.setQuantity(ThreadLocalRandom.current().nextLong(1, 1000));
        event.setTimestamp(System.nanoTime());
        event.setType(getRandomEventType());
        return event;
    }
    
    /**
     * Create a test event with specific parameters
     */
    public static Event createTestEvent(String symbol, double price, long quantity, Event.EventType type) {
        Event event = new Event();
        event.setId(ThreadLocalRandom.current().nextLong(1, 1000000));
        event.setSymbol(symbol);
        event.setPrice(price);
        event.setQuantity(quantity);
        event.setTimestamp(System.nanoTime());
        event.setType(type);
        return event;
    }
    
    /**
     * Get a random symbol from predefined list
     */
    public static String getRandomSymbol() {
        return SYMBOLS[ThreadLocalRandom.current().nextInt(SYMBOLS.length)];
    }
    
    /**
     * Get a random event type
     */
    public static Event.EventType getRandomEventType() {
        return EVENT_TYPES[ThreadLocalRandom.current().nextInt(EVENT_TYPES.length)];
    }
    
    /**
     * Generate test key-value pairs
     */
    public static String generateTestKey(int index) {
        return "testKey_" + index;
    }
    
    public static String generateTestValue(int index) {
        return "testValue_" + index + "_" + System.currentTimeMillis();
    }
    
    /**
     * Wait for async operations to complete
     */
    public static void waitForProcessing(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting", e);
        }
    }
    
    /**
     * Measure execution time of a runnable
     */
    public static long measureExecutionTime(Runnable operation) {
        long startTime = System.nanoTime();
        operation.run();
        return System.nanoTime() - startTime;
    }
    
    /**
     * Calculate throughput in operations per second
     */
    public static double calculateThroughput(long operations, long durationNanos) {
        return operations * 1_000_000_000.0 / durationNanos;
    }
}