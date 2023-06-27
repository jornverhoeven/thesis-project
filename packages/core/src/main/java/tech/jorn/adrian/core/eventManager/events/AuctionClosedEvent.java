package tech.jorn.adrian.core.eventManager.events;

import tech.jorn.adrian.core.Auction;
import tech.jorn.adrian.core.AuctionProposal;

import java.util.Date;
import java.util.Optional;

public class AuctionClosedEvent extends Event {
    private final Auction auction;
    private final Optional<AuctionProposal> selectedProposal;

    public AuctionClosedEvent(Auction auction, Optional<AuctionProposal> selectedProposal) {
        super(new Date());
        this.auction = auction;
        this.selectedProposal = selectedProposal;
    }

    public Auction getAuction() {
        return auction;
    }

    public Optional<AuctionProposal> getSelectedProposal() {
        return selectedProposal;
    }
}
