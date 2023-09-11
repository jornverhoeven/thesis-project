package tech.jorn.adrian.core.mutations;

import tech.jorn.adrian.core.graphs.AbstractDetailedNode;

public abstract class Mutation<N extends AbstractDetailedNode<?>> {
    private final N node;
    private final float cost;

    public Mutation(N node, float cost) {
        this.node = node;
        this.cost = cost;
    }

    public N getNode() {
        return node;
    }

    public float getCosts() {
        return this.cost;
    }

    public abstract void apply(N node);

    public abstract boolean isApplicable(N node);

    public abstract String toString();
}

