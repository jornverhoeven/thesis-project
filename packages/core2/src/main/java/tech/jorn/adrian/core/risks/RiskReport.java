package tech.jorn.adrian.core.risks;

import tech.jorn.adrian.core.graphs.base.INode;

import java.util.List;

public class RiskReport {
    private final List<INode> path;
    private final float probability;
    private final float damageValue;
    private final float damage;

    public RiskReport(List<INode> path, float probability, float damageValue, float damage){
        this.path = path;
        this.probability = probability;
        this.damageValue = damageValue;
        this.damage = damage;
    }

    public List<INode> getPath() {
        return path;
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
