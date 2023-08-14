package tech.jorn.adrian.experiment;

import tech.jorn.adrian.agent.AgentConfiguration;
import tech.jorn.adrian.agent.controllers.AuctionController;
import tech.jorn.adrian.agent.controllers.KnowledgeController;
import tech.jorn.adrian.agent.controllers.ProposalController;
import tech.jorn.adrian.agent.controllers.RiskController;
import tech.jorn.adrian.agent.services.BasicRiskDetection;
import tech.jorn.adrian.core.controllers.IController;
import tech.jorn.adrian.core.events.EventManager;
import tech.jorn.adrian.core.events.queue.InMemoryQueue;
import tech.jorn.adrian.core.graphs.base.AbstractNode;
import tech.jorn.adrian.core.graphs.infrastructure.Infrastructure;
import tech.jorn.adrian.core.graphs.infrastructure.InfrastructureNode;
import tech.jorn.adrian.core.graphs.infrastructure.SoftwareAsset;
import tech.jorn.adrian.core.graphs.knowledgebase.*;
import tech.jorn.adrian.core.messages.EventMessage;
import tech.jorn.adrian.core.observables.EventDispatcher;
import tech.jorn.adrian.core.risks.RiskRule;
import tech.jorn.adrian.core.services.AuctionManager;
import tech.jorn.adrian.experiment.messages.Envelope;
import tech.jorn.adrian.experiment.messages.InMemoryBroker;
import tech.jorn.adrian.risks.rules.RuleInfrastructureNodeHasFirewall;
import tech.jorn.adrian.risks.rules.RuleSoftwareComponentIsEncrypted;

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
            var configuration = new AgentConfiguration(node, neighbours);

            var queue = new InMemoryQueue();
            var eventManager = new EventManager(queue);
            var messageBroker = new InMemoryBroker(node, neighbours, messageDispatcher);
            var knowledgeBase = new KnowledgeBase();

            var knowledgeRoot = new KnowledgeBaseNode(node.getID());
            node.getProperties().forEach(knowledgeRoot::setProperty);
            knowledgeRoot.setKnowledgeOrigin(KnowledgeOrigin.DIRECT);
            knowledgeBase.upsertNode(knowledgeRoot);

            infrastructure.getNeighbours(node).forEach(n -> {
                KnowledgeBaseEntry<?> knowledgeNode = null;
                if (n instanceof SoftwareAsset) knowledgeNode = new KnowledgeBaseSoftwareAsset(n.getID())
                                .setKnowledgeOrigin(KnowledgeOrigin.DIRECT);
//                if (n instanceof InfrastructureNode) knowledgeNode = new KnowledgeBaseNode(n.getID())
//                        .setKnowledgeOrigin(KnowledgeOrigin.INFERRED);
                if (knowledgeNode != null) {
                    knowledgeBase.upsertNode(knowledgeNode);
                    knowledgeBase.addEdge(knowledgeRoot, knowledgeNode);
                    knowledgeBase.addEdge(knowledgeNode, knowledgeRoot);
                }
            });

            List<RiskRule> riskRules = List.of(
                    new RuleInfrastructureNodeHasFirewall(),
                    new RuleSoftwareComponentIsEncrypted()
            );

            List<IController> controllers = Arrays.asList(
                    new RiskController(new BasicRiskDetection(riskRules), knowledgeBase, eventManager),
//                    new AuctionController(new AuctionManager(messageBroker), eventManager),
                    new KnowledgeController(messageBroker, eventManager),
                    new ProposalController(eventManager)
            );
            var agent = new ExperimentalAgent(messageBroker, eventManager, controllers, configuration);

            messageBroker.onNewMessage().subscribe(message -> {
                if (message instanceof EventMessage<?> m) eventManager.emit(m.getEvent());
            });

            agents.add(agent);
        });
        return agents;
    }
}
