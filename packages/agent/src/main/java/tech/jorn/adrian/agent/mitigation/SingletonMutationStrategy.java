package tech.jorn.adrian.agent.mitigation;

import java.util.Arrays;
import java.util.List;

import tech.jorn.adrian.core.mitigations.MutationResults;
import tech.jorn.adrian.core.risks.RiskRule;
import tech.jorn.adrian.core.risks.graph.AttackGraph;
import tech.jorn.adrian.risks.mitigations.IMutator;
import tech.jorn.adrian.risks.mitigations.PropertyBasedMutator;
import tech.jorn.adrian.risks.rules.PropertyBasedRule;

/**
 * A mutation strategy that applies a single mutation to each node in the attack graph.
 */
public class SingletonMutationStrategy implements IMutationStrategy {
    private final IMutator mutator;

    public SingletonMutationStrategy(IMutator mutator) {
        this.mutator = mutator;
    }
    public SingletonMutationStrategy(RiskRule rule) {
        if (!(rule instanceof PropertyBasedRule<?>))
            throw new IllegalArgumentException("Rule must be a PropertyBasedRule");
        this.mutator = new PropertyBasedMutator<>((PropertyBasedRule<?>) rule);
    }

    @Override
    public List<MutationResults> applyMutations(AttackGraph attackGraph) {
        var mitigationResults = attackGraph.getNodes().stream().map(node -> {
            var mitigation = this.mutator.applyForNode(node);
            return new MutationResults(Arrays.asList(mitigation));
        }).toList();
        return mitigationResults;
    }
    
}
