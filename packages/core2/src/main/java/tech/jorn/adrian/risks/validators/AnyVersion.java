package tech.jorn.adrian.risks.validators;

import tech.jorn.adrian.core.properties.AbstractProperty;

public class AnyVersion implements PropertyValidator<String> {
    public AnyVersion() {

    }

    @Override
    public boolean validate(String property) {
        return !property.isEmpty();
    }
}

