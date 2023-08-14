package tech.jorn.adrian.experiments.simulation;

import org.yaml.snakeyaml.Yaml;
import tech.jorn.adrian.agent.AgentConfiguration;
import tech.jorn.adrian.agent.AgentConfigurationLoader;
import tech.jorn.adrian.core.agent.IAgent;
import tech.jorn.adrian.core.agent.IAgentConfiguration;
import tech.jorn.adrian.core.assets.SoftwareAssetFactory;
import tech.jorn.adrian.core.graph.INode;
import tech.jorn.adrian.core.infrastructure.Infrastructure;
import tech.jorn.adrian.core.infrastructure.Link;
import tech.jorn.adrian.core.messaging.IMessageBroker;
import tech.jorn.adrian.core.properties.NodeProperty;
import tech.jorn.adrian.experiments.ExperimentMessageBroker;
import tech.jorn.adrian.experiments.ExperimentalAgent;
import tech.jorn.adrian.experiments.ExperimentalNode;
import tech.jorn.adrian.experiments.utils.MessageQueue;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
public class SimulationGenerator {
    private BiFunction<List<INode>, MessageQueue, IMessageBroker> messageBrokerFactory;
    private Function<IAgentConfiguration, ExperimentalAgent> agentFactory;

    public SimulationGenerator() {
        messageBrokerFactory = ExperimentMessageBroker::new;
    }

    public SimulationGenerator withMessageBroker(BiFunction<List<INode>, MessageQueue, IMessageBroker> messageBrokerFactory) {
        this.messageBrokerFactory = messageBrokerFactory;
        return this;
    }

    public SimulationGenerator withAgentFactory(Function<IAgentConfiguration, ExperimentalAgent> agentFactory) {
        this.agentFactory = agentFactory;
        return this;
    }

    public Simulation fromFile(String filePath) {
        var yaml = new Yaml();
        InputStream inputStream = AgentConfigurationLoader.class
                .getClassLoader()
                .getResourceAsStream(filePath);
        Map<String, Object> content = yaml.load(inputStream);

        var messageQueue = new MessageQueue();
        var infrastructure = new Infrastructure();
        var agents = new ArrayList<ExperimentalAgent>();
        var nodes = (List<Map<String, Object>>) content.get("nodes");
        nodes.forEach(node -> {
            var iNode = new ExperimentalNode(
                    (String) node.get("id"),
                    (String) node.get("name"));
            var properties = new HashMap<>(node);
            properties.remove("id");
            properties.remove("name");
            properties.entrySet()
                    .forEach(entry -> iNode.getProperties().add(new NodeProperty<>(entry.getKey(), entry.getValue())));
            infrastructure.addNode(iNode);
        });

        var links = (List<Map<String, Object>>) content.get("links");
        links.forEach(link -> {
            var source = infrastructure.getNodes()
                    .stream()
                    .filter(n -> n.getID().equals(link.get("source")))
                    .findFirst();
            var target = infrastructure.getNodes()
                    .stream()
                    .filter(n -> n.getID().equals(link.get("target")))
                    .findFirst();
            if (source.isEmpty() || target.isEmpty())
                return;

            // TODO: Omnidirectional?
            infrastructure.addLink(new Link(source.get(), target.get()));
            infrastructure.addLink(new Link(target.get(), source.get()));
        });

        var assets = (List<Map<String, Object>>) content.get("assets");
        assets.forEach(asset -> {
            var host = infrastructure.getNodes()
                    .stream()
                    .filter(n -> n.getID().equals(asset.get("host")))
                    .findFirst();
            if (host.isEmpty())
                return;
            host.get().addSoftwareAsset(SoftwareAssetFactory.fromMap(asset));
        });

        // TODO: Implement connectors
        // TODO: Create Agents for Nodes
        // TODO: Use classloader to get all rules


        nodes.forEach(node -> {
            if (!(Boolean) node.get("hasAgent"))
                return;

            var parent = infrastructure.getNode((String) node.get("id"));
            if (parent.isEmpty())
                return;

            var neighbours = infrastructure.getNeighbours(parent.get())
                    .stream().map(n -> (INode) n)
                    .collect(Collectors.toList());
            var configuration = new AgentConfiguration(parent.get(), neighbours, 10000);
            var agent = this.agentFactory.apply(configuration);
            agents.add(agent);
        });

        return new Simulation(infrastructure, agents, messageQueue);
    }


}
