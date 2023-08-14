package tech.jorn.adrian.core.agent;

import tech.jorn.adrian.core.observables.SubscribableValueEvent;
import tech.jorn.adrian.core.risks.detection.RiskReport;

import java.util.Optional;

public interface IAgent {

    SubscribableValueEvent<IAgentConfiguration> onConfigurationChange();
    SubscribableValueEvent<AgentState> onStateChange();

    IAgentConfiguration getConfiguration();
    AgentState getState();

    void shareKnowledge();
    Optional<RiskReport> detectRisks();


}
