package tech.jorn.adrian.core.risks.detection;

import tech.jorn.adrian.core.knowledge.IKnowledgeBase;
import tech.jorn.adrian.core.observables.EventDispatcher;
import tech.jorn.adrian.core.observables.SubscribableEvent;
import tech.jorn.adrian.core.risks.IDamageProbabilityCalculator;
import tech.jorn.adrian.core.risks.RiskRule;
import tech.jorn.adrian.core.risks.graph.AttackGraph;
import tech.jorn.adrian.core.risks.graph.RiskHardwareNode;
import tech.jorn.adrian.core.risks.graph.RiskNode;
import tech.jorn.adrian.core.risks.graph.RiskSoftwareNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BasicRiskDetector implements IRiskDetection {
    private final List<RiskRule> rules;
    private final IDamageProbabilityCalculator damageProbabilityCalculator;
    private final float damageThreshold;
    private final IRiskSelector riskSelector;

    private final EventDispatcher<List<RiskReport>> risksFoundDispatcher = new EventDispatcher<>();
    private final EventDispatcher<RiskReport> riskSelectedDispatcher = new EventDispatcher<>();

    public BasicRiskDetector(List<RiskRule> rules) {
        this.rules = rules;
        this.damageProbabilityCalculator = new IDamageProbabilityCalculator() {
            @Override
            public float calculate(List<RiskNode> riskPath) {
                return 0;
            }
        };
        this.damageThreshold = 1.0f;
        this.riskSelector = new IRiskSelector() {
            @Override
            public Optional<RiskReport> select(List<RiskReport> risks) {
                if (risks.size() == 0) return Optional.empty();
                return Optional.of(risks.get(0));
            }
        };
    }

    @Override
    public AttackGraph calculateAttackGraph(IKnowledgeBase knowledgeBase) {
        var attackGraph = new AttackGraph();

        knowledgeBase.getNodes().forEach(n -> {
            var node = new RiskHardwareNode(n);
            attackGraph.addNode(node);
        });

        knowledgeBase.getAssets().forEach(a -> {
            var asset = new RiskSoftwareNode(a);
            // TODO: Set parent node for asset: a.getParent()
            attackGraph.addNode(asset);
        });

        this.rules.stream()
                .flatMap(r -> r.evaluate(knowledgeBase, attackGraph).stream())
                .forEach(riskEdge -> {
                    // TODO: Upsert instead of adding
                    attackGraph.addEdge(riskEdge);
                });

        return attackGraph;
    }

    @Override
    public AttackGraph mergeAttackGraph(AttackGraph attackGraph, RiskReport riskReport) {
        // TODO Auto-generated method stub
        return attackGraph;
    }

    public List<RiskReport> findRisks(AttackGraph attackGraph) {
        List<RiskSoftwareNode> criticalSoftwareList = attackGraph.getNodes().stream()
                .filter(n -> n instanceof RiskSoftwareNode && (Boolean) n.getProperty("isCritical").orElse(false))
                .map(n -> (RiskSoftwareNode) n)
                .toList();

        List<RiskHardwareNode> exposedNodes = attackGraph.getNodes().stream()
                .filter(n -> n instanceof RiskHardwareNode && (Boolean) n.getProperty("isExposed").orElse(false))
                .map(n -> (RiskHardwareNode) n)
                .toList();

        var riskReports = new ArrayList<RiskReport>();

        exposedNodes.forEach(hardwareNode -> {
            criticalSoftwareList.forEach(criticalSoftware -> {
                var riskPath = attackGraph.depthFirstSearch(hardwareNode, criticalSoftware);

                var damageProbability = this.damageProbabilityCalculator.calculate(riskPath);
                var damageValue = criticalSoftware.getProperty("damageValue")
                        .map(p -> ((Double) p).floatValue())
                        .orElse(0.0f);

                if (damageProbability * damageValue >= this.damageThreshold) {
                    var riskReport = new RiskReport(riskPath, attackGraph.pathRepresentation(riskPath), damageProbability, damageValue, damageProbability * damageValue);
                    riskReports.add(riskReport);
                }
            });
        });

        return riskReports;
    }

    public Optional<RiskReport> selectRisk(AttackGraph attackGraph) {
        var risks = this.findRisks(attackGraph);
        var risk = this.riskSelector.select(risks);

        risk.ifPresent(this.riskSelectedDispatcher::dispatch);

        if (risk.isEmpty()) return Optional.empty();
        return risk;
    }

    public SubscribableEvent<List<RiskReport>> onRisksFound() { return this.risksFoundDispatcher.subscribable; }
    public SubscribableEvent<RiskReport> onRiskSelected() { return this.riskSelectedDispatcher.subscribable; }
}
