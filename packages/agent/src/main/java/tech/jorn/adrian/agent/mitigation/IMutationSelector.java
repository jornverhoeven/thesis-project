package tech.jorn.adrian.agent.mitigation;

import java.util.List;

import tech.jorn.adrian.core.mitigations.MutationResults;
import tech.jorn.adrian.core.risks.graph.AttackGraph;

public interface IMutationSelector {
    MutationResults select(AttackGraph attackGraph, List<MutationResults> mutations);
}
