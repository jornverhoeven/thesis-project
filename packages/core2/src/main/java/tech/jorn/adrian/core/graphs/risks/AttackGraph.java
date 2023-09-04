package tech.jorn.adrian.core.graphs.risks;

import tech.jorn.adrian.core.graphs.base.AbstractWeightedGraph;
import tech.jorn.adrian.core.graphs.base.WeightedLink;
import tech.jorn.adrian.core.risks.RiskEdge;
import tech.jorn.adrian.core.risks.RiskReport;
import tech.jorn.adrian.core.services.probability.IRiskProbabilityCalculator;
import tech.jorn.adrian.core.services.probability.ProductRiskProbability;

import java.util.ArrayList;
import java.util.List;

public class AttackGraph extends AbstractWeightedGraph<AttackGraphEntry<?>> {

    public AttackGraph mergeRiskReport(RiskReport report) {
        for (int i=0; i<report.path().size()-1; i++) {
            var node = report.path().get(i);
            var next = report.path().get(i+1);

            var exists = this.findById(node.getID()).isPresent();
            if (exists) continue;

            var attackNode = new AttackGraphNode(node.getID());
            var attackNext = this.findById(next.getID())
                    .orElse(new AttackGraphNode(next.getID()));
            var probability = report.graph()
                    .getNeighboursWithWeights(attackNode)
                    .get(0)
                    .getWeight();

            this.upsertNode(attackNode);
            this.upsertNode(attackNext);
            this.addEdge(attackNode, attackNext, probability);
        }
        return this;
    }

    public AttackGraph getGraphForPath(List<AttackGraphEntry<?>> path, IRiskProbabilityCalculator probabilityCalculator) {
        AttackGraph result = new AttackGraph();
        for (int i=0; i<path.size()-1; i++) {
            var node = path.get(i);
            var next = path.get(i+1);

            var probabilities = this.adjacent.get(node).stream()
                    .filter(link -> link.getNode().equals(next))
                    .map(WeightedLink::getWeight)
                    .toList();
            var edgeProbability = probabilityCalculator.calculate(probabilities);
            result.upsertNode(node);
            result.upsertNode(next);
            result.addEdge(node, next, edgeProbability);
        }
        return result;
    }

    public float getProbabilityForPath(List<AttackGraphEntry<?>> path, IRiskProbabilityCalculator probabilityCalculator) {
        List<Float> result = new ArrayList<>();
        for (int i=0; i<path.size()-1; i++) {
            var node = path.get(i);
            var next = path.get(i+1);

            var probabilities = this.adjacent.get(node).stream()
                    .filter(link -> link.getNode().equals(next))
                    .map(WeightedLink::getWeight)
                    .toList();
            var edgeProbability = probabilityCalculator.calculate(probabilities);
            result.add(edgeProbability);
        }
        return result.stream()
                .reduce(1.0f, (acc, cur) -> acc * cur);
    }
}
