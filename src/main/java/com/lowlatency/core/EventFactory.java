package com.lowlatency.core;

import com.lmax.disruptor.EventFactory;

/**
 * Factory for creating Event instances in the Disruptor ring buffer
 */
public class EventFactory implements EventFactory<Event> {
    
    @Override
    public Event newInstance() {
        return new Event();
    }
}