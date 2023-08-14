package tech.jorn.adrian.agent.mitigation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import tech.jorn.adrian.core.mitigations.MutationResults;
import tech.jorn.adrian.core.risks.graph.AttackGraph;

public class MitigationManager {
    private final List<IMutationStrategy> mitigationStrategies;
    private final IMutationSelector mutationSelector;

    public MitigationManager(List<IMutationStrategy> mitigationStrategies, IMutationSelector mutationSelector) {
        this.mitigationStrategies = mitigationStrategies;
        this.mutationSelector = mutationSelector;
    }

    public MutationResults getMitigation(AttackGraph attackGraph) {

        List<MutationResults> mutations = this.mitigationStrategies.stream()
                .map(strategy -> strategy.applyMutations(attackGraph))
                .flatMap(Collection::stream)
                .toList();

        var mitigation = this.mutationSelector.select(attackGraph, mutations);
        return mitigation;
    }
}
