package tech.jorn.adrian.core.events;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tech.jorn.adrian.core.agents.AgentState;
import tech.jorn.adrian.core.events.queue.IEventQueue;
import tech.jorn.adrian.core.observables.EventDispatcher;
import tech.jorn.adrian.core.observables.SubscribableValueEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class EventManager {
    protected Logger log = LogManager.getLogger(EventManager.class);

    private final IEventQueue queue;
    private final SubscribableValueEvent<AgentState> agentState;
    private final Map<Class<Event>, List<Consumer<Event>>> eventHandlers = new ConcurrentHashMap<>();
    private final EventDispatcher<Void> finishedProcessing = new EventDispatcher<Void>();
    private boolean processing = false;

    public EventManager(IEventQueue queue, SubscribableValueEvent<AgentState> agentState) {
        this.queue = queue;
        this.agentState = agentState;

        this.queue.onNewEvent().subscribe(this::onEvent);
        this.finishedProcessing.subscribable.subscribe(this::onEvent);
    }

    public <E extends Event> void registerEventHandler(Class<E> eventClass, Consumer<E> eventHandler) {
        var handlers = this.eventHandlers.getOrDefault((Class<Event>) eventClass, new CopyOnWriteArrayList<>());
        handlers.add((Consumer<Event>) eventHandler);
        this.eventHandlers.put((Class<Event>) eventClass, handlers);
    }

    public <E extends Event> void once(Class<E> eventClass, Consumer<E> eventHandler) {
        var handlers = this.eventHandlers.getOrDefault((Class<Event>) eventClass, new CopyOnWriteArrayList<>());
        handlers.add((Consumer<Event>)new Once<>(eventHandler, handlers));
        this.eventHandlers.put((Class<Event>) eventClass, handlers);
    }

    public void emit(Event event) {
        if (event.isImmediate()) {
            this.log.debug("Executing immediate \033[4m{}\033[0m", event.getClass().getSimpleName());
            processEvent(event, true);
            return;
        }

        if (!event.isDebugEvent())
            this.log.debug("Added \033[4m{}\033[0m to queue with {} events before it", event.getClass().getSimpleName(), queue.size());
        this.queue.enqueue(event);
    }

    private void onEvent() {
        if (this.processing) return;
        if (this.queue.size() == 0) return;
        readyForNextEvent();
    }

    private void readyForNextEvent() {
        var event = this.queue.dequeue();
        if (event == null) return;

        processEvent(event, false);
        this.log.trace("Done processing \033[4m{}\033[0m. processing: {}, queue size: {}", event.getClass().getSimpleName(), this.processing, this.queue.size());
    }

    private <E extends Event> void processEvent(E event, boolean immediate) {
        if (!immediate) this.processing = true;

        if (!event.isDebugEvent())
            this.log.debug("Processing event \033[4m{}\033[0m", event.getClass().getSimpleName());

        var handlers = this.getEventHandlers(event.getClass());
        if (handlers != null) {
            for (var handler : handlers) {
                handler.accept(event);
            }
        }

        if (!immediate) {
            this.processing = false;
            this.finishedProcessing.dispatch(null);
        }
    }

    private <E extends Event> List<Consumer<Event>> getEventHandlers(Class<E> eventClass) {
        return this.eventHandlers.keySet().stream()
            .filter(c -> {
                return c.equals(eventClass) || c.isAssignableFrom(eventClass);
            })
            .flatMap(c -> this.eventHandlers.get(c).stream())
            .toList();
    }

    public IEventQueue getQueue() {
        return queue;
    }
}

class Once<E extends Event> implements Consumer<E> {
    private final Consumer<E> handler;
    private final List<Consumer<Event>> handlers;

    public Once(Consumer<E> handler, List<Consumer<Event>> handlers) {
        this.handler = handler;
        this.handlers = handlers;
    }

    @Override
    public void accept(E t) {
        this.handler.accept(t);
        this.handlers.remove(this);
    }
}
