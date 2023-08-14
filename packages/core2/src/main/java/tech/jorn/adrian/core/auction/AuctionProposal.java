package tech.jorn.adrian.core.auction;

import tech.jorn.adrian.core.graphs.base.INode;

public record AuctionProposal(INode origin, Auction auction, Mutation mutation) {
}
