package tech.jorn.adrian.experiment.instruments;

import tech.jorn.adrian.agent.events.*;
import tech.jorn.adrian.core.agents.IAgentConfiguration;
import tech.jorn.adrian.core.auction.Auction;
import tech.jorn.adrian.core.controllers.AbstractController;
import tech.jorn.adrian.core.events.Event;
import tech.jorn.adrian.core.events.EventManager;
import tech.jorn.adrian.core.services.IDGenerator;

import java.util.ArrayList;

public class ProposalImplementationController extends AbstractController {

    private final IAgentConfiguration configuration;

    public ProposalImplementationController(EventManager eventManager, IAgentConfiguration configuration) {
        super(eventManager);
        this.configuration = configuration;

        this.registerEvents();
    }

    private void registerEvents() {
        this.eventManager.registerEventHandler(JoinAuctionRequestEvent.class, this::doNothing);
        this.eventManager.registerEventHandler(JoinAuctionAcceptEvent.class, this::doNothing);
        this.eventManager.registerEventHandler(JoinAuctionRejectEvent.class, this::doNothing);

        this.eventManager.registerEventHandler(AuctionBidEvent.class, this::doNothing);
        this.eventManager.registerEventHandler(CancelProposalEvent.class, this::doNothing);
        this.eventManager.registerEventHandler(AuctionFinalizedEvent.class, this::doNothing);
        this.eventManager.registerEventHandler(AuctionCancelledEvent.class, this::doNothing);

        this.eventManager.registerEventHandler(InitiateAuctionEvent.class, this::initiateAuction);
        this.eventManager.registerEventHandler(SelectedProposalEvent.class, this::selectedProposal);
    }

    public void doNothing(Event e) {
    }

    public void initiateAuction(InitiateAuctionEvent event) {
        var auction = new Auction(new IDGenerator().getID(), this.configuration.getParentNode(), new ArrayList<>(), event.getReport());
        this.eventManager.emit(new SearchForProposalEvent(auction));
    }
    public void selectedProposal(SelectedProposalEvent event) {
        this.eventManager.emit(new ApplyProposalEvent(event.getProposal()));
    }
}
