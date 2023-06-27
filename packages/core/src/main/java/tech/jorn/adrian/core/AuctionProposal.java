package tech.jorn.adrian.core;

import tech.jorn.adrian.core.mitigations.MutationResults;

public class AuctionProposal {
    private final IIdentifiable origin;
    private final Auction auction;
    private final MutationResults proposal;

    public AuctionProposal(IIdentifiable origin, Auction auction, MutationResults proposal) {
        this.origin = origin;
        this.auction = auction;
        this.proposal = proposal;
    }

    public IIdentifiable getOrigin() {
        return this.origin;
    }

    public Auction getAuction() {
        return auction;
    }

    public MutationResults getProposal() {
        return proposal;
    }
}
