package tech.jorn.adrian.core.eventManager;

public class EventManager {

    private EventQueue eventQueue;

    EventManager() {
        this.eventQueue = new InMemoryEventQueue();
    }
    EventManager(EventQueue eventQueue) {
        this.eventQueue = eventQueue;
    }
}
