package tech.jorn.adrian.core.graphs.base;

public class GraphLink<N extends INode> {
    protected final N node;

    public GraphLink(N node) {
        this.node = node;
    }

    public N getNode() {
        return this.node;
    }
}
