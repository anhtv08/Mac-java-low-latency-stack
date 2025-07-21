package com.lowlatency.core;

/**
 * Reusable event object for Disruptor ring buffer
 * Designed to minimize GC pressure through object reuse
 */
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
    
    // Default constructor required by Disruptor
    public Event() {}
    
    public void reset() {
        this.id = 0;
        this.symbol = null;
        this.price = 0.0;
        this.quantity = 0;
        this.timestamp = 0;
        this.type = null;
    }
    
    public void copyFrom(Event other) {
        this.id = other.id;
        this.symbol = other.symbol;
        this.price = other.price;
        this.quantity = other.quantity;
        this.timestamp = other.timestamp;
        this.type = other.type;
    }
    
    // Getters and setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    
    public long getQuantity() { return quantity; }
    public void setQuantity(long quantity) { this.quantity = quantity; }
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    
    public EventType getType() { return type; }
    public void setType(EventType type) { this.type = type; }
    
    @Override
    public String toString() {
        return String.format("Event{id=%d, symbol='%s', price=%.2f, quantity=%d, timestamp=%d, type=%s}",
                id, symbol, price, quantity, timestamp, type);
    }
}