package tech.jorn.adrian.experiment;

import tech.jorn.adrian.agent.AdrianAgent;
import tech.jorn.adrian.agent.events.IdentifyRiskEvent;
import tech.jorn.adrian.agent.events.ShareKnowledgeEvent;
import tech.jorn.adrian.core.agents.IAgentConfiguration;
import tech.jorn.adrian.core.controllers.IController;
import tech.jorn.adrian.core.events.EventManager;
import tech.jorn.adrian.core.messages.EventMessage;
import tech.jorn.adrian.core.messages.MessageBroker;

import java.util.List;

public class ExperimentalAgent extends AdrianAgent {
    private final MessageBroker messageBroker;
    private final EventManager eventManager;

    public ExperimentalAgent(MessageBroker messageBroker, EventManager eventManager, List<IController> controllers, IAgentConfiguration configuration) {
        super(controllers, configuration);
        this.messageBroker = messageBroker;
        this.eventManager = eventManager;
    }

    public void shareKnowledge() {
        var event = new ShareKnowledgeEvent(
                this.getConfiguration().getParentNode(),
                this.getConfiguration().getNeighbours(),
                1
        );
        this.messageBroker.broadcast(new EventMessage<>(event));
    }
    public void identifyRisk() {
        var event = new IdentifyRiskEvent();
        this.eventManager.emit(event);
    }

    public EventManager getEventManager() {
        return this.eventManager;
    }
}
