package tech.jorn.adrian.core;

import tech.jorn.adrian.core.graph.INode;
import tech.jorn.adrian.core.infrastructure.Node;

import java.util.List;
import java.util.stream.Collectors;

public class NodeLink {
    private final INode source;
    private final String target;

    public NodeLink(INode node, INode target) {
        this(node, target.getID());
    }

    public NodeLink(INode node, String target) {
        this.source = node;
        this.target = target;
    }

    public INode getSource() {
        return source;
    }

    public String getTarget() {
        return target;
    }

    public static List<NodeLink> fromList(INode parent, List<INode> nodes) {
        return nodes.stream()
                .map(n -> new NodeLink(parent, n))
                .collect(Collectors.toList());
    }
}
