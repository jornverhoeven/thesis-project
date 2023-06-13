package tech.jorn.adrian.core.eventManager.events;

import tech.jorn.adrian.core.AuctionProposal;

import java.util.Date;

public class AuctionProposalEvent extends Event {
    private final AuctionProposal proposal;

    public AuctionProposalEvent(AuctionProposal proposal) {
        super(new Date());
        this.proposal = proposal;
    }

    public AuctionProposal getProposal() {
        return proposal;
    }
}
