package tech.jorn.adrian.agent;

import tech.jorn.adrian.core.agents.IAgentConfiguration;
import tech.jorn.adrian.core.graphs.infrastructure.InfrastructureNode;
import tech.jorn.adrian.core.observables.ValueDispatcher;

import java.util.List;

public class AgentConfiguration implements IAgentConfiguration {
    private final ValueDispatcher<InfrastructureNode> parent;
    private final ValueDispatcher<List<String>> neighbours;


    public AgentConfiguration(InfrastructureNode parent, List<String> neighbours) {
        this.parent = new ValueDispatcher<>(parent);
        this.neighbours = new ValueDispatcher<>(neighbours);
    }

    @Override
    public InfrastructureNode getParentNode() {
        return this.parent.current();
    }

    @Override
    public List<String> getNeighbours() {
        return this.neighbours.current();
    }
}
