package tech.jorn.adrian.experiment.messages;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.jorn.adrian.core.graphs.base.INode;
import tech.jorn.adrian.core.messages.EventMessage;
import tech.jorn.adrian.core.messages.Message;
import tech.jorn.adrian.core.messages.MessageBroker;
import tech.jorn.adrian.core.observables.EventDispatcher;
import tech.jorn.adrian.core.observables.SubscribableEvent;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class InMemoryBroker implements MessageBroker, AutoCloseable {
    private final Logger log = LogManager.getLogger(InMemoryBroker.class);

    private final INode node;
    private final List<String> neighbours;
    private final EventDispatcher<Envelope> messageDispatcher;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    /**
     * @param events Get the event dispatcher from elsewhere for testing purposes.
     */
    public InMemoryBroker(INode node, List<String> neighbours, EventDispatcher<Envelope> events) {
        this.node = node;
        this.neighbours = neighbours;
        this.messageDispatcher = events;
    }

    @Override
    public void close() {
        try {
            scheduler.shutdown();
        } catch (Exception e) {
            // ...
        }
    }

    @Override
    public void send(INode recipient, Message message) {
        this.scheduleDispatch(new Envelope(this.node, recipient.getID(), message));
    }

    @Override
    public void broadcast(Message message) {
        this.neighbours.forEach(neighbour ->
                this.scheduleDispatch(new Envelope(this.node, neighbour, message))
        );
    }

    @Override
    public void addRecipient(INode recipient) {
        this.neighbours.add(recipient.getID());
    }

    @Override
    public SubscribableEvent<Message> onNewMessage() {
        var wrappedDispatcher = new EventDispatcher<Message>();
        this.messageDispatcher.subscribable.subscribe(e -> {
            if (e.recipient() == null || e.recipient().equals(this.node.getID())) {
                log.debug("\033[4m{}\033[0m sent from \033[4m{}\033[0m to \033[4m{}\033[0m (event {})", e.message().getClass().getSimpleName(), e.sender().getID(), e.recipient(), ((EventMessage<?>) e.message()).getEvent().getClass().getSimpleName());
                try { Thread.sleep(1); }
                catch (InterruptedException ex) { log.warn("could not sleep"); }
                wrappedDispatcher.dispatch(e.message());
            }
        });
        return wrappedDispatcher.subscribable;
    }

    private void scheduleDispatch(Envelope envelope) {
//        var scheduler = Executors.newSingleThreadScheduledExecutor();
//        this.scheduler.schedule(() -> {
            this.messageDispatcher.dispatch(envelope);
//        }, 1000, TimeUnit.MILLISECONDS);
    }
}
