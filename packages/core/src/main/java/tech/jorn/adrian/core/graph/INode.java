package tech.jorn.adrian.core.graph;

import tech.jorn.adrian.core.IIdentifiable;
import tech.jorn.adrian.core.properties.IProperty;
import java.util.List;
import java.util.Optional;

public interface INode extends IIdentifiable {

    <P extends IProperty<?>> List<P> getProperties();
    public <T> Optional<T> getProperty(String key);
}
