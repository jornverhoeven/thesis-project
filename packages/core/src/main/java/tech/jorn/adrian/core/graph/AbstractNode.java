package tech.jorn.adrian.core.graph;

import tech.jorn.adrian.core.properties.IProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AbstractNode<P extends IProperty<?>> implements INode {
    private final String id;
    private final String name;
    protected List<P> properties = new ArrayList<>();

    public AbstractNode(String id, String name, List<P> properties) {
        this.id = id;
        this.name = name;
        this.properties = properties;
    }

    @Override
    public String getID() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public List<P> getProperties() {
        return this.properties;
    }

    public <T> Optional<T> getProperty(String key) {
        return this.properties.stream()
                .filter(p -> p.getName().equals(key))
                .map(p -> (T) p.getValue())
                .findAny();
    }

    public <T> void setProperty(String key, T value) {
        this.properties.stream()
                .filter(p -> p.getName().equals(key))
                .forEach(p -> ((IProperty<T>) p).setValue(value));
    }
}
