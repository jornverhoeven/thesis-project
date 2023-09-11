package tech.jorn.adrian.experiment.scenarios;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.jorn.adrian.agent.AdrianAgent;
import tech.jorn.adrian.core.graphs.infrastructure.Infrastructure;
import tech.jorn.adrian.core.graphs.infrastructure.InfrastructureNode;
import tech.jorn.adrian.core.observables.EventDispatcher;
import tech.jorn.adrian.experiment.messages.Envelope;

import java.util.function.Function;
import java.util.function.Supplier;

public class GrowingInfrastructureScenario extends Scenario {
    Logger log = LogManager.getLogger(GrowingInfrastructureScenario.class);

    private final Function<InfrastructureNode, AdrianAgent> agentFactory;

    public GrowingInfrastructureScenario(Infrastructure infrastructure, EventDispatcher<Envelope> messageDispatcher, Function<InfrastructureNode, AdrianAgent> agentFactory) {
        super(infrastructure, messageDispatcher, 4 * 60 * 1000);
        this.agentFactory = agentFactory;
    }

    @Override
    public void onScheduleEvents() {
        this.after(12 * 1000, () -> {
            var node = new InfrastructureNode("node-z");
            node.setProperty("name", "Node Z");
            node.setProperty("hasFirewall", false);
            node.setProperty("isExposed", true);
            node.setProperty("hasAgent", true);

            this.log.info("Adding new node + agent");
            infrastructure.upsertNode(node);
            var neighbour = infrastructure.findById("node-b").get();
            infrastructure.addEdge(node, neighbour);

            var agent = this.agentFactory.apply(node);
            agent.start();

            this.newAgent.dispatch(agent);
        });
    }


}
