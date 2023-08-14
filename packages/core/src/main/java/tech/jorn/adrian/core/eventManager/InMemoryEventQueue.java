package tech.jorn.adrian.core.eventManager;

import tech.jorn.adrian.core.eventManager.events.Event;
import tech.jorn.adrian.core.observables.SubscribableValueEvent;
import tech.jorn.adrian.core.observables.ValueDispatcher;

import java.util.ArrayList;
import java.util.List;

public class InMemoryEventQueue implements IEventQueue<Event> {
    private final ValueDispatcher<List<Event>> queue;

    public InMemoryEventQueue() {
        this.queue = new ValueDispatcher<>(new ArrayList<>());

    }

    @Override
    public void enqueue(Event e) {
        this.queue.current().add(e);
        this.queue.setCurrent(this.queue.current());
    }

    @Override
    public Event dequeue() {
        var event = this.queue.current().get(0);
        this.queue.current().remove(event);
        this.queue.setCurrent(this.queue.current());
        return event;
    }

    @Override
    public SubscribableValueEvent<List<Event>> onQueueChange() {
        return this.queue.subscribable;
    }
}
