package tech.jorn.adrian.core.risks.detection;

import tech.jorn.adrian.core.knowledge.IKnowledgeBase;
import tech.jorn.adrian.core.risks.RiskRule;
import tech.jorn.adrian.core.risks.graph.AttackGraph;
import tech.jorn.adrian.core.risks.graph.RiskHardwareNode;
import tech.jorn.adrian.core.risks.graph.RiskSoftwareNode;

import java.util.List;

public class BasicRiskDetector implements IRiskDetection {

    private final List<RiskRule> rules;

    public BasicRiskDetector(List<RiskRule> rules) {
        this.rules = rules;
    }

    @Override
    public AttackGraph calculateAttackGraph(IKnowledgeBase knowledgeBase) {
        var attackGraph = new AttackGraph();

        knowledgeBase.getNodes().forEach(n -> {
            var node = new RiskHardwareNode(n);
            attackGraph.addNode(node);
        });

        knowledgeBase.getAssets().forEach(a -> {
            var asset = new RiskSoftwareNode(a);
            // TODO: Set parent node for asset: a.getParent()
            attackGraph.addNode(asset);
        });

        this.rules.stream()
                .flatMap(r -> r.evaluate(knowledgeBase, attackGraph).stream())
                .forEach(riskEdge -> {
                    // TODO: Upsert instead of adding
                    attackGraph.addEdge(riskEdge);
                });

        return attackGraph;
    }
}
