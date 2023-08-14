package tech.jorn.adrian.agent;

import tech.jorn.adrian.core.agents.AgentState;
import tech.jorn.adrian.core.agents.IAgent;
import tech.jorn.adrian.core.agents.IAgentConfiguration;
import tech.jorn.adrian.core.controllers.IController;
import tech.jorn.adrian.core.observables.SubscribableValueEvent;

import java.util.List;

public class AdrianAgent implements IAgent {

    protected final List<IController> controllers;
    private final IAgentConfiguration configuration;

    public AdrianAgent(List<IController> controllers, IAgentConfiguration configuration) {
        this.controllers = controllers;
        this.configuration = configuration;
    }

    @Override
    public SubscribableValueEvent<AgentState> onStateChange() {
        return null;
    }

    public IAgentConfiguration getConfiguration() {
        return this.configuration;
    }
}
