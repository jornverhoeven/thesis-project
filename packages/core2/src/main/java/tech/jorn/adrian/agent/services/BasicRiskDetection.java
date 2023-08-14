package tech.jorn.adrian.agent.services;

import tech.jorn.adrian.core.graphs.knowledgebase.KnowledgeBase;
import tech.jorn.adrian.core.graphs.knowledgebase.KnowledgeBaseNode;
import tech.jorn.adrian.core.graphs.knowledgebase.KnowledgeBaseSoftwareAsset;
import tech.jorn.adrian.core.graphs.risks.AttackGraph;
import tech.jorn.adrian.core.graphs.risks.AttackGraphEntry;
import tech.jorn.adrian.core.graphs.risks.AttackGraphNode;
import tech.jorn.adrian.core.graphs.risks.AttackGraphSoftwareAsset;
import tech.jorn.adrian.core.risks.RiskReport;
import tech.jorn.adrian.core.risks.RiskRule;
import tech.jorn.adrian.core.services.RiskDetection;

import java.util.Arrays;
import java.util.List;

public class BasicRiskDetection implements RiskDetection {
    private final List<RiskRule> riskRules;

    public BasicRiskDetection(List<RiskRule> riskRules) {
        this.riskRules = riskRules;
    }

    @Override
    public AttackGraph createAttackGraph(KnowledgeBase knowledgeBase) {
        var attackGraph = new AttackGraph();

        knowledgeBase.getNodes().forEach(node -> {
            AttackGraphEntry<?> attackNode = null;
            if (node instanceof KnowledgeBaseNode) 
                attackNode = new AttackGraphNode(node.getID());
            else if (node instanceof KnowledgeBaseSoftwareAsset)
                attackNode = new AttackGraphSoftwareAsset(node.getID());
            
            node.getProperties().forEach(attackNode::setProperty);
            attackGraph.upsertNode(attackNode);
        });

        this.riskRules.stream()
                .flatMap(r -> r.evaluate(knowledgeBase, attackGraph).stream())
                .forEach(riskEdge -> {
                    // TODO: Upsert instead of adding
                    attackGraph.addEdge(riskEdge.to(), riskEdge.from());
                });

        return attackGraph;
    }

    @Override
    public List<RiskReport> identifyRisks(AttackGraph attackGraph) {
        return List.of();
    }
}
