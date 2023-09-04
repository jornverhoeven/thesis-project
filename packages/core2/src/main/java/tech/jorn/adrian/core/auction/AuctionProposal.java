package tech.jorn.adrian.core.auction;

import tech.jorn.adrian.core.graphs.base.INode;
import tech.jorn.adrian.core.mutations.Mutation;

public record AuctionProposal(INode origin, Auction auction, Mutation mutation, float newDamage) {
}
