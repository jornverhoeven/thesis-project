package tech.jorn.adrian.agent.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.jorn.adrian.agent.events.*;
import tech.jorn.adrian.core.controllers.AbstractController;
import tech.jorn.adrian.core.controllers.AbstractEventController;
import tech.jorn.adrian.core.events.EventManager;
import tech.jorn.adrian.core.services.proposals.ProposalManager;

public class ProposalController extends AbstractController {
    Logger log = LogManager.getLogger(ProposalController.class);

    private final ProposalManager proposalManager;

    public ProposalController(ProposalManager proposalManager, EventManager eventManager) {
        super(eventManager);

        this.proposalManager = proposalManager;

        // TODO: Probably also implement some event for selecting proposals?
        this.eventManager.registerEventHandler(SearchForProposalEvent.class, this::searchForProposal);
        this.eventManager.registerEventHandler(ApplyProposalEvent.class, this::applyProposal);
    }

    protected void searchForProposal(SearchForProposalEvent event) {
        var proposals = this.proposalManager.findProposals(event.getAuction());
        var proposal = this.proposalManager.selectProposal(proposals);

        proposals.forEach(p -> eventManager.emit(new FoundProposalEvent(p)));
        proposal.ifPresentOrElse(
                p -> eventManager.emit(new SelectedProposalEvent(p)),
                () -> eventManager.emit(new CancelProposalEvent(event.getAuction()))
        );
    }

    protected void applyProposal(ApplyProposalEvent event) {
        this.log.debug("Applying proposal: {}", event.getProposal().mutation().toString());
        proposalManager.applyProposal(event.getProposal());
    }
}
