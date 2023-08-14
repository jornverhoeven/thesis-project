package tech.jorn.adrian.agent.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.jorn.adrian.agent.events.*;
import tech.jorn.adrian.core.controllers.AbstractController;
import tech.jorn.adrian.core.events.EventManager;
import tech.jorn.adrian.core.services.AuctionManager;

public class AuctionController extends AbstractController {
    Logger log = LogManager.getLogger(AuctionController.class);
    private final AuctionManager auctionManager;

    public AuctionController(AuctionManager auctionManager, EventManager eventManager) {
        super(eventManager);
        this.auctionManager = auctionManager;
        this.registerEvents();
    }

    private void registerEvents() {
        this.eventManager.registerEventHandler(InitiateAuctionEvent.class, this::initiateAuction);
        this.eventManager.registerEventHandler(JoinAuctionRequestEvent.class, this::joinAuctionRequest);
        this.eventManager.registerEventHandler(JoinAuctionAcceptEvent.class, this::joinAuctionAccept);
        this.eventManager.registerEventHandler(JoinAuctionRejectEvent.class, this::joinAuctionReject);
    }

    private void initiateAuction(InitiateAuctionEvent event) {
        this.auctionManager.startAuction();
    }

    private void joinAuctionRequest(JoinAuctionRequestEvent event) {
        boolean shouldJoin = this.auctionManager.isAuctioning();
        if (shouldJoin) this.auctionManager.joinAuction(event.getAuction());
        else this.auctionManager.rejectAuction(event.getAuction());
    }

    private void joinAuctionAccept(JoinAuctionAcceptEvent event) {
        this.auctionManager.onAuctionJoined(event.getAuction(), event.getOrigin());
    }

    private void joinAuctionReject(JoinAuctionRejectEvent event) {
        this.auctionManager.onAuctionRejected(event.getAuction(), event.getOrigin());
    }
}
