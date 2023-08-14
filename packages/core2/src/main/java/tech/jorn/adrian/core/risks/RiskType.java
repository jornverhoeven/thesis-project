package tech.jorn.adrian.core.risks;

public abstract class RiskType {
    private final String label;
    private final Float unmitigatedRiskFactor;
    private final Float mitigatedRiskFactor;

    public RiskType(String label, Float unmitigatedRiskFactor, Float mitigatedRiskFactor) {
        this.label = label;
        this.unmitigatedRiskFactor = unmitigatedRiskFactor;
        this.mitigatedRiskFactor = mitigatedRiskFactor;
    }

    public String getLabel() {
        return label;
    }

    public Float getUnmitigatedRiskFactor() {
        return unmitigatedRiskFactor;
    }

    public Float getMitigatedRiskFactor() {
        return mitigatedRiskFactor;
    }
}
