package tech.jorn.adrian.core.knowledge;

import tech.jorn.adrian.core.assets.SoftwareAsset;
import tech.jorn.adrian.core.graph.AbstractNode;
import tech.jorn.adrian.core.infrastructure.Node;
import tech.jorn.adrian.core.observables.SubscribableValueEvent;
import tech.jorn.adrian.core.observables.ValueDispatcher;
import tech.jorn.adrian.core.properties.NodeProperty;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class KNode extends AbstractNode<NodeProperty<?>> implements KnowledgeEntry {
    List<SoftwareAsset> softwareAssets = new ArrayList<>();

    private final ValueDispatcher<Date> updateAt = new ValueDispatcher<>(new Date());
    private KnowledgeOrigin knowledgeOrigin;

    public KNode(String id) {
        this(id, "KnowledgeNode " + id, new ArrayList<>());
    }

    public KNode(String id, String name, List<NodeProperty<?>> properties) {
        super(id, name, properties);
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

    public List<SoftwareAsset> getSoftwareAssets() {
        return this.softwareAssets;
    }

    static public KNode fromNode(Node node) {
        return KNode.fromNode(node, KnowledgeOrigin.Direct);
    }

    static public KNode fromNode(Node node, KnowledgeOrigin origin) {
        var knowledge = new KNode(node.getID(), node.getName(), node.getProperties());
        knowledge.knowledgeOrigin = origin;
        knowledge.updateAt.setCurrent(new Date());
//        knowledge.properties.addAll(node.getProperties());
        knowledge.softwareAssets.addAll(node.getSoftwareAssets());
        return knowledge;
    }
}
