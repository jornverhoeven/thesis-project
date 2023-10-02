package tech.jorn.adrian.agent.services;

import tech.jorn.adrian.core.agents.IAgentConfiguration;
import tech.jorn.adrian.core.graphs.base.GraphLink;
import tech.jorn.adrian.core.graphs.base.INode;
import tech.jorn.adrian.core.graphs.base.VoidNode;
import tech.jorn.adrian.core.graphs.knowledgebase.KnowledgeBase;
import tech.jorn.adrian.core.graphs.knowledgebase.KnowledgeBaseNode;
import tech.jorn.adrian.core.graphs.knowledgebase.KnowledgeBaseSoftwareAsset;
import tech.jorn.adrian.core.graphs.risks.*;
import tech.jorn.adrian.core.risks.RiskEdge;
import tech.jorn.adrian.core.risks.RiskReport;
import tech.jorn.adrian.core.risks.RiskRule;
import tech.jorn.adrian.core.services.RiskDetection;
import tech.jorn.adrian.core.services.probability.IRiskProbabilityCalculator;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class BasicRiskDetection implements RiskDetection {
    private final List<RiskRule> riskRules;
    private final IRiskProbabilityCalculator probabilityCalculator;
    private final IAgentConfiguration configuration;

    public BasicRiskDetection(List<RiskRule> riskRules, IRiskProbabilityCalculator probabilityCalculator, IAgentConfiguration configuration) {
        this.riskRules = riskRules;
        this.probabilityCalculator = probabilityCalculator;
        this.configuration = configuration;
    }

    @Override
    public AttackGraph createAttackGraph(KnowledgeBase knowledgeBase) {
        var attackGraph = new AttackGraph();
        attackGraph.upsertNode(VoidNode.getIncoming());

        knowledgeBase.getNodes().forEach(node -> {
            AttackGraphEntry<?> attackNode;
            if (node instanceof KnowledgeBaseNode)
                attackNode = AttackGraphNode.fromNode((KnowledgeBaseNode) node);
            else // if (node instanceof KnowledgeBaseSoftwareAsset)
                attackNode = AttackGraphSoftwareAsset.fromNode((KnowledgeBaseSoftwareAsset) node);

            node.getProperties().forEach(attackNode::setFromProperty);
            attackGraph.upsertNode(attackNode);
        });

        Consumer<RiskEdge> dispatchRisk = this.createRiskDispatcher(attackGraph);

        this.riskRules.forEach(r -> r.evaluate(knowledgeBase, dispatchRisk));
        return attackGraph;
    }

    @Override
    public List<RiskReport> identifyRisks(AttackGraph attackGraph) {
        // 1. Collect all the exposed nodes and critical software components to calculate all paths.
        var voidNode = attackGraph.findById(VoidNode.getIncoming().getID());
        List<? extends AttackGraphEntry<?>> exposed = attackGraph.getNeighbours(VoidNode.getIncoming());
        List<AttackGraphSoftwareAsset> criticalSoftware = new ArrayList<>();
        attackGraph.getNodes().forEach(node -> {
            if (node instanceof AttackGraphSoftwareAsset) {
                if ((boolean) node.getProperty("isCritical").orElse(false)) {
                    criticalSoftware.add((AttackGraphSoftwareAsset) node);
                }
            }
        });

        // 2. If there are no nodes to start from or go to, we can exit.
        if (exposed.isEmpty() || criticalSoftware.isEmpty())
            return new ArrayList<>();

        // 3. Get all possible paths between all different start en end nodes.
        List<List<AttackGraphEntry<?>>> criticalPaths = new ArrayList<>();
        exposed.forEach(node -> {
            criticalSoftware.forEach(asset -> {
                var newPaths = attackGraph.findPathsTo(node, asset).stream()
                        .filter(path -> {
                            // Check if the path contains the current node, otherwise we should not select it
                            if (this.configuration == null) return true;
                            var contained = path.stream().filter(n -> n.getID().equals(this.configuration.getNodeID())).findAny();
                            return contained.isPresent();
                        })
                        .toList();
                newPaths.forEach(path -> path.add(0, voidNode.get()));
                criticalPaths.addAll(newPaths);
            });
        });

        // 4. Calculate the risk reports based on the critical paths.
        List<RiskReport> riskReports = criticalPaths.stream().map(criticalPath -> {
            var path = criticalPath.stream().map(n -> (INode) n).toList();
            var graph = attackGraph.getGraphForPath(criticalPath);
            var probability = attackGraph.getProbabilityForPath(criticalPath, this.probabilityCalculator);
            var software = criticalPath.get(criticalPath.size() - 1);
            var damage = (float) software.getProperty("damageValue").orElse(0.0f);
            return new RiskReport(graph, path, probability, damage, probability * damage);
        }).toList();

        return riskReports;
    }

    public Consumer<RiskEdge> createRiskDispatcher(AttackGraph attackGraph) {
        return e -> {
            var from = attackGraph.findById(e.from().getID()).get();
            var to = attackGraph.findById(e.to().getID()).get();

            attackGraph.addEdge(from, to, e.risk());
        };
    }
}
