package tech.jorn.adrian.core.risks.graph;

import tech.jorn.adrian.core.graph.INode;
import tech.jorn.adrian.core.properties.SoftwareProperty;

import java.util.List;

public class RiskHardwareNode extends RiskNode {
    public RiskHardwareNode(INode node) {
        super(node);
    }
    public RiskHardwareNode(String id, String name, List<SoftwareProperty<?>> properties) {
        super(id, name, properties);
    }
}
