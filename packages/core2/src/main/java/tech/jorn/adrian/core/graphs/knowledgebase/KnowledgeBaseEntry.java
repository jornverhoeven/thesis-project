package tech.jorn.adrian.core.graphs.knowledgebase;

import tech.jorn.adrian.core.graphs.AbstractDetailedNode;
import tech.jorn.adrian.core.properties.AbstractProperty;

public abstract class KnowledgeBaseEntry<P extends AbstractProperty<?>> extends AbstractDetailedNode<P> {
    private KnowledgeOrigin knowledgeOrigin;

    public KnowledgeBaseEntry(String id) {
        super(id);
    }

    public KnowledgeOrigin getKnowledgeOrigin() {
        return knowledgeOrigin;
    }

    public KnowledgeBaseEntry<P> setKnowledgeOrigin(KnowledgeOrigin knowledgeOrigin) {
        if (this.knowledgeOrigin == null || knowledgeOrigin.compareTo(this.knowledgeOrigin) < 0) {
            this.knowledgeOrigin = knowledgeOrigin;
        }
        return this;
    }
}

