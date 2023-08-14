package tech.jorn.adrian.experiments;

import tech.jorn.adrian.agent.AdrianAgent;
import tech.jorn.adrian.agent.mitigation.MitigationManager;
import tech.jorn.adrian.core.agent.IAgentConfiguration;
import tech.jorn.adrian.core.messaging.IMessageBroker;
import tech.jorn.adrian.core.risks.detection.IRiskDetection;

public class ExperimentalAgent extends AdrianAgent {
    public ExperimentalAgent(IAgentConfiguration configuration, IMessageBroker messageBroker, IRiskDetection riskDetection, MitigationManager mitigationManager) {
        super(configuration, messageBroker, riskDetection, mitigationManager);
    }

    public IRiskDetection getRiskDetection() {
        return this.riskDetection;
    }
    public MitigationManager getMitigationManager() {
        return this.mitigationManager;
    }
}
