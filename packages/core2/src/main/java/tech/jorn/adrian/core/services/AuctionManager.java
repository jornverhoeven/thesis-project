package tech.jorn.adrian.core.services;

import tech.jorn.adrian.agent.events.JoinAuctionAcceptEvent;
import tech.jorn.adrian.agent.events.JoinAuctionRequestEvent;
import tech.jorn.adrian.core.auction.Auction;
import tech.jorn.adrian.core.auction.AuctionProposal;
import tech.jorn.adrian.core.graphs.base.INode;
import tech.jorn.adrian.core.messages.EventMessage;
import tech.jorn.adrian.core.messages.MessageBroker;
import tech.jorn.adrian.core.observables.SubscribableValueEvent;
import tech.jorn.adrian.core.observables.ValueDispatcher;

public class AuctionManager {
    private final MessageBroker messageBroker;

    public AuctionManager(MessageBroker messageBroker) {
        this.messageBroker = messageBroker;
    }

    private final ValueDispatcher<Auction> auction = new ValueDispatcher<>(null);

    public Auction startAuction() {
        var auction = new Auction("", null, null, null);
        this.messageBroker.broadcast(new EventMessage<>(new JoinAuctionRequestEvent(auction)));
        this.auction.setCurrent(auction);
        return auction;
    }

    public void joinAuction(Auction auction) {
        var event = new JoinAuctionAcceptEvent(auction.getHost(), auction);
        this.messageBroker.send(auction.getHost(), new EventMessage<>(event));

        this.auction.setCurrent(auction);
    }

    public void rejectAuction(Auction auction) {
        var event = new JoinAuctionAcceptEvent(auction.getHost(), auction);
        this.messageBroker.send(auction.getHost(), new EventMessage<>(event));
    }

    public void receiveProposal(AuctionProposal proposal) {

    }

    public void onAuctionJoined(Auction auction, INode participant) {

    }
    public void onAuctionRejected(Auction auction, INode participant) {

    }

    private void finalizeAuction() {

    }

    public boolean isAuctioning() {
        return this.auction.current() != null;
    }

    public SubscribableValueEvent<Auction> onAuctionChanged() {
        return this.auction.subscribable;
    }
}
