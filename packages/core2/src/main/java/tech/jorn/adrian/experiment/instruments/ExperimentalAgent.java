package tech.jorn.adrian.experiment.instruments;

import tech.jorn.adrian.agent.AdrianAgent;
import tech.jorn.adrian.agent.events.IdentifyRiskEvent;
import tech.jorn.adrian.agent.events.ShareKnowledgeEvent;
import tech.jorn.adrian.core.agents.IAgentConfiguration;
import tech.jorn.adrian.core.controllers.IController;
import tech.jorn.adrian.core.events.EventManager;
import tech.jorn.adrian.core.graphs.knowledgebase.KnowledgeBase;
import tech.jorn.adrian.core.messages.EventMessage;
import tech.jorn.adrian.core.messages.MessageBroker;
import tech.jorn.adrian.core.services.RiskDetection;

import java.util.ArrayList;
import java.util.List;

public class ExperimentalAgent extends AdrianAgent {
    private final MessageBroker messageBroker;
    private final EventManager eventManager;
    private final ExperimentalRiskDetection riskDetection;
    private final KnowledgeBase knowledgeBase;

    public ExperimentalAgent(MessageBroker messageBroker, EventManager eventManager, ExperimentalRiskDetection riskDetection, KnowledgeBase knowledgeBase, List<IController> controllers, IAgentConfiguration configuration) {
        super(controllers, configuration);
        this.messageBroker = messageBroker;
        this.eventManager = eventManager;
        this.riskDetection = riskDetection;
        this.knowledgeBase = knowledgeBase;
    }

    public void shareKnowledge() {
        var event = new ShareKnowledgeEvent(
                this.getConfiguration().getParentNode(),
                this.getConfiguration().getNeighbours(),
                this.getConfiguration().getAssets(),
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
    public MessageBroker getMessageBroker() {
        return this.messageBroker;
    }
    public ExperimentalRiskDetection getRiskDetection() {
        return this.riskDetection;
    }
    public KnowledgeBase getKnowledgeBase() {
        return this.knowledgeBase;
    }
}
