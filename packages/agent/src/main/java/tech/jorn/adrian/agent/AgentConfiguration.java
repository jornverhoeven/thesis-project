package tech.jorn.adrian.agent;

import tech.jorn.adrian.core.infrastructure.Node;

import java.util.ArrayList;
import java.util.List;

public class AgentConfiguration {
    private final Node parentNode;
    private final List<Node> upstreamNodes;
    private final int auctionDuration = 10000;

    public AgentConfiguration(Node parentNode) {
        this(parentNode, new ArrayList<>());
    }
    public AgentConfiguration(Node parentNode, List<Node> upstreamNodes) {
        this.parentNode = parentNode;
        this.upstreamNodes = upstreamNodes;
    }

    public Node getParentNode() {
        return parentNode;
    }

    public List<Node> getUpstreamNodes() {
        return upstreamNodes;
    }

    public int getAuctionDuration() {
        return this.auctionDuration;
    }
}
