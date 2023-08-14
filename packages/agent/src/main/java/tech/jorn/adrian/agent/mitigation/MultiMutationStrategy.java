package tech.jorn.adrian.agent.mitigation;

import java.util.ArrayList;
import java.util.List;

import tech.jorn.adrian.core.mitigations.Mutation;
import tech.jorn.adrian.core.mitigations.MutationResults;
import tech.jorn.adrian.core.risks.RiskRule;
import tech.jorn.adrian.core.risks.graph.AttackGraph;
import tech.jorn.adrian.risks.mitigations.IMutator;
import tech.jorn.adrian.risks.mitigations.PropertyBasedMutator;
import tech.jorn.adrian.risks.rules.PropertyBasedRule;

/**
 * A mutation strategy that applies a single mutation to all nodes in the attack graph as a single mutation.
 */
public class MultiMutationStrategy implements IMutationStrategy {
    private final IMutator mutator;

    public MultiMutationStrategy(IMutator mutator) {
        this.mutator = mutator;
    }

    public MultiMutationStrategy(RiskRule rule) {
        if (!(rule instanceof PropertyBasedRule<?>))
            throw new IllegalArgumentException("Rule must be a PropertyBasedRule");
        this.mutator = new PropertyBasedMutator<>((PropertyBasedRule<?>) rule);
    }

    @Override
    public List<MutationResults> applyMutations(AttackGraph attackGraph) {
        var mutations = new ArrayList<Mutation>();
        attackGraph.getNodes().forEach(node -> {
            var mutation = this.mutator.applyForNode(node);
            mutations.add(mutation);
        });
        return List.of(new MutationResults(mutations));
    }
    
}
