package tech.jorn.adrian.core.messaging;

import tech.jorn.adrian.core.IIdentifiable;
import tech.jorn.adrian.core.infrastructure.Node;

import java.util.function.Consumer;
import java.util.function.Function;

public interface IMessageBroker {
    <M> void broadcast(M message);
    <M> void send(IIdentifiable target, M message);
    <M, R> void send(IIdentifiable target, M message, Consumer<MessageResponse<R>> callback);

    <M> void onMessage(Consumer<MessageResponse<M>> message);

    void setSender(Node node);
}

