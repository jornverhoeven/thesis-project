package tech.jorn.adrian.agent.events;

import tech.jorn.adrian.core.events.Event;
import tech.jorn.adrian.core.graphs.AbstractDetailedNode;
import tech.jorn.adrian.core.properties.NodeProperty;

import java.util.List;

public class ShareKnowledgeEvent extends Event  {

    private final AbstractDetailedNode<NodeProperty<?>>  origin;
    private final List<String> links;
    private final int distance;

    public ShareKnowledgeEvent(AbstractDetailedNode<NodeProperty<?>> origin, List<String> links, int distance) {
        this.origin = origin;
        this.links = links;
        this.distance = distance;
    }

    public AbstractDetailedNode<NodeProperty<?>>  getOrigin() {
        return origin;
    }

    public List<String> getLinks() {
        return links;
    }

    public int getDistance() {
        return distance;
    }

    public static ShareKnowledgeEvent reducedDistance(ShareKnowledgeEvent event) {
        return new ShareKnowledgeEvent(event.getOrigin(), event.getLinks(), event.getDistance() - 1);
    }
}


