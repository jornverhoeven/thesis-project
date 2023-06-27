package tech.jorn.adrian.core.risks.detection;

import tech.jorn.adrian.core.knowledge.IKnowledgeBase;
import tech.jorn.adrian.core.risks.graph.AttackGraph;

public interface IRiskDetection {
    AttackGraph calculateAttackGraph(IKnowledgeBase knowledgeBase);

    AttackGraph mergeAttackGraph(AttackGraph attackGraph, RiskReport riskReport);

}
