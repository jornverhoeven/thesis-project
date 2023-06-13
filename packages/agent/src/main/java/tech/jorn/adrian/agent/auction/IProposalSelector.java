package tech.jorn.adrian.agent.auction;

import tech.jorn.adrian.agent.AdrianAgent;
import tech.jorn.adrian.core.AuctionProposal;

import java.util.Optional;

public interface IProposalSelector {
    // TODO: Maybe instead of the Agent we can just use the Knowledge base
    Optional<AuctionProposal> select(AdrianAgent agent, Auctioneer auction);
}
