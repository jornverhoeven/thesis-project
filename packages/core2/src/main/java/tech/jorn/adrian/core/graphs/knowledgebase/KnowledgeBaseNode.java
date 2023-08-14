package tech.jorn.adrian.core.graphs.knowledgebase;

import tech.jorn.adrian.core.properties.NodeProperty;

public class KnowledgeBaseNode extends KnowledgeBaseEntry<NodeProperty<?>> {
    public KnowledgeBaseNode(String id) {
        super(id);
    }

    @Override
    protected <T> NodeProperty<?> makeProperty(String property, T value) {
        return new NodeProperty<>(property, value);
    }
}
