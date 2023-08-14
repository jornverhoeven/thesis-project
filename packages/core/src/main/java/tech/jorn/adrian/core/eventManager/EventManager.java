package tech.jorn.adrian.core.eventManager;

import tech.jorn.adrian.core.eventManager.events.Event;

public class EventManager {

    private IEventQueue eventQueue;

    EventManager() {
        this.eventQueue = new InMemoryEventQueue();
    }
    EventManager(IEventQueue eventQueue) {
        this.eventQueue = eventQueue;
    }

    public void onEvent(Event event) {
        this.eventQueue.enqueue(event);
    }


}
