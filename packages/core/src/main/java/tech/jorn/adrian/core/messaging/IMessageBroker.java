package tech.jorn.adrian.core.messaging;

import tech.jorn.adrian.core.IIdentifiable;
import tech.jorn.adrian.core.graph.INode;

import java.util.function.Consumer;

public interface IMessageBroker {
    <M> void broadcast(M message);
    <M> void send(IIdentifiable target, M message);
    <M, R> void send(IIdentifiable target, M message, Consumer<MessageResponse<R>> callback);

    <M> void onMessage(Consumer<MessageResponse<M>> message);

    void setSender(INode node);
}

