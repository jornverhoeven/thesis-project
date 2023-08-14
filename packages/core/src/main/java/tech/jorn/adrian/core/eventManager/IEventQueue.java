package tech.jorn.adrian.core.eventManager;

import tech.jorn.adrian.core.eventManager.events.Event;
import tech.jorn.adrian.core.observables.SubscribableValueEvent;

import java.util.List;

public interface IEventQueue<E extends Event> {
    void enqueue(E e);
    E dequeue();

    SubscribableValueEvent<List<E>> onQueueChange();
}
