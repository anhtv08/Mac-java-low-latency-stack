package com.lowlatency.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

class EventHandlerTest {
    
    private EventHandler eventHandler;
    private Event testEvent;
    
    @BeforeEach
    void setUp() {
        eventHandler = new EventHandler("TestHandler");
        testEvent = new Event();
        testEvent.setId(1L);
        testEvent.setSymbol("TEST");
        testEvent.setPrice(100.0);
        testEvent.setQuantity(50L);
        testEvent.setTimestamp(System.nanoTime());
    }
    
    @Test
    void testEventHandlerCreation() {
        assertNotNull(eventHandler);
        assertEquals("TestHandler", eventHandler.getHandlerName());
        assertEquals(0, eventHandler.getProcessedCount());
    }
    
    @Test
    void testTradeEventProcessing() throws Exception {
        testEvent.setType(Event.EventType.TRADE);
        
        eventHandler.onEvent(testEvent, 1L, false);
        
        assertEquals(1, eventHandler.getProcessedCount());
    }
    
    @Test
    void testQuoteEventProcessing() throws Exception {
        testEvent.setType(Event.EventType.QUOTE);
        
        eventHandler.onEvent(testEvent, 2L, false);
        
        assertEquals(1, eventHandler.getProcessedCount());
    }
    
    @Test
    void testOrderEventProcessing() throws Exception {
        testEvent.setType(Event.EventType.ORDER);
        
        eventHandler.onEvent(testEvent, 3L, false);
        
        assertEquals(1, eventHandler.getProcessedCount());
    }
    
    @Test
    void testBatchProcessing() throws Exception {
        testEvent.setType(Event.EventType.TRADE);
        
        // Process multiple events, last one is end of batch
        eventHandler.onEvent(testEvent, 1L, false);
        eventHandler.onEvent(testEvent, 2L, false);
        eventHandler.onEvent(testEvent, 3L, true); // End of batch
        
        assertEquals(3, eventHandler.getProcessedCount());
    }
    
    @Test
    void testMultipleEventProcessing() throws Exception {
        for (int i = 0; i < 100; i++) {
            testEvent.setType(Event.EventType.values()[i % 3]);
            eventHandler.onEvent(testEvent, i, i == 99);
        }
        
        assertEquals(100, eventHandler.getProcessedCount());
    }
    
    @Test
    void testNullEventType() throws Exception {
        testEvent.setType(null);
        
        // Should not throw exception
        assertDoesNotThrow(() -> eventHandler.onEvent(testEvent, 1L, false));
        assertEquals(1, eventHandler.getProcessedCount());
    }
}