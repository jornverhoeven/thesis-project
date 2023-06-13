package tech.jorn.adrian.core.infrastructure;

import tech.jorn.adrian.core.graph.AbstractGraph;

public class Infrastructure extends AbstractGraph<Node, Link> {

    public void addNode(Node node) {
        var nodes = this.nodes.current();
        nodes.add(node);
        this.nodes.setCurrent(nodes);
    }

    public void addLink(Link link) {
        var links = this.edges.current();
        links.add(link);
        this.edges.setCurrent(links);
    }

}
