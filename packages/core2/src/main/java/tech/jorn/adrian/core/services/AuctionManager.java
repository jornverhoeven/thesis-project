package tech.jorn.adrian.core.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.jorn.adrian.agent.events.*;
import tech.jorn.adrian.core.agents.IAgentConfiguration;
import tech.jorn.adrian.core.auction.Auction;
import tech.jorn.adrian.core.auction.AuctionProposal;
import tech.jorn.adrian.agent.events.AuctionCancelled;
import tech.jorn.adrian.agent.events.AuctionFinalizedEvent;
import tech.jorn.adrian.core.events.EventManager;
import tech.jorn.adrian.core.graphs.base.INode;
import tech.jorn.adrian.core.graphs.risks.AttackGraphNode;
import tech.jorn.adrian.core.messages.EventMessage;
import tech.jorn.adrian.core.messages.MessageBroker;
import tech.jorn.adrian.core.observables.SubscribableValueEvent;
import tech.jorn.adrian.core.observables.ValueDispatcher;
import tech.jorn.adrian.core.risks.RiskReport;
import tech.jorn.adrian.core.services.proposals.IProposalSelector;

import java.util.*;
import java.util.stream.Collectors;

public class AuctionManager {
    Logger log = LogManager.getLogger(AuctionManager.class);

    private final MessageBroker messageBroker;
    private final EventManager eventManager;
    private final IProposalSelector proposalSelector;
    private final IAgentConfiguration configuration;
    private TimerTask timeout;
    private Map<INode, AuctionProposal> proposals = new HashMap<>();
    private List<INode> participants = new ArrayList<>();

    public AuctionManager(MessageBroker messageBroker, EventManager eventManager, IProposalSelector proposalSelector, IAgentConfiguration configuration) {
        this.messageBroker = messageBroker;
        this.eventManager = eventManager;
        this.proposalSelector = proposalSelector;
        this.configuration = configuration;
    }

    private final ValueDispatcher<Auction> auction = new ValueDispatcher<>(null);

    public Auction startAuction(RiskReport riskReport) {
        var auction = new Auction(new IDGenerator().getID(), this.configuration.getParentNode(), new ArrayList<>(), riskReport);

        var timer = new Timer();
        var manager = this;
        if (this.timeout != null) this.timeout.cancel();
        this.timeout = new TimerTask() {
            @Override
            public void run() {
                manager.finalizeAuction(auction);
            }
        };
        timer.schedule(this.timeout, this.configuration.getAuctionTimeout());

        riskReport.graph().getNodes().forEach(node -> {
            if (!(node instanceof AttackGraphNode)) return;
            if (node.getID().equals(this.configuration.getNodeID())) return;
            this.messageBroker.send(node, new EventMessage<>(new JoinAuctionRequestEvent(auction)));
            this.participants.add(node);
        });

        this.auction.setCurrent(auction);
        this.eventManager.emit(new SearchForProposalEvent(auction));
        this.participants.add(this.configuration.getParentNode());
        return auction;
    }

    public void joinAuction(Auction auction) {
        var event = new JoinAuctionAcceptEvent(auction.getHost(), auction);
        this.messageBroker.send(auction.getHost(), new EventMessage<>(event));

        this.auction.setCurrent(auction);
        this.eventManager.emit(new SearchForProposalEvent(auction));
    }

    public void rejectAuction(Auction auction) {
        var event = new JoinAuctionRejectEvent(auction.getHost(), auction);
        this.messageBroker.send(auction.getHost(), new EventMessage<>(event));
    }

    public void receiveProposal(AuctionProposal proposal, INode participant) {
        var auction = this.auction.current();
        if (!auction.getId().equals(proposal.auction().getId())) {
            this.log.warn("Should not have received a proposal when not the auctioneer node");
            return;
        }

        if (proposal.mutation() == null) {
            this.log.info("Node {} did not send a proposal", participant.getID());
        } else {
            this.log.info("Received proposal {} {} from {}", proposal.mutation().getCosts(), proposal.newDamage(), participant.getID());
        }

        this.proposals.put(participant, proposal);

        if (this.isSaturated()) {
            this.finalizeAuction(auction);
        }
    }

    public void bidProposal(AuctionProposal proposal) {
        var event = new AuctionBidEvent(this.configuration.getParentNode(), proposal);
        this.messageBroker.send(proposal.auction().getHost(), new EventMessage<>(event));
    }

    public void cancelProposal(Auction auction) {
        var event = new AuctionBidEvent(this.configuration.getParentNode(), new AuctionProposal(this.configuration.getParentNode(), auction, null, Float.MAX_VALUE));
        this.messageBroker.send(auction.getHost(), new EventMessage<>(event));
    }

    public void onAuctionJoined(Auction auction, INode participant) {

    }

    public void onAuctionRejected(Auction auction, INode participant) {
        this.log.debug("Agent {} rejected auction", participant.getID());
        this.participants.remove(participant);
        // TODO: If all nodes have rejected, finalize
    }

    private void finalizeAuction(Auction auction) {
        this.log.info("Finalizing auction {}", auction.getId());
        this.log.debug("Received proposals from: \n{}", proposals.values()
                .stream()
                .map(p -> p.mutation() != null
                        ? String.format("- proposal from %s: reducing to %.2f by %s", p.origin().getID(), p.newDamage(), p.mutation())
                        : String.format("- proposal from %s: nothing", p.origin().getID()))
                .collect(Collectors.joining("\n")));
        this.timeout.cancel();

        var proposal = this.proposalSelector.select(proposals.values().stream().toList());
        if (proposal.isEmpty()) {
            this.participants.forEach(node -> this.messageBroker.send(node, new EventMessage<>(new AuctionCancelled(auction))));
            return;
        }

        this.log.info("Selected proposal from {}: reducing to {} by {}",
                proposal.get().origin().getID(),
                proposal.get().newDamage(),
                proposal.get().mutation().toString()
        );

        this.participants.forEach(node -> {
            if (node.equals(this.configuration.getNodeID())) return;
            // TODO: If no proposal was sent, we might not want to sent this event
            var event = new EventMessage<>(new AuctionFinalizedEvent(auction, proposal.get()));
            this.messageBroker.send(node, event);
        });

        this.reset();
    }

    public boolean isAuctioning() {
        return this.auction.current() == null;
    }

    public SubscribableValueEvent<Auction> onAuctionChanged() {
        return this.auction.subscribable;
    }

    private boolean isSaturated() {
        return this.proposals.size() == participants.size()
                && proposals.values().stream().allMatch(Objects::nonNull);
    }

    public void reset() {
        this.auction.setCurrent(null);
        this.proposals = new HashMap<>();
        this.participants = new ArrayList<>();
        this.log.debug("reset auctioning state");
    }
}
