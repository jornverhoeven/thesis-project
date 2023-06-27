package tech.jorn.adrian.core.infrastructure;

import tech.jorn.adrian.core.assets.SoftwareAsset;
import tech.jorn.adrian.core.graph.AbstractNode;
import tech.jorn.adrian.core.graph.INode;
import tech.jorn.adrian.core.properties.NodeProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Node extends AbstractNode<NodeProperty<?>> implements INode {
    List<SoftwareAsset> softwareAssets = new ArrayList<>();

    public Node(String id, String name) {
        super(id, name, new ArrayList<>());
    }

    public Node(String id) {
        super(id, "Node " + id, new ArrayList<>());
    }

    public Node addSoftwareAsset(SoftwareAsset softwareAsset) {
        this.softwareAssets.add(softwareAsset);
        return this;
    }

    public List<SoftwareAsset> getSoftwareAssets() {
        return this.softwareAssets;
    }
    
}
