package tech.jorn.adrian.experiments;

import tech.jorn.adrian.agent.AdrianAgent;
import tech.jorn.adrian.core.infrastructure.Node;

public class ExperimentalNode extends Node {
    private AdrianAgent agent;

    public ExperimentalNode(String id, String name) {
        super(id, name);
    }

    public ExperimentalNode(String id) {
        super(id);
    }

    public ExperimentalNode withAgent(AdrianAgent agent) {
        this.agent = agent;
        return this;
    }
}
