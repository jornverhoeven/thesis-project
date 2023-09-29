package tech.jorn.adrian.core.events;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.jorn.adrian.agent.events.SelectedProposalEvent;
import tech.jorn.adrian.core.events.queue.IEventQueue;
import tech.jorn.adrian.core.observables.EventDispatcher;
import tech.jorn.adrian.core.observables.SubscribableEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class EventManager {
    protected Logger log = LogManager.getLogger(EventManager.class);

    private final IEventQueue queue;
    private final Map<Class<Event>, List<Consumer<Event>>> eventHandlers = new HashMap<>();
    private final EventDispatcher<Void> finishedProcessing = new EventDispatcher<Void>();
    private boolean processing = false;

    public EventManager(IEventQueue queue) {
        this.queue = queue;

        this.queue.onNewEvent().subscribe(this::onEvent);
        this.finishedProcessing.subscribable.subscribe(this::onEvent);
    }

    public <E extends Event> void registerEventHandler(Class<E> eventClass, Consumer<E> eventHandler) {
        var handlers = this.eventHandlers.getOrDefault((Class<Event>) eventClass, new ArrayList<>());
        handlers.add((Consumer<Event>) eventHandler);
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

        var handlers = this.eventHandlers.get(event.getClass());
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
}