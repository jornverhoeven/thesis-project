package tech.jorn.adrian.core.graphs;

import tech.jorn.adrian.core.graphs.base.AbstractNode;
import tech.jorn.adrian.core.observables.EventDispatcher;
import tech.jorn.adrian.core.observables.SubscribableEvent;
import tech.jorn.adrian.core.properties.AbstractProperty;
import tech.jorn.adrian.core.properties.IDetailed;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class AbstractDetailedNode<P extends AbstractProperty<?>> extends AbstractNode implements IDetailed<P> {
    private final Map<String, P> properties;
    private final EventDispatcher<P> onPropertyChange = new EventDispatcher<>();

    public AbstractDetailedNode(String id) {
        super(id);
        this.properties = new HashMap<>();
    }

    public Map<String, P> getProperties() {
        return properties;
    }

    @Override
    public <T> Optional<T> getProperty(String property) {
        if (this.properties.containsKey(property))
            return Optional.of((T) this.properties.get(property).getValue());
        return Optional.empty();
    }

    @Override
    public <T> void setProperty(String property, T value) {
        var p = this.makeProperty(property, value);
        this.properties.put(property, p);
        this.onPropertyChange.dispatch(p);
    }

    protected abstract <T> P makeProperty(String property, T value);

    @Override
    public SubscribableEvent<P> onPropertyChange() {
        return this.onPropertyChange.subscribable;
    }
}
