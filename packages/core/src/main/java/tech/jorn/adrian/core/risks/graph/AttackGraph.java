package tech.jorn.adrian.core.risks.graph;

import tech.jorn.adrian.core.IIdentifiable;
import tech.jorn.adrian.core.graph.AbstractGraph;
import tech.jorn.adrian.core.risks.IDamageProbabilityCalculator;
import tech.jorn.adrian.core.risks.detection.RiskReport;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AttackGraph extends AbstractGraph<RiskNode, RiskEdge> {
    private final IDamageProbabilityCalculator damageProbabilityCalculator;
    private final float damageThreshold;

    // TODO: Get the damageThreshold and calculator from the constructor
    public AttackGraph() {
        this.damageProbabilityCalculator = new IDamageProbabilityCalculator() {
            @Override
            public float calculate(List<RiskNode> riskPath) {
                return 0;
            }
        };
        this.damageThreshold = 0.f;
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

    // TODO: Move this to BasicRiskDetector
    public List<RiskReport> findRisks() {
        List<RiskSoftwareNode> criticalSoftwareList = this.nodes.current().stream()
                .filter(n -> n instanceof RiskSoftwareNode && (Boolean) n.getProperty("isCritical").orElse(false))
                .map(n -> (RiskSoftwareNode) n)
                .toList();

        List<RiskHardwareNode> exposedNodes = this.nodes.current().stream()
                .filter(n -> n instanceof RiskHardwareNode && (Boolean) n.getProperty("isExposed").orElse(false))
                .map(n -> (RiskHardwareNode) n)
                .toList();

        var riskReports = new ArrayList<RiskReport>();

        exposedNodes.forEach(hardwareNode -> {
            criticalSoftwareList.forEach(criticalSoftware -> {
                var riskPath = this.depthFirstSearch(hardwareNode, criticalSoftware);

                var damageProbability = this.damageProbabilityCalculator.calculate(riskPath);
                var damageValue = criticalSoftware.getProperty("damageValue")
                        .map(p -> ((Double) p).floatValue())
                        .orElse(0.0f);

                if (damageProbability * damageValue >= this.damageThreshold) {
                    var riskReport = new RiskReport(riskPath, this.pathRepresentation(riskPath), damageProbability, damageValue, damageProbability * damageValue);
                    riskReports.add(riskReport);
                }
            });
        });

        return riskReports;
    }

    private String pathRepresentation(List<RiskNode> path) {
        return "path-representation";
    }
}
