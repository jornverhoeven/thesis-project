package tech.jorn.adrian.experiment.scenarios;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.jorn.adrian.agent.AdrianAgent;
import tech.jorn.adrian.core.agents.IAgent;
import tech.jorn.adrian.core.graphs.infrastructure.Infrastructure;
import tech.jorn.adrian.core.graphs.infrastructure.InfrastructureNode;
import tech.jorn.adrian.core.observables.EventDispatcher;
import tech.jorn.adrian.experiment.instruments.ExperimentalAgent;
import tech.jorn.adrian.experiment.messages.Envelope;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class GrowingInfrastructureScenario extends Scenario {
    Logger log = LogManager.getLogger(GrowingInfrastructureScenario.class);

    private final Function<InfrastructureNode, IAgent> agentFactory;

    public GrowingInfrastructureScenario(Infrastructure infrastructure, EventDispatcher<Envelope> messageDispatcher, Function<InfrastructureNode, IAgent> agentFactory) {
        super(infrastructure, messageDispatcher, 10 * 60 * 1000);
        this.agentFactory = agentFactory;
    }

    @Override
    public void onScheduleEvents(List<ExperimentalAgent> agents) {
        var nodeList = new ArrayList<InfrastructureNode>();
        var neighbourList = new HashMap<String, List<String>>();

        {
            var node = new InfrastructureNode("node-z");
            node.setProperty("name", "Node Z");
            node.setProperty("hasFirewall", false);
            node.setProperty("hasAgent", true);
            node.setProperty("os-contiki-ng/contiki-ng-version", "3.2.1");
            nodeList.add(node);
            neighbourList.put(node.getID(), List.of("iot-controller"));
        }
        {
            var node = new InfrastructureNode("node-y");
            node.setProperty("name", "Node Y");
            node.setProperty("hasFirewall", false);
            node.setProperty("hasAgent", true);
            node.setProperty("os-contiki-ng/contiki-ng-version", "3.2.1");
            nodeList.add(node);
            neighbourList.put(node.getID(), List.of("iot-controller"));
        }
        {
            var node = new InfrastructureNode("node-x");
            node.setProperty("name", "Node X");
            node.setProperty("hasFirewall", false);
            node.setProperty("hasAgent", true);
            node.setProperty("os-contiki-ng/contiki-ng-version", "3.2.1");
            node.setProperty("sdk-google/cloud_iot_device_sdk_for_embedded_c-version", "1.0.1");
            nodeList.add(node);
            neighbourList.put(node.getID(), List.of("node-z", "node-y"));
        }
        {
            var node = new InfrastructureNode("node-w");
            node.setProperty("name", "Node W");
            node.setProperty("hasFirewall", false);
            node.setProperty("hasAgent", true);
            node.setProperty("os-contiki-ng/contiki-ng-version", "3.2.1");
            nodeList.add(node);
            neighbourList.put(node.getID(), List.of("router"));
        }
        {
            var node = new InfrastructureNode("node-v");
            node.setProperty("name", "Node V");
            node.setProperty("hasFirewall", false);
            node.setProperty("hasAgent", true);
            node.setProperty("os-contiki-ng/contiki-ng-version", "3.2.1");
            nodeList.add(node);
            neighbourList.put(node.getID(), List.of("router", "node-w", "node-x"));
        }

        for (var i=1; i <= nodeList.size(); i++) {
            var node = nodeList.get(i-1);
            this.after(i * 30 * 1000, () -> {
                this.log.info("Adding new node + agent {}", node.getID());

                infrastructure.upsertNode(node);

                var neighbours = neighbourList.getOrDefault(node.getID(), List.of());
                neighbours.forEach(id -> {
                    var neighbour = infrastructure.findById(id);
                    infrastructure.addEdge(node, neighbour.get());
                    infrastructure.addEdge(neighbour.get(), node);
                });

                var agent = this.agentFactory.apply(node);
                agent.start();

                this.newAgent.dispatch(agent);
            });
        }

        this.after(4 * 60 * 1000, () -> {
            this.log.debug("Waiting for silence");
            this.waitForSilence(10 * 1000, this.finished::raise);
        });
    }


}
