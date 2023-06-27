package tech.jorn.adrian.agent.mitigation;

import tech.jorn.adrian.core.mitigations.MutationResults;
import tech.jorn.adrian.core.risks.graph.AttackGraph;

import java.util.List;

public class RandomRiskMutationSelector implements IMutationSelector {
    @Override
    public MutationResults select(AttackGraph attackGraph, List<MutationResults> mutations) {
        var index = (int) Math.floor(Math.random() * mutations.size());
        return mutations.get(index);
    }
}
