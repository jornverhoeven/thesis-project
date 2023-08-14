package tech.jorn.adrian.core.risks.graph;

import tech.jorn.adrian.core.IIdentifiable;
import tech.jorn.adrian.core.graph.AbstractGraph;
import tech.jorn.adrian.core.risks.IDamageProbabilityCalculator;
import tech.jorn.adrian.core.risks.detection.RiskReport;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class AttackGraph extends AbstractGraph<RiskNode, RiskEdge> {

    public AttackGraph() {
    }

    public Optional<RiskHardwareNode> findNode(IIdentifiable node) {
        return this.findByID(node.getID());
    }

    public Optional<RiskSoftwareNode> findAsset(IIdentifiable asset) {
        return this.findByID(asset.getID());
    }

    public List<RiskSoftwareNode> listAssets() {
        return this.nodes.current().stream()
                .filter(n -> n instanceof RiskSoftwareNode)
                .map(n -> (RiskSoftwareNode) n)
                .toList();
    }

    private <N extends RiskNode> Optional<N> findByID(String id) {
        return (Optional<N>) this.nodes.current().stream()
                .filter(n -> n.getID().equals(id))
                .findFirst();
    }

    public void addNode(RiskNode node) {
        var nodes = this.nodes.current();
        if (this.findByID(node.getID()).isEmpty()) nodes.add(node);
        else {
        } // maybe do something here
        this.nodes.setCurrent(nodes);
    }

    public void addEdge(RiskEdge edge) {
        var edges = this.edges.current();
        // TODO: Do something with existing edges
        edges.add(edge);
        this.edges.setCurrent(edges);
    }

    public String pathRepresentation(List<RiskNode> path) {
        return path.stream()
                .map(p -> p.getID())
                .collect(Collectors.joining("->"));
    }
}
