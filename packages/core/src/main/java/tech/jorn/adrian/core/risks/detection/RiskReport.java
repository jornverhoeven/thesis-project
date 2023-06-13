package tech.jorn.adrian.core.risks.detection;

import tech.jorn.adrian.core.risks.graph.RiskNode;

import java.util.List;

public class RiskReport {
    private final List<RiskNode> path;
    private final String pathRepresentation;
    private final float probability;
    private final float damageValue;
    private final float damage;

    public RiskReport(List<RiskNode> path, String pathRepresentation, float probability, float damageValue, float damage) {

        this.path = path;
        this.pathRepresentation = pathRepresentation;
        this.probability = probability;
        this.damageValue = damageValue;
        this.damage = damage;
    }

    public List<RiskNode> getPath() {
        return this.path;
    }

    public String getPathRepresentation() {
        return pathRepresentation;
    }

    public float getProbability() {
        return probability;
    }

    public float getDamageValue() {
        return damageValue;
    }

    public float getDamage() {
        return damage;
    }
}
