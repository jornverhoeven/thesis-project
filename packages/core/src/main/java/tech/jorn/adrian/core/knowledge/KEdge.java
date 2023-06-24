package tech.jorn.adrian.core.knowledge;

import tech.jorn.adrian.core.graph.AbstractEdge;
import tech.jorn.adrian.core.observables.SubscribableValueEvent;
import tech.jorn.adrian.core.observables.ValueDispatcher;

import java.util.Date;

public class KEdge extends AbstractEdge<KnowledgeEntry> implements IKnowledge {
    private final ValueDispatcher<Date> updateAt = new ValueDispatcher<>(new Date());
    private KnowledgeOrigin knowledgeOrigin;

    public KEdge(KnowledgeEntry parent, KnowledgeEntry child) {
        super(parent, child);
    }

    @Override
    public SubscribableValueEvent<Date> getUpdatedAt() {
        return this.updateAt.subscribable;
    }

    @Override
    public void setUpdatedAt(Date updatedAt) {
        this.updateAt.setCurrent(updatedAt);
    }

    @Override
    public KnowledgeOrigin getUpdateOrigin() {
        return this.knowledgeOrigin;
    }

    @Override
    public void setUpdateOrigin(KnowledgeOrigin origin) {
        this.knowledgeOrigin = origin;
    }
}
