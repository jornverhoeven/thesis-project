package tech.jorn.adrian.core.mutations;

import tech.jorn.adrian.core.graphs.AbstractDetailedNode;
import tech.jorn.adrian.core.properties.AbstractProperty;

public class AttributeChange<N extends AbstractDetailedNode<?>, P extends AbstractProperty<?>> extends Mutation<N> {
    private final P newValue;

    public AttributeChange(N node, P newValue) {
        super(node, 0f);
        this.newValue = newValue;
    }

    public P getNewValue() {
        return newValue;
    }

    @Override
    public void apply(N node) {
        node.setProperty(this.newValue.getName(), this.newValue.getValue());
    }

    @Override
    public String toString() {
        return String.format("AttributeChange \033[4m%s\033[0m to \033[4m%s\033[0m for node \033[4m%s\033[0m", this.newValue.getName(), this.newValue.getValue(), this.getNode().getID());
    }
}
