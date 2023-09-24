package tech.jorn.adrian.experiment.instruments;

import com.sun.jdi.Value;
import tech.jorn.adrian.agent.AdrianAgent;
import tech.jorn.adrian.agent.events.IdentifyRiskEvent;
import tech.jorn.adrian.agent.events.ShareKnowledgeEvent;
import tech.jorn.adrian.core.agents.AgentState;
import tech.jorn.adrian.core.agents.IAgentConfiguration;
import tech.jorn.adrian.core.controllers.IController;
import tech.jorn.adrian.core.events.EventManager;
import tech.jorn.adrian.core.graphs.knowledgebase.KnowledgeBase;
import tech.jorn.adrian.core.messages.EventMessage;
import tech.jorn.adrian.core.messages.MessageBroker;
import tech.jorn.adrian.core.observables.ValueDispatcher;
import tech.jorn.adrian.core.services.RiskDetection;

import java.util.ArrayList;
import java.util.List;

public class ExperimentalAgent extends AdrianAgent {
    private final MessageBroker messageBroker;
    private final EventManager eventManager;
    private final ExperimentalRiskDetection riskDetection;
    private final KnowledgeBase knowledgeBase;

    public ExperimentalAgent(MessageBroker messageBroker, EventManager eventManager, ExperimentalRiskDetection riskDetection, KnowledgeBase knowledgeBase, List<IController> controllers, IAgentConfiguration configuration, ValueDispatcher<AgentState> agentState) {
        super(controllers, configuration, agentState);
        this.messageBroker = messageBroker;
        this.eventManager = eventManager;
        this.riskDetection = riskDetection;
        this.knowledgeBase = knowledgeBase;
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
