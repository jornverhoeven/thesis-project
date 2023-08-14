package tech.jorn.adrian.core.services;

import java.util.UUID;

public class IDGenerator {
    public String getID() {
        return UUID.randomUUID().toString();
    }
}
