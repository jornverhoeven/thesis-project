package tech.jorn.adrian.risks.rules;

import tech.jorn.adrian.core.graph.IEdge;
import tech.jorn.adrian.core.graph.IGraph;
import tech.jorn.adrian.core.graph.INode;
import tech.jorn.adrian.core.knowledge.IKnowledgeBase;
import tech.jorn.adrian.core.risks.Risk;
import tech.jorn.adrian.core.risks.RiskRule;
import tech.jorn.adrian.core.risks.graph.AttackGraph;
import tech.jorn.adrian.core.risks.graph.RiskEdge;
import tech.jorn.adrian.risks.types.RiskInfrastructureNodeHasFirewall;

import java.util.ArrayList;
import java.util.List;

public class RuleInfrastructureNodeHasFirewall extends PropertyBasedRule<RiskInfrastructureNodeHasFirewall> {
    public RuleInfrastructureNodeHasFirewall() {
        super("hasFirewall", RiskInfrastructureNodeHasFirewall.class);
    }
}
