package tech.jorn.adrian.risks.rules;

public class RuleInfrastructureNodeIsPhysicallySecured extends PropertyBasedRule {
    public RuleInfrastructureNodeIsPhysicallySecured() {
        super("isPhysicallySecured", "infrastructureNodeIsPhysicallySecured", 0.8f, 0.2f);
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
