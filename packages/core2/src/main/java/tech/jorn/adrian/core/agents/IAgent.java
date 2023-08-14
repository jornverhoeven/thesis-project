package tech.jorn.adrian.core.agents;

import tech.jorn.adrian.core.observables.SubscribableValueEvent;

public interface IAgent {
    SubscribableValueEvent<AgentState> onStateChange();
}
