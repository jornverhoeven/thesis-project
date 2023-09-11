package tech.jorn.adrian.agent;

import tech.jorn.adrian.agent.controllers.AuctionController;
import tech.jorn.adrian.agent.controllers.KnowledgeController;
import tech.jorn.adrian.agent.controllers.ProposalController;
import tech.jorn.adrian.agent.controllers.RiskController;
import tech.jorn.adrian.agent.events.ShareKnowledgeEvent;
import tech.jorn.adrian.agent.services.BasicRiskDetection;
import tech.jorn.adrian.core.agents.AgentState;
import tech.jorn.adrian.core.agents.IAgentConfiguration;
import tech.jorn.adrian.core.controllers.IController;
import tech.jorn.adrian.core.events.Event;
import tech.jorn.adrian.core.events.EventManager;
import tech.jorn.adrian.core.events.queue.InMemoryQueue;
import tech.jorn.adrian.core.graphs.base.INode;
import tech.jorn.adrian.core.graphs.knowledgebase.KnowledgeBase;
import tech.jorn.adrian.core.graphs.knowledgebase.KnowledgeBaseNode;
import tech.jorn.adrian.core.graphs.knowledgebase.KnowledgeOrigin;
import tech.jorn.adrian.core.messages.EventMessage;
import tech.jorn.adrian.core.observables.EventDispatcher;
import tech.jorn.adrian.core.observables.SubscribableEvent;
import tech.jorn.adrian.core.services.AuctionManager;
import tech.jorn.adrian.core.messages.Message;
import tech.jorn.adrian.core.messages.MessageBroker;
import tech.jorn.adrian.core.services.proposals.ProposalManager;
import tech.jorn.adrian.core.services.probability.ProductRiskProbability;
import tech.jorn.adrian.core.services.proposals.LowestDamage;

import java.util.Arrays;
import java.util.List;

public class AgentRunner {
    public static void main(String[] args) {
        var configuration = (IAgentConfiguration) null;
        var queue = new InMemoryQueue();
        var eventManager = new EventManager(queue);
        var messageBroker = new InMemoryBroker();

        var knowledgeBase = new KnowledgeBase();
        var riskDetection = new BasicRiskDetection(List.of(), new ProductRiskProbability());
        var proposalManager = new ProposalManager(knowledgeBase, riskDetection, new LowestDamage(2.0f), configuration);

        KnowledgeBaseNode parent = KnowledgeBaseNode.fromNode(configuration.getParentNode(), KnowledgeOrigin.DIRECT);
        knowledgeBase.upsertNode(parent);

        List<IController> controllers = Arrays.asList(
                new RiskController(riskDetection, knowledgeBase, proposalManager, eventManager),
                new AuctionController(new AuctionManager(messageBroker, eventManager, new LowestDamage(2.0f), configuration), eventManager, configuration),
                new KnowledgeController(knowledgeBase, messageBroker, eventManager, configuration),
                new ProposalController(proposalManager, eventManager)
        );
        var agent = new AdrianAgent(controllers, configuration);

        messageBroker.onNewMessage().subscribe(message -> {
            if (message instanceof EventMessage<?> m) eventManager.emit(m.getEvent());
        });
        agent.onStateChange().subscribe(state -> {
            if (state != AgentState.Ready) return;
            var event = new ShareKnowledgeEvent(
                    configuration.getParentNode(),
                    configuration.getNeighbours(),
                    configuration.getAssets(),
                    1
            );
            messageBroker.broadcast(new EventMessage<>(event));
        });
    }
}

class InMemoryBroker implements MessageBroker {
    private final EventDispatcher<Event> messageDispatcher = new EventDispatcher<>();

    public InMemoryBroker() {

    }

    @Override
    public void send(INode recipient, Message message) {

    }

    @Override
    public void broadcast(Message message) {

    }

    @Override
    public void addRecipient(INode recipient) {

    }

    @Override
    public SubscribableEvent<Message> onNewMessage() {
        return null;
    }
}