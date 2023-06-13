package tech.jorn.adrian.experiments.utils;

import tech.jorn.adrian.core.messaging.Message;
import tech.jorn.adrian.core.observables.EventDispatcher;
import tech.jorn.adrian.core.observables.SubscribableEvent;

public class MessageQueue {
    private final EventDispatcher<Message<?>> message = new EventDispatcher<>();

    public void push(Message<?> message) {
        this.message.dispatch(message);
    }

    public SubscribableEvent<Message<?>> onMessage() {
        return this.message.subscribable;
    }


}
