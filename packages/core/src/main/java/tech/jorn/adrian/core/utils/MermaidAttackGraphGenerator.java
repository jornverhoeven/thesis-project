package tech.jorn.adrian.core.utils;

import tech.jorn.adrian.core.risks.graph.RiskEdge;
import tech.jorn.adrian.core.risks.graph.RiskNode;

public class MermaidAttackGraphGenerator extends MermaidGraphGenerator<RiskNode, RiskEdge> {
    @Override
    protected String printEdge(RiskEdge edge) {
        return String.format("\t%s -->|%s| %s", edge.getParent().getID(), edge.getRisks().get(0).getFactor(), edge.getChild().getID());
    }
}
