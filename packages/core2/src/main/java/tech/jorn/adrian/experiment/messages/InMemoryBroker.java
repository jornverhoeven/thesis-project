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
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

public class InMemoryBroker implements MessageBroker {
    private final Logger log = LogManager.getLogger(InMemoryBroker.class);

    private final INode node;
    private final List<String> neighbours;
    private final EventDispatcher<Envelope> messageDispatcher;
    private final Executor executor = Executors.newFixedThreadPool(1);

    private final ConcurrentLinkedQueue<Consumer<Message>> messageHandlers = new ConcurrentLinkedQueue<>();

    /**
     * @param events Get the event dispatcher from elsewhere for testing purposes.
     */
    public InMemoryBroker(INode node, List<String> neighbours, EventDispatcher<Envelope> events) {
        this.node = node;
        this.neighbours = neighbours;
        this.messageDispatcher = events;

        events.subscribable.subscribe(this::onMessageInner);
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
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ex) {
                    log.warn("could not sleep");
                }
                wrappedDispatcher.dispatch(e.message());
            }
        });
        return wrappedDispatcher.subscribable;
    }

    @Override
    public void onMessage(Consumer<Message> messageHandler) {
        messageHandlers.add(messageHandler);
    }

    private void scheduleDispatch(Envelope envelope) {
//        log.debug("Sending from {} to {} (current: {})", envelope.sender().getID(), envelope.recipient(), this.node.getID());
        this.messageDispatcher.dispatch(envelope);
    }

    private void onMessageInner(Envelope envelope) {
//        log.debug("Received from {} to {} (current: {})", envelope.sender().getID(), envelope.recipient(), this.node.getID());
        if (envelope.recipient() != null && envelope.recipient().equals(this.node.getID())) {
            log.debug("\033[4m{}\033[0m sent from \033[4m{}\033[0m to \033[4m{}\033[0m (event {})",
                    envelope.message().getClass().getSimpleName(),
                    envelope.sender().getID(), envelope.recipient(),
                    ((EventMessage<?>) envelope.message()).getEvent().getClass().getSimpleName());
            try {
                Thread.sleep(1);
            } catch (InterruptedException ex) {
                log.warn("could not sleep");
            }

            for (Consumer<Message> handler : this.messageHandlers) {
                this.executor.execute(() -> handler.accept(envelope.message()));
            }
        }
    }
}
