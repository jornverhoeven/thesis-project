package tech.jorn.adrian.agent.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.jorn.adrian.agent.events.IdentifyRiskEvent;
import tech.jorn.adrian.agent.events.ShareKnowledgeEvent;
import tech.jorn.adrian.core.controllers.AbstractEventController;
import tech.jorn.adrian.core.events.EventManager;
import tech.jorn.adrian.core.graphs.knowledgebase.KnowledgeBase;
import tech.jorn.adrian.core.messages.EventMessage;
import tech.jorn.adrian.core.messages.MessageBroker;

public class KnowledgeController extends AbstractEventController<ShareKnowledgeEvent> {
    Logger log = LogManager.getLogger(KnowledgeController.class);

    private final KnowledgeBase knowledgeBase;
    private final MessageBroker messageBroker;

    public KnowledgeController(MessageBroker messageBroker, EventManager eventManager) {
        this(new KnowledgeBase(), messageBroker, eventManager);
    }

    public KnowledgeController(KnowledgeBase knowledgeBase, MessageBroker messageBroker, EventManager eventManager) {
        super(ShareKnowledgeEvent.class, eventManager);
        this.messageBroker = messageBroker;
        this.knowledgeBase = knowledgeBase;
    }

    @Override
    protected void processEvent(ShareKnowledgeEvent event) {
        this.knowledgeBase.processNewInformation(event.getOrigin(), event.getLinks());

        if (event.getDistance() > 1) {
            var next = ShareKnowledgeEvent.reducedDistance(event);
            this.messageBroker.broadcast(new EventMessage<>(next));
        }

//        this.eventManager.onEvent(new IdentifyRiskEvent());
    }
}
