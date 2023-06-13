package tech.jorn.adrian.core.risks;

import tech.jorn.adrian.core.risks.graph.RiskEdge;
import tech.jorn.adrian.core.risks.graph.RiskNode;

import java.util.List;

public interface IDamageProbabilityCalculator {
    float calculate(List<RiskNode> riskPath);
}
