package tech.jorn.adrian.core.eventManager.events;

import tech.jorn.adrian.core.Auction;
import tech.jorn.adrian.core.IIdentifiable;

import java.util.Date;

public class JoinAuctionAcceptEvent extends Event {
    private final IIdentifiable origin;
    private final Auction auction;

    public JoinAuctionAcceptEvent(IIdentifiable origin, Auction auction) {
        super(new Date());
        this.origin = origin;
        this.auction = auction;
    }

    public IIdentifiable getOrigin() { return this.origin; }
    public Auction getAuction() {
        return this.auction;
    }
}
