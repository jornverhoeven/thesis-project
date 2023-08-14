package tech.jorn.adrian.agent;

import tech.jorn.adrian.core.agent.IAgentConfiguration;
import tech.jorn.adrian.core.assets.SoftwareAsset;
import tech.jorn.adrian.core.graph.INode;
import tech.jorn.adrian.core.infrastructure.Node;

import java.util.ArrayList;
import java.util.List;

public class AgentConfiguration implements IAgentConfiguration {
    private final Node parentNode;
    private final List<INode> upstreamNodes;
    private final int auctionTimeout;

    public AgentConfiguration(Node parentNode) {
        this(parentNode, new ArrayList<>(), 10000);
    }
    public AgentConfiguration(Node parentNode, List<INode> upstreamNodes, int auctionTimeout) {
        this.parentNode = parentNode;
        this.upstreamNodes = upstreamNodes;
        this.auctionTimeout = auctionTimeout;
    }

    public Node getParentNode() {
        return parentNode;
    }

    public List<INode> getUpstreamNodes() {
        return upstreamNodes;
    }

    public int getAuctionTimeout() {
        return this.auctionTimeout;
    }
}
