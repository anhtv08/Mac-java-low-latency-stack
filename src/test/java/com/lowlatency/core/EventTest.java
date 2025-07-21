package com.lowlatency.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

class EventTest {
    
    private Event event;
    
    @BeforeEach
    void setUp() {
        event = new Event();
    }
    
    @Test
    void testEventCreation() {
        assertNotNull(event);
        assertEquals(0, event.getId());
        assertNull(event.getSymbol());
        assertEquals(0.0, event.getPrice());
        assertEquals(0, event.getQuantity());
        assertEquals(0, event.getTimestamp());
        assertNull(event.getType());
    }
    
    @Test
    void testEventSettersAndGetters() {
        event.setId(123L);
        event.setSymbol("AAPL");
        event.setPrice(150.50);
        event.setQuantity(1000L);
        event.setTimestamp(System.nanoTime());
        event.setType(Event.EventType.TRADE);
        
        assertEquals(123L, event.getId());
        assertEquals("AAPL", event.getSymbol());
        assertEquals(150.50, event.getPrice());
        assertEquals(1000L, event.getQuantity());
        assertTrue(event.getTimestamp() > 0);
        assertEquals(Event.EventType.TRADE, event.getType());
    }
    
    @Test
    void testEventReset() {
        // Set up event with data
        event.setId(123L);
        event.setSymbol("MSFT");
        event.setPrice(300.0);
        event.setQuantity(500L);
        event.setTimestamp(System.nanoTime());
        event.setType(Event.EventType.QUOTE);
        
        // Reset the event
        event.reset();
        
        // Verify all fields are reset
        assertEquals(0, event.getId());
        assertNull(event.getSymbol());
        assertEquals(0.0, event.getPrice());
        assertEquals(0, event.getQuantity());
        assertEquals(0, event.getTimestamp());
        assertNull(event.getType());
    }
    
    @Test
    void testEventCopyFrom() {
        Event sourceEvent = new Event();
        sourceEvent.setId(456L);
        sourceEvent.setSymbol("GOOGL");
        sourceEvent.setPrice(2500.0);
        sourceEvent.setQuantity(200L);
        sourceEvent.setTimestamp(12345L);
        sourceEvent.setType(Event.EventType.ORDER);
        
        event.copyFrom(sourceEvent);
        
        assertEquals(456L, event.getId());
        assertEquals("GOOGL", event.getSymbol());
        assertEquals(2500.0, event.getPrice());
        assertEquals(200L, event.getQuantity());
        assertEquals(12345L, event.getTimestamp());
        assertEquals(Event.EventType.ORDER, event.getType());
    }
    
    @Test
    void testEventToString() {
        event.setId(789L);
        event.setSymbol("TSLA");
        event.setPrice(800.25);
        event.setQuantity(100L);
        event.setTimestamp(98765L);
        event.setType(Event.EventType.TRADE);
        
        String eventString = event.toString();
        
        assertTrue(eventString.contains("789"));
        assertTrue(eventString.contains("TSLA"));
        assertTrue(eventString.contains("800.25"));
        assertTrue(eventString.contains("100"));
        assertTrue(eventString.contains("98765"));
        assertTrue(eventString.contains("TRADE"));
    }
    
    @Test
    void testEventTypes() {
        assertEquals(3, Event.EventType.values().length);
        assertNotNull(Event.EventType.valueOf("TRADE"));
        assertNotNull(Event.EventType.valueOf("QUOTE"));
        assertNotNull(Event.EventType.valueOf("ORDER"));
    }
}