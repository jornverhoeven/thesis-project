package tech.jorn.adrian.agent.events;

import tech.jorn.adrian.core.risks.RiskReport;

public class SelectedRiskEvent extends FoundRiskEvent {
    public SelectedRiskEvent(RiskReport risk) {
        super(risk);
    }
}
