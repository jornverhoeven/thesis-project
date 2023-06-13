package tech.jorn.adrian.core.knowledge;

import tech.jorn.adrian.core.NodeLink;
import tech.jorn.adrian.core.assets.SoftwareAsset;
import tech.jorn.adrian.core.graph.IEdge;
import tech.jorn.adrian.core.graph.IGraph;
import tech.jorn.adrian.core.graph.INode;
import tech.jorn.adrian.core.infrastructure.Node;
import tech.jorn.adrian.core.observables.SubscribableEvent;

import java.util.List;

public interface IKnowledgeBase extends IGraph<KnowledgeEntry, KEdge> {

    void upsertNode(Node node);
    void upsertNode(Node node, KnowledgeOrigin origin);
    void upsertLinks(List<NodeLink> links);
    void upsertAsset(SoftwareAsset asset, Node parent);

    void markAsDirty(Node node);
    void markAsDirty(String nodeId);

    SubscribableEvent<KnowledgeEntry> onKnowledgeUpdate();

    List<KSoftwareAsset> getAssets();
}
