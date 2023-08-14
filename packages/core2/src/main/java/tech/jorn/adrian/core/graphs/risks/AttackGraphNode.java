package tech.jorn.adrian.core.graphs.risks;

import tech.jorn.adrian.core.properties.NodeProperty;

public class AttackGraphNode extends AttackGraphEntry<NodeProperty<?>> {
    public AttackGraphNode(String id) {
        super(id);
    }

    @Override
    protected <T> NodeProperty<?> makeProperty(String property, T value) {
        return new NodeProperty<>(property, value);
    }
}
