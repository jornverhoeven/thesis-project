package tech.jorn.adrian.agent;

import tech.jorn.adrian.core.agents.AgentState;
import tech.jorn.adrian.core.agents.IAgent;
import tech.jorn.adrian.core.agents.IAgentConfiguration;
import tech.jorn.adrian.core.controllers.IController;
import tech.jorn.adrian.core.observables.SubscribableValueEvent;
import tech.jorn.adrian.core.observables.ValueDispatcher;

import java.util.List;

public class AdrianAgent implements IAgent {

    protected final List<IController> controllers;
    private final IAgentConfiguration configuration;
    private final ValueDispatcher<AgentState> agentState = new ValueDispatcher<>(AgentState.Initializing);

    public AdrianAgent(List<IController> controllers, IAgentConfiguration configuration) {
        this.controllers = controllers;
        this.configuration = configuration;
    }

    public void start() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        this.agentState.setCurrent(AgentState.Ready);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        this.agentState.setCurrent(AgentState.Idle);
    }

    @Override
    public SubscribableValueEvent<AgentState> onStateChange() {
        return this.agentState.subscribable;
    }

    public IAgentConfiguration getConfiguration() {
        return this.configuration;
    }
}
