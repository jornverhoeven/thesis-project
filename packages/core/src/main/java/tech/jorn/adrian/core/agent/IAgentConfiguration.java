package tech.jorn.adrian.core.agent;

import tech.jorn.adrian.core.graph.INode;
import tech.jorn.adrian.core.infrastructure.Node;

import java.util.List;

public interface IAgentConfiguration {
    public Node getParentNode();
    public List<INode> getUpstreamNodes();
    public int getAuctionTimeout();
}
