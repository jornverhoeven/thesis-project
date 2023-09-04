package tech.jorn.adrian.core.mutations;

import tech.jorn.adrian.core.graphs.AbstractDetailedNode;
import tech.jorn.adrian.core.properties.SoftwareProperty;

public class Migration<N extends AbstractDetailedNode<SoftwareProperty<?>>> extends Mutation<N> {
    public Migration(N node) {
        super(node, 0f);
    }

    @Override
    public void apply(N node) {

    }

    @Override
    public String toString() {
        return "Migration";
    }
}
