package tech.jorn.adrian.core.graphs.knowledgebase;

import tech.jorn.adrian.core.properties.SoftwareProperty;

public class KnowledgeBaseSoftwareAsset extends KnowledgeBaseEntry<SoftwareProperty<?>> {
    public KnowledgeBaseSoftwareAsset(String id) {
        super(id);
    }

    @Override
    protected <T> SoftwareProperty<?> makeProperty(String property, T value) {
        return new SoftwareProperty<>(property, value);
    }
}
