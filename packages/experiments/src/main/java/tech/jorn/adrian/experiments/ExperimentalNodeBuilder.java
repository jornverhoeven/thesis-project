package tech.jorn.adrian.experiments;

import tech.jorn.adrian.agent.AdrianAgent;
import tech.jorn.adrian.core.utils.IDFactory;

public class ExperimentalNodeBuilder {
    private AdrianAgent agent;
    private IDFactory idFactory;

    public ExperimentalNodeBuilder withAgent(AdrianAgent agent) {
        this.agent = agent;
        return this;
    }
    public ExperimentalNodeBuilder withIDFactory(IDFactory factory) {
        this.idFactory = factory;
        return this;
    }

    public ExperimentalNode build() {
        String id = this.idFactory.getID();
        return new ExperimentalNode(id)
                .withAgent(this.agent);
    }

}
