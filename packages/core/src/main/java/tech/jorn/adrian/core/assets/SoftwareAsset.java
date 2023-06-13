package tech.jorn.adrian.core.assets;

import tech.jorn.adrian.core.IIdentifiable;
import tech.jorn.adrian.core.graph.AbstractNode;
import tech.jorn.adrian.core.graph.INode;
import tech.jorn.adrian.core.observables.SubscribableEvent;
import tech.jorn.adrian.core.observables.SubscribableValueEvent;
import tech.jorn.adrian.core.observables.ValueDispatcher;
import tech.jorn.adrian.core.properties.SoftwareProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class SoftwareAsset extends AbstractNode<SoftwareProperty<?>> {

    public SoftwareAsset(String id) {
        this(id, "Software Asset " + id);
    }
    public SoftwareAsset(String id, String name) {
        super(id, name, new ArrayList<>());
    }

    public void setProperties(Map<String, Object> properties) {
        var props = this.properties;
        properties.forEach((key, value) -> {
            props.add(new SoftwareProperty<>(key, value));
        });
    }
}
