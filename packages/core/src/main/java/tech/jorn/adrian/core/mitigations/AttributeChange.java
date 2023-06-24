package tech.jorn.adrian.core.mitigations;

import tech.jorn.adrian.core.graph.INode;

public class AttributeChange<T> extends Mitigation {
    private final INode node;
    private final String attribute;
    private final T value;

    public AttributeChange(INode node, String attribute, T value) {
        super();
        this.node = node;
        this.attribute = attribute;
        this.value = value;
    }
}
