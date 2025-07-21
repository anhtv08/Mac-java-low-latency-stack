package com.lowlatency.core;

import com.lmax.disruptor.EventHandler;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * High-performance event handler for processing events from the ring buffer
 */
@Slf4j
@RequiredArgsConstructor
@Getter
public class LowLatencyEventHandler implements EventHandler<Event> {
    
    private final String handlerName;
    private long processedCount = 0;
    
    @Override
    public void onEvent(Event event, long sequence, boolean endOfBatch) throws Exception {
        log.debug("Processing event {} with sequence {}, endOfBatch: {}", event.getId(), sequence, endOfBatch);
        
        // Process the event - this is where your business logic goes
        processEvent(event);
        processedCount++;
        
        if (endOfBatch) {
            log.debug("End of batch reached, processed {} events total", processedCount);
            onBatchEnd();
        }
    }
    
    private void processEvent(Event event) {
        log.trace("Processing event: {}", event);
        
        if (event.getType() == null) {
            log.warn("Event {} has null type, skipping processing", event.getId());
            return;
        }
        
        // Example processing - replace with actual business logic
        switch (event.getType()) {
            case TRADE -> {
                log.debug("Processing TRADE event: {} for symbol {}", event.getId(), event.getSymbol());
                processTrade(event);
            }
            case QUOTE -> {
                log.debug("Processing QUOTE event: {} for symbol {}", event.getId(), event.getSymbol());
                processQuote(event);
            }
            case ORDER -> {
                log.debug("Processing ORDER event: {} for symbol {}", event.getId(), event.getSymbol());
                processOrder(event);
            }
        }
    }
    
    private void processTrade(Event event) {
        log.trace("Trade processing for event {}: price={}, quantity={}", 
                 event.getId(), event.getPrice(), event.getQuantity());
        // Trade processing logic
    }
    
    private void processQuote(Event event) {
        log.trace("Quote processing for event {}: price={}", event.getId(), event.getPrice());
        // Quote processing logic
    }
    
    private void processOrder(Event event) {
        log.trace("Order processing for event {}: quantity={}", event.getId(), event.getQuantity());
        // Order processing logic
    }
    
    private void onBatchEnd() {
        log.trace("Batch processing completed for handler: {}", handlerName);
        // Batch completion logic - e.g., flush buffers, send notifications
    }
}