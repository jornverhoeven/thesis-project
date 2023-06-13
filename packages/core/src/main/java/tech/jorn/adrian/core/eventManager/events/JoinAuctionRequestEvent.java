package tech.jorn.adrian.core.eventManager.events;

import tech.jorn.adrian.core.Auction;

import java.util.Date;

public class JoinAuctionRequestEvent extends Event {
    private final Auction auction;

    public JoinAuctionRequestEvent(Auction auction) {
        super(new Date());
        this.auction = auction;
    }

    public Auction getAuction() {
        return this.auction;
    }
}
