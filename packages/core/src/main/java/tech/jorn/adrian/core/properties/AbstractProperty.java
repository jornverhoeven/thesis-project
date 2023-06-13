package tech.jorn.adrian.core.properties;

public abstract class AbstractProperty<T> implements IProperty<T> {
    protected String name;
    protected T value;

    public AbstractProperty(String name, T value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public T getValue() {
        return this.value;
    }
}
