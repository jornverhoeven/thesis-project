package tech.jorn.adrian.agent.auction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.jorn.adrian.agent.AdrianAgent;
import tech.jorn.adrian.core.Auction;
import tech.jorn.adrian.core.AuctionProposal;
import tech.jorn.adrian.core.IIdentifiable;
import tech.jorn.adrian.core.eventManager.events.*;
import tech.jorn.adrian.core.messaging.IMessageBroker;
import tech.jorn.adrian.core.observables.SubscribableValueEvent;
import tech.jorn.adrian.core.observables.ValueDispatcher;
import tech.jorn.adrian.core.risks.detection.RiskReport;
import tech.jorn.adrian.core.risks.graph.RiskHardwareNode;
import tech.jorn.adrian.core.utils.IDFactory;
import tech.jorn.adrian.core.utils.UUIDFactory;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class AuctionManager {
    private final Logger log = LogManager.getLogger(AuctionManager.class);
    private final AdrianAgent agent;
    private final IMessageBroker broker;
    private final IDFactory idFactory;
    private final IProposalSelector proposalSelector;

    private final ValueDispatcher<Auction> auction = new ValueDispatcher<>(null);
    private TimerTask timeout = null;

    public AuctionManager(AdrianAgent agent, IMessageBroker broker) {
        this(agent, broker, new UUIDFactory(), new BasicProposalSelector());
    }

    public AuctionManager(AdrianAgent agent, IMessageBroker broker, IDFactory idFactory, IProposalSelector proposalSelector) {
        this.agent = agent;
        this.broker = broker;
        this.idFactory = idFactory;
        this.proposalSelector = proposalSelector;
    }

    public Auctioneer initiateAuction(RiskReport risk) {
        var participants = risk.getPath().stream()
                .filter(n -> n instanceof RiskHardwareNode)
                .map(n -> (IIdentifiable) n)
                .filter(n -> !n.getID().equals(this.agent.getConfiguration().getParentNode().getID())) // TODO: Make the getParentNode.getID() easier to access
                .toList();
        var auction = new Auctioneer(idFactory.getID(), this.agent.getConfiguration().getParentNode(), participants, risk);
        this.auction.setCurrent(auction);

        var timer = new Timer();
        var manager = this;
        if (this.timeout != null) this.timeout.cancel();
        this.timeout = new TimerTask() {
            @Override
            public void run() {
                manager.finalizeAuction(auction);
            }
        };
        timer.schedule(this.timeout, this.agent.getConfiguration().getAuctionDuration());

        participants.forEach(p -> this.broker.send(p, new JoinAuctionRequestEvent(auction)));

        return auction;
    }

    public Participant joinAuction(Auction auction) {
        log.info("{} is joining auction for {} ({})", this.agent.getConfiguration().getParentNode().getName(), auction.getHost().getName(), auction.getId());

        this.broker.send(auction.getHost(), new JoinAuctionAcceptEvent(
                this.agent.getConfiguration().getParentNode(),
                auction
        ));
        this.auction.setCurrent(auction);

        // TODO: Start proposing things
        var proposal = new AuctionProposal(this.agent.getConfiguration().getParentNode(), auction, new Object());
        this.broker.send(auction.getHost(), new AuctionProposalEvent(proposal));

        return new Participant(auction, this.agent);
    }

    public void rejectAuction(Auction auction) {
        log.info("{} is rejecting auction {}", this.agent.getConfiguration().getParentNode().getName(), auction.getId());
        this.broker.send(auction.getHost(), new JoinAuctionRejectEvent(auction));
        return;
    }

    public void receiveProposal(AuctionProposal proposal) {
        var auction = this.auction.current();
        if (!(auction instanceof Auctioneer auctioneer)) {
            log.warn("Should not have received a proposal when not the auctioneer node");
            return;
        }

        auctioneer.getProposals().put(proposal.getOrigin(), proposal);
        log.info(auctioneer.getProposals().values()
                .stream()
                .map(p -> String.format("proposal from %s: %s", p.getOrigin().getID(), "---"))
                .toList());
        if (this.isSaturated(auctioneer)) {
            this.finalizeAuction(auctioneer);
        }
    }

    public void auctionJoined(IIdentifiable agent) {
        var auction = this.auction.current();
        if (!(auction instanceof Auctioneer auctioneer)) {
            log.warn("Some agent tried to join the wrong auction");
            return;
        }
        log.info("Agent {} joined the auction {}", agent.getName(), auction.getId());
        // Set the agents proposal to null, indicating that it joined
        auctioneer.getProposals().put(agent, null);
    }

    private void finalizeAuction(Auctioneer auction) {
        log.info("Finalizing auction");
        if (this.timeout != null) {
            log.info("Auction ended in time");
            this.timeout.cancel();
        }

        // TODO: Select proposal
        var proposal = this.proposalSelector.select(this.agent, auction);
        // TODO: Send out messages
        // TODO: Should we sent messages to all nodes, or just the participating nodes.
        auction.getParticipants().forEach(node -> this.broker.send(node, new AuctionClosedEvent(auction, proposal)));
    }

    private boolean isSaturated(Auctioneer auctioneer) {
        return auctioneer.getProposals().size() == auctioneer.getParticipants().size()
                && auctioneer.getProposals().values().stream().allMatch(Objects::nonNull);
    }

    public SubscribableValueEvent<Auction> onAuctionChanged() {
        return this.auction.subscribable;
    }
}
