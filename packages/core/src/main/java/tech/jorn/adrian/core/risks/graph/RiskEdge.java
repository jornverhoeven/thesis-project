package tech.jorn.adrian.core.risks.graph;

import tech.jorn.adrian.core.graph.IEdge;
import tech.jorn.adrian.core.properties.RiskProperty;
import tech.jorn.adrian.core.risks.Risk;

import java.util.ArrayList;
import java.util.List;

public class RiskEdge implements IEdge<RiskNode> {
    private final RiskNode parent;
    private final RiskNode child;
    private final List<Risk> risks = new ArrayList<>();
    private final List<RiskProperty<?>> properties = new ArrayList<>();

    public RiskEdge(RiskNode parent, RiskNode child) {
        this.parent = parent;
        this.child = child;
    }

    @Override
    public RiskNode getParent() {
        return parent;
    }

    @Override
    public RiskNode getChild() {
        return child;
    }

    public List<Risk> getRisks() { return this.risks; };

    public List<RiskProperty<?>> getProperties() {
        return properties;
    }
}
