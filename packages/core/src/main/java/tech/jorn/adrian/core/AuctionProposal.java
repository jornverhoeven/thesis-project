package tech.jorn.adrian.core;

public class AuctionProposal {
    private final IIdentifiable origin;

    public AuctionProposal(IIdentifiable origin, Auction auction, Object proposal) {
        this.origin = origin;
    }

    public IIdentifiable getOrigin() {
        return this.origin;
    }

}
