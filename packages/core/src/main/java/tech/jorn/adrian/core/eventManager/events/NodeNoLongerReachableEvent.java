package tech.jorn.adrian.core.eventManager.events;

import java.util.Date;

public class NodeNoLongerReachableEvent extends Event {

    private final String nodeId;

    public NodeNoLongerReachableEvent(String nodeId) {
        this(nodeId, new Date());
    }
    public NodeNoLongerReachableEvent(String nodeId, Date time) {
        super(time);
        this.nodeId = nodeId;
    }
}
