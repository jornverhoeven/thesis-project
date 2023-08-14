package tech.jorn.adrian.experiments;

import tech.jorn.adrian.core.IIdentifiable;
import tech.jorn.adrian.core.graph.INode;
import tech.jorn.adrian.core.infrastructure.Node;
import tech.jorn.adrian.core.messaging.IMessageBroker;
import tech.jorn.adrian.core.messaging.Message;
import tech.jorn.adrian.core.messaging.MessageResponse;
import tech.jorn.adrian.experiments.utils.MessageQueue;

import java.util.List;
import java.util.function.Consumer;

public class ExperimentMessageBroker implements IMessageBroker {

    private final List<INode> neighbours;
    private final MessageQueue messageQueue;

    private INode sender;

    public ExperimentMessageBroker(List<INode> neighbours, MessageQueue messageQueue) {
        this.neighbours = neighbours;
        this.messageQueue = messageQueue;
    }

    @Override
    public <M> void broadcast(M message) {
        this.neighbours.forEach(node -> {
            var envelope = this.makeEnvelope(node, message);
            this.messageQueue.push(envelope);
        });
    }

    @Override
    public <M> void send(IIdentifiable target, M message) {
        var envelope = this.makeEnvelope(target, message);
        this.messageQueue.push(envelope);
    }

    @Override
    public <M, R> void send(IIdentifiable target, M message, Consumer<MessageResponse<R>> callback) {
        var envelope = this.makeEnvelope(target, message);

        var ref = new Object() {
            Consumer<MessageResponse<?>> wrapped;
        };
        ref.wrapped = response -> {
            if (!response.getReplyId().equals(envelope.getId())) return;
            callback.accept((MessageResponse<R>) response);
//            this.listeners.remove(ref.wrapped);
        };

//        this.listeners.add(ref.wrapped);
        this.messageQueue.onMessage().subscribe(m -> {
            ref.wrapped.accept(new MessageResponse<>(m.getData()));
        });
        this.messageQueue.push(envelope);
    }

    @Override
    public <M> void onMessage(Consumer<MessageResponse<M>> handler) {
        this.messageQueue.onMessage().subscribe(m -> {
            if (m.getRecipient().getID().equals(sender.getID()))
                handler.accept(new MessageResponse(m.getData()));
        });
    }

    private <M> Message<M> makeEnvelope(IIdentifiable target, M message) {
        return new Message<>(message, this.sender, target);
    }

    @Override
    public void setSender(INode sender) {
        this.sender = sender;
    }
}

