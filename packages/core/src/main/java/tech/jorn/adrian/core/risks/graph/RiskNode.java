package tech.jorn.adrian.core.risks.graph;

import tech.jorn.adrian.core.graph.AbstractNode;
import tech.jorn.adrian.core.graph.INode;
import tech.jorn.adrian.core.properties.SoftwareProperty;

import java.util.List;

// TODO: give RiskNode a generic type
public abstract class RiskNode extends AbstractNode<SoftwareProperty<?>> {
    public RiskNode(INode node) {
        this(node.getID(), node.getName(), node.getProperties());
    }
    public RiskNode(String id, String name, List<SoftwareProperty<?>> properties) {
        super(id, name, properties);
    }
}
