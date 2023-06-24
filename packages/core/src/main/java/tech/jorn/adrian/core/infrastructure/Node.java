package tech.jorn.adrian.core.infrastructure;

import tech.jorn.adrian.core.assets.SoftwareAsset;
import tech.jorn.adrian.core.graph.INode;
import tech.jorn.adrian.core.properties.NodeProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Node implements INode {
    private String id;
    private String name;

    List<SoftwareAsset> softwareAssets = new ArrayList<>();
    List<NodeProperty<?>> properties = new ArrayList<>();

    public Node(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public Node(String id) {
        this.id = id;
        this.name = "Node " + this.id;
    }

    @Override
    public String getID() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public Node addSoftwareAsset(SoftwareAsset softwareAsset) {
        this.softwareAssets.add(softwareAsset);
        return this;
    }

    public List<SoftwareAsset> getSoftwareAssets() {
        return this.softwareAssets;
    }

    public List<NodeProperty<?>> getProperties() {
        return this.properties;
    }

    @Override
    public <T> Optional<T> getProperty(String key) {
        return this.properties.stream()
                .filter(p -> p.getName().equals(key))
                .map(p -> (T) p.getValue())
                .findFirst();
    }
}
