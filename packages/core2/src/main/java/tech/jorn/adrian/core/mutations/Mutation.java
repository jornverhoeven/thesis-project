package tech.jorn.adrian.core.mutations;

import tech.jorn.adrian.core.graphs.AbstractDetailedNode;
import tech.jorn.adrian.core.risks.RiskRule;

public abstract class Mutation<N extends AbstractDetailedNode<?>> {
    private final N node;
    private final float cost;
    private final RiskRule riskRule;

    public Mutation(N node, float cost, RiskRule riskRule) {
        this.node = node;
        this.cost = cost;
        this.riskRule = riskRule;
    }

    public N getNode() {
        return node;
    }

    public float getCost() {
        return this.cost;
    }

    public RiskRule getRiskRule() {
        return riskRule;
    }

    public abstract void apply(N node);

    public abstract boolean isApplicable(N node);

    public abstract String toString();
}

