package tech.jorn.adrian.risks.rules;

import tech.jorn.adrian.core.knowledge.IKnowledgeBase;
import tech.jorn.adrian.core.risks.Risk;
import tech.jorn.adrian.core.risks.RiskRule;
import tech.jorn.adrian.core.risks.graph.AttackGraph;
import tech.jorn.adrian.core.risks.graph.RiskEdge;
import tech.jorn.adrian.risks.types.RiskInfrastructureNodeHasFirewall;
import tech.jorn.adrian.risks.types.RiskSoftwareComponentIsEncrypted;

import java.util.ArrayList;
import java.util.List;

public class RuleSoftwareComponentIsEncrypted extends PropertyBasedRule<RiskSoftwareComponentIsEncrypted> {
    public RuleSoftwareComponentIsEncrypted() {
        super("isEncrypted", RiskSoftwareComponentIsEncrypted.class);
    }
}
