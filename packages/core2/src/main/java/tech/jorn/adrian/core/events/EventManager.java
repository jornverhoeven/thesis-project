package tech.jorn.adrian.core.events;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.jorn.adrian.core.events.queue.IEventQueue;
import tech.jorn.adrian.core.events.queue.InMemoryQueue;
import tech.jorn.adrian.core.observables.EventDispatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class EventManager implements AutoCloseable {
    protected Logger log = LogManager.getLogger(EventManager.class);

    private final IEventQueue queue;
    private final List<EventHandler<Event>> eventHandlers;
    private boolean processing = false;
    private final EventDispatcher<Void> finishedEvent = new EventDispatcher<>();

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public EventManager() {
        this(new InMemoryQueue());
    }

    public EventManager(IEventQueue queue) {
        this.queue = queue;
        this.eventHandlers = new ArrayList<>();

        this.queue.onNewEvent().subscribe(e -> {
            log.debug("Received event \033[4m{}\033[0m", e.getClass().getSimpleName());
            // Execute the event handlers if we have a new event, it's the only one, and we are not processing another.
            if (!this.processing && this.queue.size() == 1) this.finishedEvent.dispatch(null);
        });

        this.finishedEvent.subscribable.subscribe(() -> {
            var event = this.queue.dequeue();
            if (event != null) this.nextEvent(event);
        });
    }

    public <E extends Event> void registerEventHandler(Class<E> event, Consumer<E> handler) {
        this.registerEventHandler(event, handler, false);
    }
    public <E extends Event> void registerEventHandler(Class<E> event, Consumer<E> handler, boolean strict) {
//        log.debug("Registering event handler for event \033[4m{}\033[0m", event.getSimpleName());
        this.eventHandlers.add(new EventHandler<>(
                (Class<Event>) event,
                (Consumer<Event>) handler,
                strict)
        );
    }

    public <E extends Event> void emit(E event) {
        this.queue.enqueue(event);
    }

    private <E extends Event> void nextEvent(E event) {
        this.processing = true;
        log.debug("Processing event \033[4m{}\033[0m", event.getClass().getSimpleName());

        var processed = new AtomicBoolean(false);

//        this.scheduler.schedule(() -> {
            this.eventHandlers.forEach(handler -> {
                if (handler.strict() && !handler.eventType().equals(event.getClass())) return;
                if (!handler.strict() && !event.getClass().isAssignableFrom(handler.eventType())) return;

                handler.handler().accept(event);
                processed.set(true);
                try { Thread.sleep(1); }
                catch (InterruptedException e ) { this.log.warn("Could not sleep"); }
            });

            if (!processed.get()) {
                log.warn("No event handler for event \033[4m{}\033[0m", event.getClass().getSimpleName());
            }

            this.processing = false;
            this.finishedEvent.dispatch(null);
//        }, 1000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void close() throws Exception {
        this.scheduler.shutdown();
    }
}

record EventHandler<E extends Event>(Class<E> eventType, Consumer<E> handler, boolean strict) {
}