package tech.jorn.adrian.experiment.instruments;

import org.apache.logging.log4j.LogManager;
import tech.jorn.adrian.core.agents.IAgentConfiguration;
import tech.jorn.adrian.core.events.EventManager;
import tech.jorn.adrian.core.events.queue.IEventQueue;

public class ExperimentalEventManager extends EventManager {

    public ExperimentalEventManager(IEventQueue eventQueue, IAgentConfiguration agentConfiguration) {
        super(eventQueue);
        this.log = LogManager.getLogger("[" + agentConfiguration.getNodeID() + "] EventManager");
    }


}
