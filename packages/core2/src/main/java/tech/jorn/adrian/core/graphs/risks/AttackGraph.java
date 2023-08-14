package tech.jorn.adrian.core.graphs.risks;

import tech.jorn.adrian.core.graphs.base.AbstractGraph;
import tech.jorn.adrian.core.graphs.base.GraphLink;

public class AttackGraph extends AbstractGraph<AttackGraphEntry<?>, GraphLink<AttackGraphEntry<?>>> {

    public static AttackGraph mergeGraphs(AttackGraph a, AttackGraph b) {
        return a;
    }
}
