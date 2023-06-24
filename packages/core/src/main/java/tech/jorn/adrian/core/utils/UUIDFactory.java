package tech.jorn.adrian.core.utils;

import java.util.UUID;

public class UUIDFactory implements IDFactory {
    @Override
    public String getID() {
        return UUID.randomUUID().toString();
    }
}
