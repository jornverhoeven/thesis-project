package tech.jorn.adrian.core.graphs.knowledgebase;

import tech.jorn.adrian.agent.events.ShareKnowledgeEvent;
import tech.jorn.adrian.core.graphs.AbstractDetailedNode;
import tech.jorn.adrian.core.graphs.base.AbstractGraph;
import tech.jorn.adrian.core.graphs.base.GraphLink;
import tech.jorn.adrian.core.graphs.infrastructure.SoftwareAsset;
import tech.jorn.adrian.core.properties.NodeProperty;
import tech.jorn.adrian.core.properties.SoftwareProperty;

import java.util.List;
import java.util.Optional;

public class KnowledgeBase extends AbstractGraph<KnowledgeBaseEntry<?>, GraphLink<KnowledgeBaseEntry<?>>> {
    public KnowledgeBase() {
        super();
    }

    public void processNewInformation(AbstractDetailedNode<?> origin, List<String> neighbours, List<SoftwareAsset> assets) {
        var node = this.findById(origin.getID());
        if (node.isEmpty()) {
            // Create a new node in the knowledgebase.
            var knowledge = new KnowledgeBaseNode(origin.getID());
            origin.getProperties()
                    .forEach(knowledge::setFromProperty);
            node = Optional.of(knowledge);
        }

        // TODO: If the node was already direct info, dont set the origin
        this.upsertNode(node.get()
                .setKnowledgeOrigin(KnowledgeOrigin.INDIRECT)
        );

        for (var id : neighbours) {
            var adjacent = this.findById(id);
            if (adjacent.isEmpty()) {
                var knowledge = new KnowledgeBaseNode(id)
                        .setKnowledgeOrigin(KnowledgeOrigin.INFERRED);
                adjacent = Optional.of(knowledge);
                this.upsertNode(knowledge);
            }
            this.addEdge(node.get(), adjacent.get());
            this.addEdge(adjacent.get(), node.get());
        }
        for (var asset : assets) {
            var knowledge = KnowledgeBaseSoftwareAsset.fromNode(asset)
                    .setKnowledgeOrigin(KnowledgeOrigin.INFERRED);
            this.upsertNode(knowledge);
            this.addEdge(node.get(), knowledge);
            this.addEdge(knowledge, node.get());
        }
    }

    public KnowledgeBase clone() {
        var clone = new KnowledgeBase();
        // TODO: copy mutable state here, so the clone can't change the internals of the original
        this.nodes.forEach(node -> {
            try {
                var knowledgeNode = (node instanceof KnowledgeBaseNode)
                        ? KnowledgeBaseNode.fromNode((AbstractDetailedNode<NodeProperty<?>>) node)
                        : KnowledgeBaseSoftwareAsset.fromNode((AbstractDetailedNode<SoftwareProperty<?>>) node);
                if (knowledgeNode != null) clone.upsertNode(knowledgeNode);
            } catch (Exception e) {
                System.out.println("error");
            }
        });
        this.adjacent.forEach((knowledgeBaseEntry, graphLinks) -> {
            var from = clone.findById(knowledgeBaseEntry.getID());
            graphLinks.forEach(link -> {
                var to = clone.findById(link.getNode().getID());
                // TODO: Check the .get()
                clone.addEdge(from.get(), to.get());
            });
        });
        return clone;
    }

}
