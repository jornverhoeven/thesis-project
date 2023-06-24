package tech.jorn.adrian.agent.auction;

import tech.jorn.adrian.core.Auction;
import tech.jorn.adrian.core.AuctionProposal;
import tech.jorn.adrian.core.IIdentifiable;
import tech.jorn.adrian.core.risks.detection.RiskReport;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Auctioneer extends Auction {

    private final Map<IIdentifiable, AuctionProposal> proposals;

    public Auctioneer(String id, IIdentifiable host, List<IIdentifiable> participants, RiskReport riskReport) {
        super(id, host, participants, riskReport);
        this.proposals = new HashMap<>();
    }

    public Map<IIdentifiable, AuctionProposal> getProposals() {
        return proposals;
    }
}
