package com.lowlatency.core;

import com.lmax.disruptor.EventHandler;

/**
 * High-performance event handler for processing events from the ring buffer
 */
public class EventHandler implements EventHandler<Event> {
    
    private final String handlerName;
    private long processedCount = 0;
    
    public EventHandler(String handlerName) {
        this.handlerName = handlerName;
    }
    
    @Override
    public void onEvent(Event event, long sequence, boolean endOfBatch) throws Exception {
        // Process the event - this is where your business logic goes
        processEvent(event);
        processedCount++;
        
        if (endOfBatch) {
            // Batch processing optimization point
            onBatchEnd();
        }
    }
    
    private void processEvent(Event event) {
        // Example processing - replace with actual business logic
        switch (event.getType()) {
            case TRADE:
                processTrade(event);
                break;
            case QUOTE:
                processQuote(event);
                break;
            case ORDER:
                processOrder(event);
                break;
        }
    }
    
    private void processTrade(Event event) {
        // Trade processing logic
    }
    
    private void processQuote(Event event) {
        // Quote processing logic
    }
    
    private void processOrder(Event event) {
        // Order processing logic
    }
    
    private void onBatchEnd() {
        // Batch completion logic - e.g., flush buffers, send notifications
    }
    
    public long getProcessedCount() {
        return processedCount;
    }
    
    public String getHandlerName() {
        return handlerName;
    }
}