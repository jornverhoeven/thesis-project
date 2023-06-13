package tech.jorn.adrian.core.risks;

import tech.jorn.adrian.core.knowledge.IKnowledgeBase;
import tech.jorn.adrian.core.risks.graph.AttackGraph;
import tech.jorn.adrian.core.risks.graph.RiskEdge;

import java.util.List;

public abstract class RiskRule {
    // Or maybe use the IKnowledgeBase instead of the IGraph
    public abstract List<RiskEdge> evaluate(IKnowledgeBase knowledgeBase, AttackGraph attackGraph);
}
