package tech.jorn.adrian.agent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.jorn.adrian.core.agents.AgentState;
import tech.jorn.adrian.core.agents.IAgent;
import tech.jorn.adrian.core.agents.IAgentConfiguration;
import tech.jorn.adrian.core.controllers.IController;
import tech.jorn.adrian.core.observables.SubscribableValueEvent;
import tech.jorn.adrian.core.observables.ValueDispatcher;

import java.util.List;

public class AdrianAgent implements IAgent {
    private Logger log;

    protected final List<IController> controllers;
    private final IAgentConfiguration configuration;
    private final ValueDispatcher<AgentState> agentState;

    public AdrianAgent(List<IController> controllers, IAgentConfiguration configuration, ValueDispatcher<AgentState> agentState) {
        this.controllers = controllers;
        this.configuration = configuration;
        this.agentState = agentState;

        this.log = LogManager.getLogger(String.format("[%s] %s", configuration.getNodeID(), "AdrianAgent"));

        this.agentState.subscribe(state -> {
            this.log.debug("Agent state changed to {}", state);
        });
    }

    public void start() {
        try {
            Thread.sleep(100 + ((int) (Math.random() * 100)));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        this.agentState.setCurrent(AgentState.Ready);
        try {
            Thread.sleep(500 + ((int)(Math.random() * 10) * 250));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        this.agentState.setCurrent(AgentState.Idle);
    }

    public void stop() {
        this.agentState.setCurrent(AgentState.Shutdown);
    }

    @Override
    public SubscribableValueEvent<AgentState> onStateChange() {
        return this.agentState.subscribable;
    }

    @Override
    public AgentState getState() {
        return this.agentState.current();
    }

    public IAgentConfiguration getConfiguration() {
        return this.configuration;
    }
}
