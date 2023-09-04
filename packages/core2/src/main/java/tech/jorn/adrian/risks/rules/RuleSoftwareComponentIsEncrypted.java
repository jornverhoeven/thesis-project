package tech.jorn.adrian.risks.rules;

public class RuleSoftwareComponentIsEncrypted extends PropertyBasedRule {
    public RuleSoftwareComponentIsEncrypted() {
        super("isEncrypted", "softwareComponentIsEncrypted", 0.8f, 0.2f);
    }

    @Override
    protected boolean includeNodes() {
        return false;
    }
}
