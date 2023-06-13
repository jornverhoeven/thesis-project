package tech.jorn.adrian.core.graph;

import tech.jorn.adrian.core.infrastructure.Node;

public abstract class AbstractEdge<N extends INode> implements IEdge<N> {
    private N parent;
    private N child;

    public AbstractEdge(N parent, N child) {
        this.parent = parent;
        this.child = child;
    }

    public N getParent() {
        return this.parent;
    }

    public N getChild() {
        return this.child;
    }

    public AbstractEdge<N> setParent(N parent) {
        this.parent = parent;
        return this;
    }

    public AbstractEdge<N> setChild(N child) {
        this.child = child;
        return this;
    }
}
