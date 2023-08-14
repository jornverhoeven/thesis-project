package tech.jorn.adrian.risks.mitigations;

import java.util.Optional;

import tech.jorn.adrian.core.graph.INode;
import tech.jorn.adrian.core.mitigations.AttributeChange;
import tech.jorn.adrian.risks.rules.PropertyBasedRule;

public class PropertyBasedMutator<R extends PropertyBasedRule<?>> implements IMutator {
    private final R rule; 

    public PropertyBasedMutator(R rule) {
        this.rule = rule;
    }

    @Override
    public AttributeChange<Boolean> applyForNode(INode node) {
        var property = rule.getProperty();
        Optional<Boolean> old = node.getProperty(property);

        if (old.isPresent()) {
            // node.setProperty(property, rule.getNewValue());
            return new AttributeChange<Boolean>(node, property, !old.get());
        } else {
            return new AttributeChange<Boolean>(node, property, true);
        }
    }
}
