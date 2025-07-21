package com.lowlatency.core;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Reusable event object for Disruptor ring buffer
 * Designed to minimize GC pressure through object reuse
 */
@Data
@NoArgsConstructor
@Slf4j
public class Event {
    private long id;
    private String symbol;
    private double price;
    private long quantity;
    private long timestamp;
    private EventType type;
    
    public enum EventType {
        TRADE, QUOTE, ORDER
    }
    
    public void reset() {
        log.trace("Resetting event with id: {}", this.id);
        this.id = 0;
        this.symbol = null;
        this.price = 0.0;
        this.quantity = 0;
        this.timestamp = 0;
        this.type = null;
    }
    
    public void copyFrom(Event other) {
        log.trace("Copying event from id: {} to id: {}", other.id, this.id);
        this.id = other.id;
        this.symbol = other.symbol;
        this.price = other.price;
        this.quantity = other.quantity;
        this.timestamp = other.timestamp;
        this.type = other.type;
    }
    
    @Override
    public String toString() {
        return String.format("Event{id=%d, symbol='%s', price=%.2f, quantity=%d, timestamp=%d, type=%s}",
                id, symbol, price, quantity, timestamp, type);
    }
}