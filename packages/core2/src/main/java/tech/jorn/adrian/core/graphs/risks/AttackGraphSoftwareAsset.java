package tech.jorn.adrian.core.graphs.risks;

import tech.jorn.adrian.core.properties.SoftwareProperty;

public class AttackGraphSoftwareAsset extends AttackGraphEntry<SoftwareProperty<?>> {
    public AttackGraphSoftwareAsset(String id) {
        super(id);
    }

    @Override
    protected <T> SoftwareProperty<?> makeProperty(String property, T value) {
        return new SoftwareProperty<>(property, value);
    }
}
