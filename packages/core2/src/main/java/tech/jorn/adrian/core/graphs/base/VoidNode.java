package tech.jorn.adrian.core.graphs.base;

import tech.jorn.adrian.core.graphs.risks.AttackGraphEntry;
import tech.jorn.adrian.core.graphs.risks.AttackGraphNode;

public class VoidNode extends AbstractNode {
    public VoidNode() {
        super("VOID");
    }

    public static AttackGraphEntry<?> getIncoming() {
        return new AttackGraphNode("VOID");
    }
}
