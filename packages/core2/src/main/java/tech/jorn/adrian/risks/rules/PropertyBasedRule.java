package tech.jorn.adrian.risks.rules;

import tech.jorn.adrian.core.Pair;
import tech.jorn.adrian.core.graphs.knowledgebase.KnowledgeBase;
import tech.jorn.adrian.core.graphs.knowledgebase.KnowledgeBaseEntry;
import tech.jorn.adrian.core.graphs.knowledgebase.KnowledgeBaseNode;
import tech.jorn.adrian.core.graphs.knowledgebase.KnowledgeBaseSoftwareAsset;
import tech.jorn.adrian.core.graphs.risks.AttackGraph;
import tech.jorn.adrian.core.graphs.risks.AttackGraphNode;
import tech.jorn.adrian.core.risks.Risk;
import tech.jorn.adrian.core.risks.RiskEdge;
import tech.jorn.adrian.core.risks.RiskRule;
import tech.jorn.adrian.core.risks.RiskType;

import java.util.ArrayList;
import java.util.List;

public abstract class PropertyBasedRule<R extends RiskType> extends RiskRule {

    private final String property;
    private final Class<R> riskClass;

    protected PropertyBasedRule(String property, Class<R> riskClass) {
        this.property = property;
        this.riskClass = riskClass;
    }

    @Override
    public List<RiskEdge> evaluate(KnowledgeBase knowledgeBase, AttackGraph attackGraph) {
        var risks = new ArrayList<RiskEdge>();

        List<KnowledgeBaseEntry<?>> nodes = new ArrayList<>();
        knowledgeBase.getNodes().forEach(node -> {
            if (this.includeNodes() && node instanceof KnowledgeBaseNode) nodes.add(node);
            if (this.includeAssets() && node instanceof KnowledgeBaseSoftwareAsset) nodes.add(node);
        });

        nodes.forEach(node -> {
            var parents = knowledgeBase.getParents(node);

            parents.forEach(parent -> {
                if (parent.getID().equals(node.getID())) return;

                Risk risk;
                try {
                    var riskType = this.riskClass.getDeclaredConstructor().newInstance();
                    var hasProperty = (boolean) node.getProperty(this.property)
                            .orElse(false);
                    if (hasProperty) {
                        risk = new Risk(riskType, riskType.getMitigatedRiskFactor());
                    } else
                        risk = new Risk(riskType, riskType.getUnmitigatedRiskFactor());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                var edge = new RiskEdge(
                        attackGraph.findById(node.getID()).get(), // TODO: Do some actual checking before calling
                        attackGraph.findById(parent.getID()).get(),
                        risk
                );
                risks.add(edge);
            });
        });
        return risks;
    }

    public String getProperty() {
        return property;
    }

    protected boolean includeAssets() {
        return true;
    }
    protected boolean includeNodes() {
        return true;
    }
}
