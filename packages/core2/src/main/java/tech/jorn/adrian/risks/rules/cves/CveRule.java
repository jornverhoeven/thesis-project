package tech.jorn.adrian.risks.rules.cves;

import tech.jorn.adrian.core.graphs.AbstractDetailedNode;
import tech.jorn.adrian.core.graphs.base.INode;
import tech.jorn.adrian.core.mutations.Mutation;
import tech.jorn.adrian.core.properties.AbstractProperty;
import tech.jorn.adrian.core.risks.RiskRule;
import tech.jorn.adrian.risks.validators.PropertyValidator;

import java.util.Optional;
import java.util.function.Predicate;

public abstract class CveRule<T> extends RiskRule {

    protected final PropertyValidator<T> validator;
    private final String cve;
    private final String property;
    private final float exploitabilityScore;

    protected final float cost;
    protected final float time;

    protected boolean allowAssets = true;
    protected boolean allowNodes = true;

    public CveRule(String cve, String property, PropertyValidator<T> validator, float exploitabilityScore, float cost, float time) {
        super();
        this.cve = cve;
        this.property = property;
        this.exploitabilityScore = exploitabilityScore;
        this.cost = cost;
        this.time = time;
        this.validator = validator;
    }

    public <C extends CveRule<T>> C withoutAssets() {
        this.allowAssets = false;
        return (C) this;
    }

    public <C extends CveRule<T>> C withoutNodes() {
        this.allowNodes = false;
        return (C) this;
    }

    public PropertyValidator<T> getValidator() {
        return validator;
    }

    public String getCve() {
        return cve;
    }

    public String getProperty() {
        return property;
    }

    public float getExploitabilityScore() {
        return exploitabilityScore;
    }

    public boolean isAllowAssets() {
        return allowAssets;
    }

    public boolean isAllowNodes() {
        return allowNodes;
    }

    public <N extends AbstractDetailedNode<P>, P extends AbstractProperty<?>> Optional<Mutation<N>> getAdaptation(N node) {
        return Optional.empty();
    }
}

