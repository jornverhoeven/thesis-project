package tech.jorn.adrian.experiment.features;

import tech.jorn.adrian.agent.AdrianAgent;
import tech.jorn.adrian.agent.AgentConfiguration;
import tech.jorn.adrian.agent.controllers.AuctionController;
import tech.jorn.adrian.agent.controllers.KnowledgeController;
import tech.jorn.adrian.agent.controllers.ProposalController;
import tech.jorn.adrian.agent.controllers.RiskController;
import tech.jorn.adrian.agent.events.ShareKnowledgeEvent;
import tech.jorn.adrian.core.agents.AgentState;
import tech.jorn.adrian.core.agents.IAgent;
import tech.jorn.adrian.core.controllers.IController;
import tech.jorn.adrian.core.events.queue.InMemoryQueue;
import tech.jorn.adrian.core.graphs.infrastructure.Infrastructure;
import tech.jorn.adrian.core.graphs.infrastructure.InfrastructureNode;
import tech.jorn.adrian.core.graphs.knowledgebase.KnowledgeBase;
import tech.jorn.adrian.core.messages.EventMessage;
import tech.jorn.adrian.core.observables.EventDispatcher;
import tech.jorn.adrian.core.services.AuctionManager;
import tech.jorn.adrian.core.services.probability.ProductRiskProbability;
import tech.jorn.adrian.core.services.proposals.LowestDamage;
import tech.jorn.adrian.core.services.proposals.ProposalManager;
import tech.jorn.adrian.core.services.risks.HighestRisk;
import tech.jorn.adrian.experiment.instruments.ExperimentalAgent;
import tech.jorn.adrian.experiment.instruments.ExperimentalEventManager;
import tech.jorn.adrian.experiment.instruments.ExperimentalRiskDetection;
import tech.jorn.adrian.experiment.messages.Envelope;
import tech.jorn.adrian.experiment.messages.InMemoryBroker;
import tech.jorn.adrian.risks.RiskLoader;

import java.util.List;

public class FullFeatureSet extends FeatureSet {

    private final EventDispatcher<Envelope> messageDispatcher;

    public FullFeatureSet(EventDispatcher<Envelope> messageDispatcher) {
        this.messageDispatcher = messageDispatcher;
    }

    @Override
    IAgent getAgent(Infrastructure infrastructure, InfrastructureNode node) {
        var neighbours = this.getNeighboursFromInfrastructure(infrastructure, node);
        var assets = this.getAssetsFromInfrastructure(infrastructure, node);
        var configuration = new AgentConfiguration(node, neighbours, assets);

        var messageQueue = new InMemoryQueue();
        var knowledgeBase = new KnowledgeBase();

        var probabilityCalculator = new ProductRiskProbability();

        // Services
        var eventManager = new ExperimentalEventManager(messageQueue, configuration);
        var riskDetection = new ExperimentalRiskDetection(RiskLoader.listRisks(), probabilityCalculator, configuration);
        var proposalManager = new ProposalManager(knowledgeBase, riskDetection, new LowestDamage(100.0f), configuration);

        var messageBroker = new InMemoryBroker(node, neighbours, this.messageDispatcher);
        var auctionManager = new AuctionManager(messageBroker, eventManager, new LowestDamage(100.0f), configuration);

        List<IController> controllers = List.of(
                new KnowledgeController(knowledgeBase, messageBroker, eventManager, configuration),
                new RiskController(riskDetection, knowledgeBase, proposalManager, eventManager, new HighestRisk(1.0f), new LowestDamage(0.1f)),
                new ProposalController(proposalManager, eventManager),
                new AuctionController(auctionManager, eventManager, configuration)
        );

        var agent = new ExperimentalAgent(messageBroker, eventManager, controllers, configuration);

        this.learnFromNeighbours(infrastructure, node, configuration, knowledgeBase);

        messageBroker.onMessage(message -> {
            if (message instanceof EventMessage<?> m) eventManager.emit(m.getEvent());
        });
        agent.onStateChange().subscribe(state -> {
            // Once an agent is ready to send/receive events, we will send neighbours our information
            if (state != AgentState.Ready) return;
            System.out.println(configuration.getNodeID());
            var event = new ShareKnowledgeEvent(
                    configuration.getParentNode(),
                    configuration.getNeighbours(),
                    configuration.getAssets(),
                    1
            );
            messageBroker.broadcast(new EventMessage<>(event));
        });

        return agent;
    }
}
