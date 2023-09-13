package tech.jorn.adrian.core.risks;

import tech.jorn.adrian.core.graphs.base.INode;
import tech.jorn.adrian.core.graphs.risks.AttackGraph;

import java.util.List;
import java.util.stream.Collectors;


public record RiskReport(AttackGraph graph, List<INode> path, float probability, float damageValue, float damage) {

    public String toString() {
        var path = this.path.stream().map(INode::getID).collect(Collectors.joining("->"));
        return String.format("%s %.2f %.2f %.2f", path, this.probability, this.damageValue, this.damage);
    }
}
