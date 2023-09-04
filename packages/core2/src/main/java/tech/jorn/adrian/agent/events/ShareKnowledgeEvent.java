package tech.jorn.adrian.agent.events;

import tech.jorn.adrian.core.events.Event;
import tech.jorn.adrian.core.graphs.AbstractDetailedNode;
import tech.jorn.adrian.core.graphs.infrastructure.SoftwareAsset;
import tech.jorn.adrian.core.properties.NodeProperty;

import java.util.List;

public class ShareKnowledgeEvent extends Event  {

    private final AbstractDetailedNode<NodeProperty<?>>  origin;
    private final List<String> links;
    private final List<SoftwareAsset> assets;
    private final int distance;

    public ShareKnowledgeEvent(AbstractDetailedNode<NodeProperty<?>> origin, List<String> links, List<SoftwareAsset> assets, int distance) {
        this.origin = origin;
        this.links = links;
        this.assets = assets;
        this.distance = distance;
    }

    public AbstractDetailedNode<NodeProperty<?>>  getOrigin() {
        return origin;
    }

    public List<String> getLinks() {
        return links;
    }

    public List<SoftwareAsset> getAssets() { return assets; }

    public int getDistance() {
        return distance;
    }

    public static ShareKnowledgeEvent reducedDistance(ShareKnowledgeEvent event) {
        return new ShareKnowledgeEvent(event.getOrigin(), event.getLinks(), event.getAssets(), event.getDistance() - 1);
    }
}


