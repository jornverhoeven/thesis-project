package tech.jorn.adrian.core.graph;

public interface IEdge<N extends INode> {
    N getParent();
    N getChild();
}
