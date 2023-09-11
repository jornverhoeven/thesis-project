package tech.jorn.adrian.core.messages;

import tech.jorn.adrian.core.events.Event;
import tech.jorn.adrian.core.graphs.base.INode;
import tech.jorn.adrian.core.observables.SubscribableEvent;

import java.util.function.Consumer;

public interface MessageBroker {
    void send(INode recipient, Message message);
    void broadcast(Message message);
//    void handleMessage(Consumer<Message> messageHandler);

    void addRecipient(INode recipient);

    SubscribableEvent<Message> onNewMessage();
}

