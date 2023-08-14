package tech.jorn.adrian.core.risks;

import tech.jorn.adrian.core.graphs.knowledgebase.KnowledgeBase;
import tech.jorn.adrian.core.graphs.risks.AttackGraph;

import java.util.List;

public abstract class RiskRule {
    public abstract List<RiskEdge> evaluate(KnowledgeBase knowledgeBase, AttackGraph attackGraph);
}

