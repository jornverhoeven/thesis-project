package tech.jorn.adrian.core.eventManager.events;

import tech.jorn.adrian.core.NodeLink;
import tech.jorn.adrian.core.infrastructure.Node;

import java.util.Date;
import java.util.List;


public class ShareKnowledgeEvent extends Event {
    private final Node node;
    private final List<NodeLink> links;
    private final int distance;

    public ShareKnowledgeEvent(Node node, List<NodeLink> links, int distance) {
        this(node, links, distance, new Date());
    }
    public ShareKnowledgeEvent(Node node, List<NodeLink> links, int distance, Date time) {
        super(time);
        this.node = node;
        this.links = links;
        this.distance = distance;
    }

    public Node getNode() {
        return node;
    }
    public List<NodeLink> getLinks() { return this.links; }

    public int getDistance() {
        return distance;
    }
}

