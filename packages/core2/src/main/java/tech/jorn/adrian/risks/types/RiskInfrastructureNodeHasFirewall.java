package tech.jorn.adrian.risks.types;

import tech.jorn.adrian.core.risks.RiskType;

public class RiskInfrastructureNodeHasFirewall extends RiskType {
    public static final float unmitigatedRiskFactor = 0.8f;
    public static final float mitigatedRiskFactor = 0.2f;

    public RiskInfrastructureNodeHasFirewall() {
        super("Infrastructure node has firewall", 0.8f, 0.2f);
    }
}
