package tech.jorn.adrian.core.messaging;

import tech.jorn.adrian.core.IIdentifiable;
import java.util.UUID;

public class Message<T> {
    private final String id = String.valueOf(UUID.randomUUID());

    private final T data;
    private final IIdentifiable sender;
    private final IIdentifiable recipient;

    public Message(T data, IIdentifiable sender, IIdentifiable recipient) {
        this.data = data;
        this.sender = sender;
        this.recipient = recipient;
    }

    public String getId() {
        return id;
    }
    public IIdentifiable getSender() { return this.sender; }
    public IIdentifiable getRecipient() {
        return this.recipient;
    }

    public T getData() {
        return data;
    }
}
