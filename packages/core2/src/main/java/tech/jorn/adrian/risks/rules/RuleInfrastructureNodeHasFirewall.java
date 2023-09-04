package tech.jorn.adrian.risks.rules;

public class RuleInfrastructureNodeHasFirewall extends PropertyBasedRule {
    public RuleInfrastructureNodeHasFirewall() {
        super("hasFirewall", "infrastructureNodeHasFirewall", 0.8f, 0.2f);
    }

    @Override
    protected boolean includeAssets() {
        return false;
    }

    @Override
    protected boolean allowAssetParent() {
        return false;
    }
}
