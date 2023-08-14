package tech.jorn.adrian.agent.controllers;

import tech.jorn.adrian.agent.events.SearchForProposalEvent;
import tech.jorn.adrian.core.controllers.AbstractEventController;
import tech.jorn.adrian.core.events.EventManager;

public class ProposalController extends AbstractEventController<SearchForProposalEvent> {
    public ProposalController(EventManager eventManager) {
        super(SearchForProposalEvent.class, eventManager);

        // TODO: Probably also implement some event for selecting proposals?
    }

    @Override
    protected void processEvent(SearchForProposalEvent event) {

    }
}

