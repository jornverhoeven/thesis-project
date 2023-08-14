package tech.jorn.adrian.core.agents;

import tech.jorn.adrian.core.graphs.base.INode;
import tech.jorn.adrian.core.graphs.infrastructure.InfrastructureNode;

import java.util.List;

public interface IAgentConfiguration {
    InfrastructureNode getParentNode();

    List<String> getNeighbours();

    default String getNodeID() {
        return this.getParentNode().getID();
    }

}
