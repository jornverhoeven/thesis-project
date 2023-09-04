package tech.jorn.adrian.risks;

import tech.jorn.adrian.core.risks.RiskRule;
import tech.jorn.adrian.risks.rules.*;

import java.util.List;

public class RiskLoader {
    public static List<RiskRule> listRisks() {
        return List.of(
                new RuleInfrastructureNodeHasFirewall(),
                new RuleInfrastructureNodeIsPhysicallySecured(),
                new RuleSoftwareComponentIsEncrypted()
        );
    }
}
