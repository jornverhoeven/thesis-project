package tech.jorn.adrian.agent.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.jorn.adrian.agent.events.*;
import tech.jorn.adrian.core.agents.AgentState;
import tech.jorn.adrian.core.agents.IAgentConfiguration;
import tech.jorn.adrian.core.auction.Auction;
import tech.jorn.adrian.core.controllers.AbstractController;
import tech.jorn.adrian.agent.events.AuctionFinalizedEvent;
import tech.jorn.adrian.core.events.EventManager;
import tech.jorn.adrian.core.observables.SubscribableValueEvent;
import tech.jorn.adrian.core.services.AuctionManager;

import java.util.List;

public class AuctionController extends AbstractController {
    Logger log = LogManager.getLogger(AuctionController.class);
    private final AuctionManager auctionManager;
    private final IAgentConfiguration configuration;

    public AuctionController(AuctionManager auctionManager, EventManager eventManager, IAgentConfiguration configuration, SubscribableValueEvent<AgentState> agentState) {
        super(eventManager, agentState);
        this.auctionManager = auctionManager;
        this.configuration = configuration;
        this.registerEvents();
    }

    private void registerEvents() {
        this.eventManager.registerEventHandler(InitiateAuctionEvent.class, this::initiateAuction);
        this.eventManager.registerEventHandler(JoinAuctionRequestEvent.class, this::joinAuctionRequest);
        this.eventManager.registerEventHandler(JoinAuctionAcceptEvent.class, this::joinAuctionAccept);
        this.eventManager.registerEventHandler(JoinAuctionRejectEvent.class, this::joinAuctionReject);

        this.eventManager.registerEventHandler(AuctionBidEvent.class, this::receiveAuctionBid);
        this.eventManager.registerEventHandler(SelectedProposalEvent.class, this::selectedProposal);
        this.eventManager.registerEventHandler(CancelProposalEvent.class, this::cancelProposal);
        this.eventManager.registerEventHandler(AuctionFinalizedEvent.class, this::onAuctionFinalized);
        this.eventManager.registerEventHandler(AuctionCancelledEvent.class, this::onAuctionCancelled);
    }

    private void initiateAuction(InitiateAuctionEvent event) {
        if (this.auctionManager.isAuctioning()) {
            this.log.warn("Agent was already participating in an auction");
            return;
        }
        this.auctionManager.startAuction(event.getReport());
    }

    private void joinAuctionRequest(JoinAuctionRequestEvent event) {
        boolean canJoin = !this.auctionManager.isAuctioning();
        if (canJoin) this.auctionManager.joinAuction(event.getAuction());
        else this.auctionManager.rejectAuction(event.getAuction());
    }

    private void joinAuctionAccept(JoinAuctionAcceptEvent event) {
        this.auctionManager.onAuctionJoined(event.getAuction(), event.getOrigin());
    }

    private void joinAuctionReject(JoinAuctionRejectEvent event) {
        this.auctionManager.onAuctionRejected(event.getAuction(), event.getOrigin());
    }

    private void receiveAuctionBid(AuctionBidEvent event) {
        this.auctionManager.receiveProposal(event.getProposal(), event.getOrigin());
    }

    private void selectedProposal(SelectedProposalEvent event) {
        this.auctionManager.bidProposal(event.getProposal());
    }
    private void cancelProposal(CancelProposalEvent event) {
        this.auctionManager.cancelProposal(event.getAuction());
    }

    private void onAuctionFinalized(AuctionFinalizedEvent event) {
        // TODO: Remove auction from AuctionManager so we are able to join another auction
        this.auctionManager.reset();
        // If we are not the one on the proposal ignore it
        if (!event.getProposal().origin().equals(this.configuration.getParentNode())) return;
        this.eventManager.emit(new ApplyProposalEvent(event.getProposal()));
    }
    private void onAuctionCancelled(AuctionCancelledEvent e) {
        this.auctionManager.reset();
    }
}
