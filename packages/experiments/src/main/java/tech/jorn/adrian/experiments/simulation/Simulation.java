package tech.jorn.adrian.experiments.simulation;

import tech.jorn.adrian.agent.AdrianAgent;
import tech.jorn.adrian.core.agent.IAgent;
import tech.jorn.adrian.core.infrastructure.Infrastructure;
import tech.jorn.adrian.experiments.ExperimentalAgent;
import tech.jorn.adrian.experiments.utils.MessageQueue;

import java.util.List;

public class Simulation {

    private final Infrastructure infrastructure;
    private final List<ExperimentalAgent> agents;
    private final MessageQueue messageQueue;

    public Simulation(Infrastructure infrastructure, List<ExperimentalAgent> agents, MessageQueue messageQueue) {
        this.infrastructure = infrastructure;
        this.agents = agents;
        this.messageQueue = messageQueue;
    }

    public Infrastructure getInfrastructure() {
        return this.infrastructure;
    }

    public List<ExperimentalAgent> getAgents() {
        return agents;
    }

    public MessageQueue getMessageQueue() {
        return messageQueue;
    }
}
