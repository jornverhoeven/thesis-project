package tech.jorn.adrian.core.risks;

import tech.jorn.adrian.core.graphs.base.INode;
import tech.jorn.adrian.core.graphs.risks.AttackGraph;

import java.util.List;


public record RiskReport(AttackGraph graph, List<INode> path, float probability, float damageValue, float damage) {
}
