package tech.jorn.adrian.core.knowledge;

import tech.jorn.adrian.core.assets.SoftwareAsset;
import tech.jorn.adrian.core.graph.AbstractNode;
import tech.jorn.adrian.core.graph.INode;
import tech.jorn.adrian.core.observables.SubscribableValueEvent;
import tech.jorn.adrian.core.observables.ValueDispatcher;
import tech.jorn.adrian.core.properties.SoftwareProperty;

import java.util.Date;

public class KSoftwareAsset extends AbstractNode<SoftwareProperty<?>> implements KnowledgeEntry {
    private final ValueDispatcher<Date> updateAt = new ValueDispatcher<>(new Date());
    private KnowledgeOrigin knowledgeOrigin;
    private final INode parent;

    public KSoftwareAsset(SoftwareAsset asset, INode parent) {
        super(asset.getID(), asset.getName(), asset.getProperties());
        this.parent = parent;
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

    public INode getParent() {
        return parent;
    }
}