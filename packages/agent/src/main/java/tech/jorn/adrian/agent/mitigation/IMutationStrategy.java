package tech.jorn.adrian.agent.mitigation;

import java.util.List;

import tech.jorn.adrian.core.mitigations.MutationResults;
import tech.jorn.adrian.core.risks.graph.AttackGraph;

public interface IMutationStrategy {
    List<MutationResults> applyMutations(AttackGraph attackGraph);

    
}


