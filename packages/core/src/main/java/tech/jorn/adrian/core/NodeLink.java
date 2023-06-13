package tech.jorn.adrian.core;

import tech.jorn.adrian.core.infrastructure.Node;

import java.util.List;
import java.util.stream.Collectors;

public class NodeLink {
    private final Node source;
    private final String target;

    public NodeLink(Node node, Node target) {
        this(node, target.getID());
    }

    public NodeLink(Node node, String target) {
        this.source = node;
        this.target = target;
    }

    public Node getSource() {
        return source;
    }

    public String getTarget() {
        return target;
    }

    public static List<NodeLink> fromList(Node parent, List<Node> nodes) {
        return nodes.stream()
                .map(n -> new NodeLink(parent, n))
                .collect(Collectors.toList());
    }
}
