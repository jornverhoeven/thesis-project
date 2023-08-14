package tech.jorn.adrian.core.risks.detection;

import tech.jorn.adrian.core.knowledge.IKnowledgeBase;
import tech.jorn.adrian.core.observables.SubscribableEvent;
import tech.jorn.adrian.core.risks.graph.AttackGraph;

import java.util.List;
import java.util.Optional;

public interface IRiskDetection {
    AttackGraph calculateAttackGraph(IKnowledgeBase knowledgeBase);

    AttackGraph mergeAttackGraph(AttackGraph attackGraph, RiskReport riskReport);

    List<RiskReport> findRisks(AttackGraph attackGraph);
    Optional<RiskReport> selectRisk(AttackGraph attackGraph);

    SubscribableEvent<List<RiskReport>> onRisksFound();
    SubscribableEvent<RiskReport> onRiskSelected();
}
