package tech.jorn.adrian.agent;

import tech.jorn.adrian.agent.controllers.AuctionController;
import tech.jorn.adrian.agent.controllers.KnowledgeController;
import tech.jorn.adrian.agent.controllers.RiskController;
import tech.jorn.adrian.agent.services.BasicRiskDetection;
import tech.jorn.adrian.core.agents.IAgentConfiguration;
import tech.jorn.adrian.core.controllers.IController;
import tech.jorn.adrian.core.events.Event;
import tech.jorn.adrian.core.events.EventManager;
import tech.jorn.adrian.core.events.queue.InMemoryQueue;
import tech.jorn.adrian.core.graphs.base.INode;
import tech.jorn.adrian.core.graphs.knowledgebase.KnowledgeBase;
import tech.jorn.adrian.core.messages.EventMessage;
import tech.jorn.adrian.core.observables.EventDispatcher;
import tech.jorn.adrian.core.observables.SubscribableEvent;
import tech.jorn.adrian.core.services.AuctionManager;
import tech.jorn.adrian.core.messages.Message;
import tech.jorn.adrian.core.messages.MessageBroker;
import tech.jorn.adrian.core.services.RiskDetection;

import java.util.Arrays;
import java.util.List;

public class AgentRunner {
    public static void main(String[] args) {
        var configuration = (IAgentConfiguration) null;
        var queue = new InMemoryQueue();
        var eventManager = new EventManager(queue);
        var messageBroker = new InMemoryBroker();

        var riskDetection = new BasicRiskDetection(List.of());
        var knowledgeBase = new KnowledgeBase();

        List<IController> controllers = Arrays.asList(
                new RiskController(riskDetection, knowledgeBase, eventManager),
                new AuctionController(new AuctionManager(messageBroker), eventManager),
                new KnowledgeController(messageBroker, eventManager)
        );
        var agent = new AdrianAgent(controllers, configuration);

        messageBroker.onNewMessage().subscribe(message -> {
            if (message instanceof EventMessage<?> m) eventManager.emit(m.getEvent());
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
    public SubscribableEvent<Message> onNewMessage() {
        return null;
    }
}