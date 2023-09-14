package tech.jorn.adrian.risks.validators;

import tech.jorn.adrian.core.properties.AbstractProperty;

@FunctionalInterface
public interface PropertyValidator<T> {
    boolean validate(T property);
}
