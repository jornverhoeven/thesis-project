package tech.jorn.adrian.risks.rules;

import tech.jorn.adrian.risks.types.RiskSoftwareComponentIsEncrypted;

public class RuleSoftwareComponentIsEncrypted extends PropertyBasedRule<RiskSoftwareComponentIsEncrypted> {
    public RuleSoftwareComponentIsEncrypted() {
        super("isEncrypted", RiskSoftwareComponentIsEncrypted.class);
    }

    @Override
    protected boolean includeNodes() {
        return false;
    }
}
