package tech.jorn.adrian.risks.rules;

import tech.jorn.adrian.core.graphs.AbstractDetailedNode;
import tech.jorn.adrian.core.graphs.knowledgebase.KnowledgeBase;
import tech.jorn.adrian.core.graphs.knowledgebase.KnowledgeBaseEntry;
import tech.jorn.adrian.core.graphs.knowledgebase.KnowledgeBaseNode;
import tech.jorn.adrian.core.graphs.knowledgebase.KnowledgeBaseSoftwareAsset;
import tech.jorn.adrian.core.mutations.AttributeChange;
import tech.jorn.adrian.core.mutations.Mutation;
import tech.jorn.adrian.core.properties.AbstractProperty;
import tech.jorn.adrian.core.properties.NodeProperty;
import tech.jorn.adrian.core.risks.Risk;
import tech.jorn.adrian.core.risks.RiskEdge;
import tech.jorn.adrian.core.risks.RiskRule;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public abstract class PropertyBasedRule extends RiskRule {

    private final String property;
    private final String ruleId;
    private final float mitigatedFactor;
    private final float unmitigatedFactor;

    protected PropertyBasedRule(String property, String ruleId, float unmitigatedFactor, float mitigatedFactor) {
        this.property = property;
        this.ruleId = ruleId;
        this.unmitigatedFactor = unmitigatedFactor;
        this.mitigatedFactor = mitigatedFactor;
    }

    @Override
    public void evaluate(KnowledgeBase knowledgeBase, Consumer<RiskEdge> dispatchRisk) {
        List<KnowledgeBaseEntry<?>> nodes = new ArrayList<>();

        knowledgeBase.getNodes().forEach(node -> {
            if (this.includeNodes() && node instanceof KnowledgeBaseNode) nodes.add(node);
            if (this.includeAssets() && node instanceof KnowledgeBaseSoftwareAsset) nodes.add(node);
        });

        nodes.forEach(node -> {
            var parents = knowledgeBase.getParents(node);

            parents.forEach(parent -> {
                // Break if the node and parent are equal somehow
                if (parent.getID().equals(node.getID())) return;

                // If the parent is a software asset, and it is ignored, we skip it
                if (parent instanceof KnowledgeBaseSoftwareAsset && !this.allowAssetParent()) return;
                // If the parent is a node, and it is ignored, we skip it
                if (parent instanceof KnowledgeBaseNode && !this.allowNodeParent()) return;

                Risk risk;
                try {
                    var check = this.isMitigated(node.getProperty(property));
                    if (check) risk = new Risk(this.ruleId, this.mitigatedFactor, true, this);
                    else risk = new Risk(this.ruleId, this.unmitigatedFactor, false, this);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                var riskEdge = new RiskEdge(parent, node, risk);
                dispatchRisk.accept(riskEdge);
            });
        });
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

    protected boolean allowAssetParent() { return true; }
    protected boolean allowNodeParent() { return true; }

    public boolean isMitigated(Optional<?> property) {
        return property
                .map(p -> (Boolean) p)
                .orElse(Boolean.FALSE);
    }

    public <N extends AbstractDetailedNode<P>, P extends AbstractProperty<?>> Optional<Mutation<N>> getAdaptation(N node) {
        System.out.println("Checking mitigation for " + node.getID() + " with property " + this.getProperty() + " and value " + node.getProperty(this.getProperty()) + " mitigated " +this.isMitigated(node.getProperty(this.getProperty())));
        if (this.isMitigated(node.getProperty(this.getProperty()))) return Optional.empty();

        var adaptation = new AttributeChange<>(node, new NodeProperty<>(this.getProperty(), true), 100, 1000, this);
        return Optional.of(adaptation);
    }
}
