package tech.jorn.adrian.agent.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.jorn.adrian.agent.events.IdentifyRiskEvent;
import tech.jorn.adrian.agent.events.ShareKnowledgeEvent;
import tech.jorn.adrian.core.agents.AgentState;
import tech.jorn.adrian.core.agents.IAgent;
import tech.jorn.adrian.core.agents.IAgentConfiguration;
import tech.jorn.adrian.core.controllers.AbstractController;
import tech.jorn.adrian.core.controllers.AbstractEventController;
import tech.jorn.adrian.core.events.EventManager;
import tech.jorn.adrian.core.graphs.knowledgebase.KnowledgeBase;
import tech.jorn.adrian.core.messages.EventMessage;
import tech.jorn.adrian.core.messages.MessageBroker;
import tech.jorn.adrian.core.observables.SubscribableEvent;
import tech.jorn.adrian.core.observables.SubscribableValueEvent;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;

public class KnowledgeController extends AbstractController {
    Logger log = LogManager.getLogger(KnowledgeController.class);

    private final KnowledgeBase knowledgeBase;
    private final MessageBroker messageBroker;
    private final IAgentConfiguration configuration;

    public KnowledgeController(KnowledgeBase knowledgeBase, MessageBroker messageBroker, EventManager eventManager, IAgentConfiguration configuration, SubscribableValueEvent<AgentState> agentState) {
        super(eventManager, agentState);
        this.messageBroker = messageBroker;
        this.knowledgeBase = knowledgeBase;
        this.configuration = configuration;

        this.eventManager.registerEventHandler(ShareKnowledgeEvent.class, this::processKnowledge);
        this.configuration.getParentNode().onPropertyChange().subscribe(prop -> this.log.debug("Updated property {} to {}", prop.getName(), prop.getValue()));
        this.configuration.getParentNode().onPropertyChange().subscribe(this::shareKnowledge);

        agentState.subscribe(state -> {
            // Once an agent is ready to send/receive events, we will send neighbours our information
            if (state != AgentState.Ready) return;

            var timer = new Timer();
            var task = new TimerTask() {

                @Override
                public void run() {
                    var event = new ShareKnowledgeEvent(
                            configuration.getParentNode(),
                            configuration.getNeighbours(),
                            configuration.getAssets(),
                            1
                    );
                    messageBroker.broadcast(new EventMessage<>(event));
                }
            };
            timer.schedule(task, 100);
        });
    }

    protected void processKnowledge(ShareKnowledgeEvent event) {
        if (knowledgeBase.findById(event.getOrigin().getID()).isEmpty() && event.getDistance() == 1) {
            this.messageBroker.addRecipient(event.getOrigin());
            this.log.info("Added a new neighbour {}", event.getOrigin().getID());
        }

        this.knowledgeBase.processNewInformation(event.getOrigin(), event.getLinks(), event.getAssets());

        if (event.getDistance() > 1) {
            var next = ShareKnowledgeEvent.reducedDistance(event);
            this.messageBroker.broadcast(new EventMessage<>(next));
        }

        this.eventManager.emit(new IdentifyRiskEvent());
    }

    protected void shareKnowledge() {
        var event = new ShareKnowledgeEvent(
                this.configuration.getParentNode(),
                this.configuration.getNeighbours(),
                this.configuration.getAssets(),
                1
        );
        this.messageBroker.broadcast(new EventMessage<>(event));
    }
}
