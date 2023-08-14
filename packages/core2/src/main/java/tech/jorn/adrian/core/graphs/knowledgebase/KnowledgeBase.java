package tech.jorn.adrian.core.graphs.knowledgebase;

import tech.jorn.adrian.agent.events.ShareKnowledgeEvent;
import tech.jorn.adrian.core.graphs.AbstractDetailedNode;
import tech.jorn.adrian.core.graphs.base.AbstractGraph;
import tech.jorn.adrian.core.graphs.base.GraphLink;

import java.util.List;
import java.util.Optional;

public class KnowledgeBase extends AbstractGraph<KnowledgeBaseEntry<?>, GraphLink<KnowledgeBaseEntry<?>>> {
    public KnowledgeBase() {
        super();
    }

    public void processNewInformation(AbstractDetailedNode<?> origin, List<String> neighbours) {
        var node = this.findById(origin.getID());
        if (node.isEmpty()) {
            // Create a new node in the knowledgebase.
            var knowledge = new KnowledgeBaseNode(origin.getID());
            origin.getProperties()
                    .forEach(knowledge::setProperty);
            node = Optional.of(knowledge);
        }

        this.upsertNode(node.get()
                .setKnowledgeOrigin(KnowledgeOrigin.INDIRECT)
        );

        for (var id: neighbours) {
            var adjacent = this.findById(id);
            if (adjacent.isEmpty()) {
                var knowledge = new KnowledgeBaseNode(id)
                        .setKnowledgeOrigin(KnowledgeOrigin.INFERRED);
                adjacent = Optional.of(knowledge);
                this.upsertNode(knowledge);
            }
            this.addEdge(node.get(), adjacent.get());
        }
    }
}
