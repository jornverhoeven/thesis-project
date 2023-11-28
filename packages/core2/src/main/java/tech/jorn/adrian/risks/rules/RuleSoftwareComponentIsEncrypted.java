package tech.jorn.adrian.risks.rules;

import java.util.Optional;

import tech.jorn.adrian.core.graphs.AbstractDetailedNode;
import tech.jorn.adrian.core.graphs.knowledgebase.KnowledgeBase;
import tech.jorn.adrian.core.graphs.knowledgebase.KnowledgeBaseSoftwareAsset;
import tech.jorn.adrian.core.graphs.risks.AttackGraphSoftwareAsset;
import tech.jorn.adrian.core.mutations.AttributeChange;
import tech.jorn.adrian.core.mutations.Mutation;
import tech.jorn.adrian.core.mutations.SoftwareAttributeChange;
import tech.jorn.adrian.core.properties.AbstractProperty;
import tech.jorn.adrian.core.properties.SoftwareProperty;

public class RuleSoftwareComponentIsEncrypted extends PropertyBasedRule {
    public RuleSoftwareComponentIsEncrypted() {
        super("isEncrypted", "softwareComponentIsEncrypted", 0.8f, 0.2f);
    }

    @Override
    protected boolean includeNodes() {
        return false;
    }

    @Override
    public <N extends AbstractDetailedNode<P>, P extends AbstractProperty<?>> Optional<Mutation<N>> getAdaptation(N node) {
        if (!(node instanceof KnowledgeBaseSoftwareAsset) || !(node instanceof AttackGraphSoftwareAsset)) return Optional.empty();
        System.out.println("Checking mitigation for " + node.getID() + " with property " + this.getProperty() + " and value " + node.getProperty(this.getProperty()) + " mitigated " +this.isMitigated(node.getProperty(this.getProperty())));
        if (this.isMitigated(node.getProperty(this.getProperty()))) return Optional.empty();

        var adaptation = new SoftwareAttributeChange<>(node, new SoftwareProperty<>(this.getProperty(), true), 100, 2000, this);
        return Optional.of(adaptation);
    }
}
