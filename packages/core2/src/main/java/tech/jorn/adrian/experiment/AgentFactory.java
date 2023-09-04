package tech.jorn.adrian.experiment;

import tech.jorn.adrian.agent.AgentConfiguration;
import tech.jorn.adrian.agent.controllers.AuctionController;
import tech.jorn.adrian.agent.controllers.KnowledgeController;
import tech.jorn.adrian.agent.controllers.ProposalController;
import tech.jorn.adrian.agent.controllers.RiskController;
import tech.jorn.adrian.core.controllers.IController;
import tech.jorn.adrian.core.events.queue.InMemoryQueue;
import tech.jorn.adrian.core.graphs.AbstractDetailedNode;
import tech.jorn.adrian.core.graphs.base.AbstractNode;
import tech.jorn.adrian.core.graphs.infrastructure.Infrastructure;
import tech.jorn.adrian.core.graphs.infrastructure.InfrastructureNode;
import tech.jorn.adrian.core.graphs.infrastructure.SoftwareAsset;
import tech.jorn.adrian.core.graphs.knowledgebase.*;
import tech.jorn.adrian.core.messages.EventMessage;
import tech.jorn.adrian.core.observables.EventDispatcher;
import tech.jorn.adrian.core.properties.SoftwareProperty;
import tech.jorn.adrian.core.services.AuctionManager;
import tech.jorn.adrian.core.services.proposals.ProposalManager;
import tech.jorn.adrian.core.services.probability.ProductRiskProbability;
import tech.jorn.adrian.core.services.proposals.LowestDamage;
import tech.jorn.adrian.experiment.instruments.ExperimentalAgent;
import tech.jorn.adrian.experiment.instruments.ExperimentalEventManager;
import tech.jorn.adrian.experiment.instruments.ExperimentalRiskDetection;
import tech.jorn.adrian.experiment.messages.Envelope;
import tech.jorn.adrian.experiment.messages.InMemoryBroker;
import tech.jorn.adrian.risks.RiskLoader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AgentFactory {

    public static List<ExperimentalAgent> fromInfrastructure(Infrastructure infrastructure, EventDispatcher<Envelope> messageDispatcher) {
        var nodes = infrastructure.listNodes();
        var agents = new ArrayList<ExperimentalAgent>();

        nodes.forEach(node -> {
            if (!(boolean) node.getProperty("hasAgent").orElse(false)) return;

            var neighbours = infrastructure.getNeighbours(node).stream()
                    .filter(n -> n instanceof InfrastructureNode)
                    .map(AbstractNode::getID)
                    .toList();
            var assets = infrastructure.getNeighbours(node).stream()
                    .filter(n -> n instanceof SoftwareAsset)
                    .map(n -> (SoftwareAsset) n)
                    .toList();
            var configuration = new AgentConfiguration(node, neighbours, assets);

            var queue = new InMemoryQueue();
            var eventManager = new ExperimentalEventManager(queue, configuration);
            var messageBroker = new InMemoryBroker(node, neighbours, messageDispatcher);
            var knowledgeBase = new KnowledgeBase();
            var riskDetection = new ExperimentalRiskDetection(RiskLoader.listRisks(), new ProductRiskProbability());
            var proposalManager = new ProposalManager(knowledgeBase, riskDetection, new LowestDamage(100.0f), configuration);

            KnowledgeBaseNode parent = KnowledgeBaseNode.fromNode(configuration.getParentNode(), KnowledgeOrigin.DIRECT);
            knowledgeBase.upsertNode(parent);

            infrastructure.getNeighbours(node).forEach(n -> {
                KnowledgeBaseEntry<?> knowledgeNode = null;
                if (n instanceof SoftwareAsset) knowledgeNode = KnowledgeBaseSoftwareAsset.fromNode((AbstractDetailedNode<SoftwareProperty<?>>) n);
//                if (n instanceof InfrastructureNode) knowledgeNode = new KnowledgeBaseNode(n.getID())
//                        .setKnowledgeOrigin(KnowledgeOrigin.INFERRED);
                if (knowledgeNode != null) {
                    knowledgeBase.upsertNode(knowledgeNode);
                    knowledgeBase.addEdge(parent, knowledgeNode);
                    knowledgeBase.addEdge(knowledgeNode, parent);
                }
            });

            List<IController> controllers = Arrays.asList(
                    new RiskController(riskDetection, knowledgeBase, proposalManager, eventManager),
                    new AuctionController(new AuctionManager(messageBroker, eventManager, new LowestDamage(100.0f), configuration), eventManager, configuration),
                    new KnowledgeController(knowledgeBase, messageBroker, eventManager, configuration),
                    new ProposalController(proposalManager, eventManager)
            );
            var agent = new ExperimentalAgent(messageBroker, eventManager, controllers, configuration);

            riskDetection.setAgent(agent);

            messageBroker.onNewMessage().subscribe(message -> {
                if (message instanceof EventMessage<?> m) eventManager.emit(m.getEvent());
            });

            agents.add(agent);
        });
        return agents;
    }
}
