package tech.jorn.adrian.risks.rules;

import tech.jorn.adrian.risks.types.RiskInfrastructureNodeHasFirewall;

public class RuleInfrastructureNodeHasFirewall extends PropertyBasedRule<RiskInfrastructureNodeHasFirewall> {
    public RuleInfrastructureNodeHasFirewall() {
        super("hasFirewall", RiskInfrastructureNodeHasFirewall.class);
    }
}
