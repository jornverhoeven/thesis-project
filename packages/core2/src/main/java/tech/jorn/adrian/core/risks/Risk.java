package tech.jorn.adrian.core.risks;

public class Risk {
    private final RiskType type;
    private final float factor;

    public Risk(RiskType type, float factor) {
        this.type = type;
        this.factor = factor;
    }

    public RiskType getType() {
        return type;
    }

    public float getFactor() {
        return factor;
    }
}
