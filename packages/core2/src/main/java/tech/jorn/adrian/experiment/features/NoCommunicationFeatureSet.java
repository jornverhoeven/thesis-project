package tech.jorn.adrian.experiment.features;

import tech.jorn.adrian.agent.AgentConfiguration;
import tech.jorn.adrian.agent.controllers.KnowledgeController;
import tech.jorn.adrian.agent.controllers.ProposalController;
import tech.jorn.adrian.agent.controllers.RiskController;
import tech.jorn.adrian.core.agents.AgentState;
import tech.jorn.adrian.core.agents.IAgent;
import tech.jorn.adrian.core.controllers.IController;
import tech.jorn.adrian.core.events.queue.InMemoryQueue;
import tech.jorn.adrian.core.graphs.base.INode;
import tech.jorn.adrian.core.graphs.infrastructure.Infrastructure;
import tech.jorn.adrian.core.graphs.infrastructure.InfrastructureNode;
import tech.jorn.adrian.core.graphs.knowledgebase.KnowledgeBase;
import tech.jorn.adrian.core.messages.Message;
import tech.jorn.adrian.core.messages.MessageBroker;
import tech.jorn.adrian.core.observables.SubscribableEvent;
import tech.jorn.adrian.core.observables.ValueDispatcher;
import tech.jorn.adrian.core.services.probability.ProductRiskProbability;
import tech.jorn.adrian.core.services.proposals.LowestDamage;
import tech.jorn.adrian.core.services.proposals.ProposalManager;
import tech.jorn.adrian.core.services.risks.HighestRisk;
import tech.jorn.adrian.experiment.instruments.ProposalImplementationController;
import tech.jorn.adrian.experiment.instruments.ExperimentalAgent;
import tech.jorn.adrian.experiment.instruments.ExperimentalEventManager;
import tech.jorn.adrian.experiment.instruments.ExperimentalRiskDetection;
import tech.jorn.adrian.risks.RiskLoader;

import java.util.List;
import java.util.function.Consumer;

public class NoCommunicationFeatureSet extends FeatureSet {
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

        var messageBroker = this.getMessageBroker();

        var agentState = new ValueDispatcher<>(AgentState.Initializing);

        List<IController> controllers = List.of(
                new KnowledgeController(knowledgeBase, messageBroker, eventManager, configuration, agentState.subscribable),
                new RiskController(riskDetection, knowledgeBase, proposalManager, eventManager, new HighestRisk(1.0f), new LowestDamage(0.1f), agentState.subscribable),
                new ProposalController(proposalManager, eventManager, agentState.subscribable),
                new ProposalImplementationController(eventManager, configuration, agentState.subscribable)
        );

        var agent = new ExperimentalAgent(messageBroker, eventManager, riskDetection, knowledgeBase, controllers, configuration, agentState);

        this.learnFromNeighbours(infrastructure, node, configuration, knowledgeBase);

        return agent;
    }

    private MessageBroker getMessageBroker() {
        class VoidMessageBroker implements MessageBroker {
            @Override
            public void send(INode recipient, Message message) { }

            @Override
            public void broadcast(Message message) { }

            @Override
            public void addRecipient(INode recipient) { }

            @Override
            public void onMessage(Consumer<Message> messageHandler) { }
        }
        return new VoidMessageBroker();
    }
}
