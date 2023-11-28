package tech.jorn.adrian.risks.rules;

import tech.jorn.adrian.core.graphs.AbstractDetailedNode;
import tech.jorn.adrian.core.graphs.base.VoidNode;
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
import tech.jorn.adrian.risks.rules.cves.CveRule;
import tech.jorn.adrian.risks.validators.PropertyValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class EntryRule extends RiskRule {
    boolean includeSoftware = false;
    boolean includeNodes = false;

    private final float factor;

    public EntryRule(float factor) {
        super();
        this.factor = factor;
    }

    public EntryRule includeSoftware() {
        this.includeSoftware = true;
        return this;
    }

    public EntryRule includeNodes() {
        this.includeNodes = true;
        return this;
    }

    @Override
    public void evaluate(KnowledgeBase knowledgeBase, Consumer<RiskEdge> attackGraph) {
        List<KnowledgeBaseEntry<?>> nodes = new ArrayList<>();

        knowledgeBase.getNodes().forEach(node -> {
            if (node.getID().equals(VoidNode.getIncoming().getID())) return;
            if (this.includeSoftware && node instanceof KnowledgeBaseSoftwareAsset) nodes.add(node);
            if (this.includeNodes && node instanceof KnowledgeBaseNode) nodes.add(node);
        });

        nodes.forEach(node -> {
            var exposed = (boolean) node.getProperty("exposed").orElse(false);
            if (!exposed) return;

            var risk = new Risk("entry", this.factor, false, this);
            attackGraph.accept(new RiskEdge(VoidNode.getIncoming(), node, risk));
        });
    }

    @Override
    public <N extends AbstractDetailedNode<P>, P extends AbstractProperty<?>> Optional<Mutation<N>> getAdaptation(
            N node) {
        return Optional.empty();
    }
}
