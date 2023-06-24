package tech.jorn.adrian.agent;

import tech.jorn.adrian.core.IIdentifiable;
import tech.jorn.adrian.core.NodeLink;
import tech.jorn.adrian.core.assets.SoftwareAsset;
import tech.jorn.adrian.core.graph.AbstractGraph;
import tech.jorn.adrian.core.infrastructure.Node;
import tech.jorn.adrian.core.knowledge.*;
import tech.jorn.adrian.core.observables.EventDispatcher;
import tech.jorn.adrian.core.observables.SubscribableEvent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class KnowledgeBase extends AbstractGraph<KnowledgeEntry, KEdge> implements IKnowledgeBase {
    private final EventDispatcher<KnowledgeEntry> knowledgeEvent = new EventDispatcher<>();
    private final List<KSoftwareAsset> assets = new ArrayList<>();

    @Override
    public void upsertNode(Node node) {
        this.upsertNode(node, KnowledgeOrigin.Indirect);
    }

    @Override
    public void upsertNode(Node node, KnowledgeOrigin origin) {
        var knowledge = KNode.fromNode(node);
        this.upsertNode(knowledge, origin);
    }

    public void upsertNode(KnowledgeEntry node, KnowledgeOrigin origin) {
        var index = this.findNodeIndex(node);

        node.setUpdateOrigin(origin);

        var nodes = this.nodes.current();
        if (index == -1) nodes.add(node);
        else nodes.set(index, node);

        this.nodes.setCurrent(nodes);
        this.knowledgeEvent.dispatch(node); // Might be a bit extra?
    }

    @Override
    public void upsertLinks(List<NodeLink> links) {
        links.forEach(link -> {
            var source = this.nodes.current().stream()
                    .filter(n -> n.getID().equals(link.getSource().getID()))
                    .findFirst();
            var _target = this.nodes.current().stream()
                    .filter(n -> n.getID().equals(link.getTarget()))
                    .findFirst();

            if (source.isEmpty()) {
                // Not sure what to do here
                return;
            }

            KNode target;
            if (_target.isEmpty()) {
                // Target is not yet known, create a template
                var newTarget = new KNode(link.getTarget());
                newTarget.setUpdateOrigin(KnowledgeOrigin.Derived);
                target = newTarget;
            } else {
                if (!_target.get().getUpdateOrigin().equals(KnowledgeOrigin.Direct)) {
                    _target.get().setUpdateOrigin(KnowledgeOrigin.Indirect);
                }
                target = (KNode) _target.get();
            }

            // TODO: Only update when dates are newer??
            this.upsertNode(target, target.getUpdateOrigin());

            // Add the new edges (two since they are bidirectional)
            // TODO: Update derived edges to indirect
            var l1 = this.edges.current().stream()
                    .filter(e -> e.getParent().getID().equals(source.get().getID()) && e.getChild().getID().equals(target.getID()))
                    .findFirst();
            var l3 = this.edges.current().stream()
                    .filter(e -> e.getParent().getID().equals(target.getID()) && e.getChild().getID().equals(source.get().getID()))
                    .findFirst();

            if (l1.isPresent() || l3.isPresent()) {
//                if (source.get().getUpdateOrigin().equals(KnowledgeOrigin.Derived))
//                    source.get().setUpdateOrigin(KnowledgeOrigin.Indirect);
//                if (target.getUpdateOrigin().equals(KnowledgeOrigin.Derived))
//                    target.setUpdateOrigin(KnowledgeOrigin.Indirect);
                l1.ifPresent(e -> {
                    if (e.getUpdateOrigin().equals(KnowledgeOrigin.Derived))
                        e.setUpdateOrigin(KnowledgeOrigin.Indirect);
                    e.setParent(source.get());
                    e.setChild(target);
                });
                l3.ifPresent(e -> {
                    if (e.getUpdateOrigin().equals(KnowledgeOrigin.Derived))
                        e.setUpdateOrigin(KnowledgeOrigin.Indirect);
                    e.setParent(target);
                    e.setChild(source.get());
                });
            } else {
                var edges = this.edges.current();
                var e1 = new KEdge(source.get(), target);
                e1.setUpdatedAt(new Date());
                e1.setUpdateOrigin(KnowledgeOrigin.Indirect);
                edges.add(e1);

                var e2 = new KEdge(target, source.get());
                e2.setUpdatedAt(new Date());
                e2.setUpdateOrigin(KnowledgeOrigin.Indirect);
                edges.add(e2);
                this.edges.setCurrent(edges);
            }
        });
    }

    public void upsertAsset(SoftwareAsset asset, Node parent) {
        var entry = this.assets.stream().filter(a -> a.getID().equals(asset.getID()))
                .findFirst();
        var index = entry.map(this.assets::indexOf);

        if (index.isEmpty()) this.assets.add(new KSoftwareAsset(asset, parent));
        else this.assets.set(index.get(), new KSoftwareAsset(asset, parent)); // TODO: Maybe merge the data
    }

    @Override
    public void markAsDirty(Node node) {

    }

    @Override
    public void markAsDirty(String nodeId) {

    }

    @Override
    public SubscribableEvent<KnowledgeEntry> onKnowledgeUpdate() {
        return this.knowledgeEvent.subscribable;
    }

    @Override
    public List<KSoftwareAsset> getAssets() {
        return this.assets;
    }

    private int findNodeIndex(IIdentifiable node) {
        var nodes = this.nodes.current();
        for (int i = 0; i < nodes.size(); i++) {
            if (node.getID().equals(nodes.get(i).getID())) return i;
        }
        return -1;
    }

    @Override
    public List<KnowledgeEntry> getChildren(String nodeId) {
        var children = super.getChildren(nodeId);
        children.addAll(this.assets);
        return children;
    }


}
