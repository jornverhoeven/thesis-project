package tech.jorn.adrian.core.graphs.risks;

import tech.jorn.adrian.core.graphs.base.*;
import tech.jorn.adrian.core.graphs.base.WeightedLink;
import tech.jorn.adrian.core.risks.Risk;
import tech.jorn.adrian.core.risks.RiskEdge;
import tech.jorn.adrian.core.risks.RiskReport;
import tech.jorn.adrian.core.services.probability.IRiskProbabilityCalculator;
import tech.jorn.adrian.core.services.probability.ProductRiskProbability;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AttackGraph extends AbstractGraph<AttackGraphEntry<?>, AttackGraphLink<AttackGraphEntry<?>>> {

    public AttackGraph() {
        super();
        this.upsertNode(VoidNode.getIncoming());
    }

    public void addEdge(AttackGraphEntry<?> from, AttackGraphEntry<?> to, Risk risk) {
        var adj = this.adjacent.getOrDefault(from, new ArrayList<>());
        adj.add(new AttackGraphLink<>(to, risk));
        this.adjacent.put(from, adj);
    }

    public void addIncoming(AttackGraphEntry<?> to, Risk risk) {
        this.addEdge(VoidNode.getIncoming(), to, risk);
    }

    public List<AttackGraphLink<AttackGraphEntry<?>>> getIncoming() {
        return this.adjacent.get(VoidNode.getIncoming());
    }

    public AttackGraph getGraphForPath(List<AttackGraphEntry<?>> path) {
        AttackGraph result = new AttackGraph();

        var startNode = path.get(0);
        result.upsertNode(startNode);
        this.getIncoming().stream()
                .filter(l -> l.getNode().equals(startNode))
                .forEach(l -> result.addIncoming(startNode, l.getRisk()));

        for (int i=0; i<path.size()-1; i++) {
            var node = path.get(i);
            var next = path.get(i+1);

            var edges = this.adjacent.get(node).stream()
                    .filter(link -> link.getNode().equals(next))
                    .toList();
            result.upsertNode(node);
            result.upsertNode(next);
            edges.forEach(edge -> result.addEdge(node, next, edge.getRisk()));;
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
                    .map(link -> link.getRisk().factor())
                    .toList();
            var edgeProbability = probabilityCalculator.calculate(probabilities);
            result.add(edgeProbability);
        }
        return result.stream()
                .reduce(1.0f, (acc, cur) -> acc * cur);
    }

    public List<AttackGraphLink<AttackGraphEntry<?>>> getNeighboursWithRisks(AttackGraphEntry<?> node) {
        return this.adjacent.get(node);
    }
}

