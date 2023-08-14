package tech.jorn.adrian.core.controllers;

import tech.jorn.adrian.core.events.EventManager;

public abstract class AbstractController implements IController {
    protected final EventManager eventManager;

    public AbstractController(EventManager eventManager) {
        this.eventManager = eventManager;
    }
}
