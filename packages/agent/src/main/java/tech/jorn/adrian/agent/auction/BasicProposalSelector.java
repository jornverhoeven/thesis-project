package tech.jorn.adrian.agent.auction;

import tech.jorn.adrian.agent.AdrianAgent;
import tech.jorn.adrian.core.AuctionProposal;

import java.util.Optional;

public class BasicProposalSelector implements IProposalSelector {
    @Override
    public Optional<AuctionProposal> select(AdrianAgent agent, Auctioneer auction) {
        return Optional.empty();
    }
}
