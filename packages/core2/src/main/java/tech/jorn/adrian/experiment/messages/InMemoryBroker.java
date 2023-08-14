package tech.jorn.adrian.experiment.messages;

import tech.jorn.adrian.core.graphs.base.INode;
import tech.jorn.adrian.core.messages.Message;
import tech.jorn.adrian.core.messages.MessageBroker;
import tech.jorn.adrian.core.observables.EventDispatcher;
import tech.jorn.adrian.core.observables.SubscribableEvent;

import java.util.List;

public class InMemoryBroker implements MessageBroker {
    private final INode node;
    private final List<String> neighbours;
    private final EventDispatcher<Envelope> messageDispatcher;

    /**
     * @param events Get the event dispatcher from elsewhere for testing purposes.
     */
    public InMemoryBroker(INode node, List<String> neighbours, EventDispatcher<Envelope> events) {
        this.node = node;
        this.neighbours = neighbours;
        this.messageDispatcher = events;
    }

    @Override
    public void send(INode recipient, Message message) {
        this.messageDispatcher.dispatch(new Envelope(this.node, recipient.getID(), message));
    }

    @Override
    public void broadcast(Message message) {
        this.neighbours.forEach(neighbour ->
                this.messageDispatcher.dispatch(new Envelope(this.node, neighbour, message))
        );
    }

    @Override
    public SubscribableEvent<Message> onNewMessage() {
        var wrappedDispatcher = new EventDispatcher<Message>();
        this.messageDispatcher.subscribable.subscribe(e -> {
            if (e.recipient() == null || e.recipient().equals(this.node.getID()))
                wrappedDispatcher.dispatch(e.message());
        });
        return wrappedDispatcher.subscribable;
    }
}
