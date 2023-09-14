package tech.jorn.adrian.core.mutations;

import tech.jorn.adrian.core.graphs.AbstractDetailedNode;
import tech.jorn.adrian.core.properties.SoftwareProperty;
import tech.jorn.adrian.core.risks.RiskRule;

public class Migration<N extends AbstractDetailedNode<SoftwareProperty<?>>> extends Mutation<N> {
    public Migration(N node, RiskRule riskRule) {
        super(node, 0f, riskRule);
    }

    @Override
    public void apply(N node) {

    }

    @Override
    public boolean isApplicable(N node) {
        return false;
    }

    @Override
    public String toString() {
        return "Migration";
    }
}
