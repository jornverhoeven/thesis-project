package tech.jorn.adrian.risks.rules.cves;

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
import tech.jorn.adrian.risks.validators.PropertyValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class EntryCve extends CveRule<String> {
    boolean includeSoftware = false;
    boolean includeNodes = false;

    private final String mitigatedVersion;

    public EntryCve(String cve, String property, PropertyValidator<String> validator, float exploitabilityScore, float cost, float time, String mitigatedVersion) {
        super(cve, property, validator, exploitabilityScore, cost ,time);
        this.mitigatedVersion = mitigatedVersion;
    }

    public EntryCve includeSoftware() {
        this.includeSoftware = true;
        return this;
    }

    public EntryCve includeNodes() {
        this.includeNodes = true;
        return this;
    }

    @Override
    public void evaluate(KnowledgeBase knowledgeBase, Consumer<RiskEdge> attackGraph) {
        List<KnowledgeBaseEntry<?>> nodes = new ArrayList<>();

        knowledgeBase.getNodes().forEach(node -> {
            if (this.includeSoftware && node instanceof KnowledgeBaseSoftwareAsset) nodes.add(node);
            if (this.includeNodes && node instanceof KnowledgeBaseNode) nodes.add(node);
        });

        nodes.forEach(node -> {
            var property = node.getProperty(this.getProperty());
            if (property.isEmpty()) return;

            var isVulnerable = this.validator.validate((String) property.get());
            if (!isVulnerable) return;

            var risk = new Risk(this.getCve(), this.getExploitabilityScore() / 10, false, this);
            attackGraph.accept(new RiskEdge(VoidNode.getIncoming(), node, risk));
        });
    }

    @Override
    public <N extends AbstractDetailedNode<P>, P extends AbstractProperty<?>> Optional<Mutation<N>> getAdaptation(N node) {
        if (this.mitigatedVersion == null) return Optional.empty();
        if (node.getProperty(this.getProperty()).isEmpty()) return Optional.empty();

        var adaptation = new AttributeChange<>(node, new NodeProperty<>(this.getProperty(), this.mitigatedVersion), this.cost, this.time, this);
        return Optional.of(adaptation);
    }
}
