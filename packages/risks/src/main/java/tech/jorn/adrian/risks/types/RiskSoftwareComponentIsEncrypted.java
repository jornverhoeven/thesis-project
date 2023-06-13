package tech.jorn.adrian.risks.types;

import tech.jorn.adrian.core.risks.RiskType;

public class RiskSoftwareComponentIsEncrypted extends RiskType {
    public static final float unmitigatedRiskFactor = 0.8f;
    public static final float mitigatedRiskFactor = 0.2f;

    public RiskSoftwareComponentIsEncrypted() {
        super("Software component is encrypted", 0.8F, 0.2F);
    }
}
