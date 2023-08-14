package tech.jorn.adrian.risks.mitigations;

import tech.jorn.adrian.core.graph.INode;
import tech.jorn.adrian.core.mitigations.Mutation;

public interface IMutator {
    Mutation applyForNode(INode node);
}
