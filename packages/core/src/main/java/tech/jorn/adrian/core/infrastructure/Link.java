package tech.jorn.adrian.core.infrastructure;

import tech.jorn.adrian.core.graph.AbstractEdge;

public class Link extends AbstractEdge<Node> {
    public Link(Node parent, Node child) {
        super(parent, child);
    }
}
