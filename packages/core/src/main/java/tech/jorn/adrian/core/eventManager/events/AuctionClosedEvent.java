package tech.jorn.adrian.core.eventManager.events;

import tech.jorn.adrian.core.Auction;
import tech.jorn.adrian.core.AuctionProposal;

import java.util.Date;
import java.util.Optional;

public class AuctionClosedEvent extends Event {
    public AuctionClosedEvent(Auction auction, Optional<AuctionProposal> selectedProposal) {
        super(new Date());
    }
}
