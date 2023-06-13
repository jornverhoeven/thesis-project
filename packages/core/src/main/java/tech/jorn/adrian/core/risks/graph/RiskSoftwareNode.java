package tech.jorn.adrian.core.risks.graph;

import tech.jorn.adrian.core.graph.INode;
import tech.jorn.adrian.core.properties.SoftwareProperty;

import java.util.List;

public class RiskSoftwareNode extends RiskNode {

    public RiskSoftwareNode(INode node) {
        super(node);
    }
    public RiskSoftwareNode(String id, String name, List<SoftwareProperty<?>> properties) {
        super(id, name, properties);
    }
}
