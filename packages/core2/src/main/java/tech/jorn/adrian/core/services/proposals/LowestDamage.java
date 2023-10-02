package tech.jorn.adrian.core.services.proposals;

import tech.jorn.adrian.core.auction.AuctionProposal;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class LowestDamage implements IProposalSelector {

    private final float threshold;

    public LowestDamage(float threshold) {
        this.threshold = threshold;
    }

    @Override
    public Optional<AuctionProposal> select(List<AuctionProposal> proposals, float threshold) {
        return proposals.stream()
                .filter(proposal -> proposal != null && proposal.newDamage() < this.threshold && proposal.newDamage() < threshold)
                .min(Comparator.comparingDouble(AuctionProposal::newDamage));
    }
}
