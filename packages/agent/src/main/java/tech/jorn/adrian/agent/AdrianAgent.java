package tech.jorn.adrian.agent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.jorn.adrian.agent.auction.AuctionManager;
import tech.jorn.adrian.agent.mitigation.MitigationManager;
import tech.jorn.adrian.core.AuctionProposal;
import tech.jorn.adrian.core.NodeLink;
import tech.jorn.adrian.core.eventManager.events.*;
import tech.jorn.adrian.core.infrastructure.Node;
import tech.jorn.adrian.core.knowledge.IKnowledgeBase;
import tech.jorn.adrian.core.knowledge.KnowledgeOrigin;
import tech.jorn.adrian.core.messaging.IMessageBroker;
import tech.jorn.adrian.core.messaging.MessageResponse;
import tech.jorn.adrian.core.mitigations.AttributeChange;
import tech.jorn.adrian.core.mitigations.Mutation;
import tech.jorn.adrian.core.mitigations.MutationResults;
import tech.jorn.adrian.core.observables.SubscribableValueEvent;
import tech.jorn.adrian.core.observables.ValueDispatcher;
import tech.jorn.adrian.core.risks.detection.IRiskDetection;
import tech.jorn.adrian.core.risks.detection.RiskReport;

import java.util.Optional;

public class AdrianAgent {
    Logger log = LogManager.getLogger(AdrianAgent.class);

    private final ValueDispatcher<AgentConfiguration> configuration;
    private final ValueDispatcher<AgentState> state = new ValueDispatcher<>(AgentState.Loading);
    private final IMessageBroker messageBroker;
    private final IKnowledgeBase knowledgeBase;
    private final IRiskDetection riskDetection;
    private final AuctionManager auctionManager;
    private final MitigationManager mitigationManager;

    public AdrianAgent(AgentConfiguration configuration, IMessageBroker messageBroker, IRiskDetection riskDetection, MitigationManager mitigationManager) {
        this(configuration, messageBroker, new KnowledgeBase(), riskDetection, mitigationManager);
    }

    public AdrianAgent(AgentConfiguration configuration, IMessageBroker messageBroker, IKnowledgeBase knowledgeBase, IRiskDetection riskDetection, MitigationManager mitigationManager) {
        this.configuration = new ValueDispatcher<>(configuration);
        this.messageBroker = messageBroker;
        this.knowledgeBase = knowledgeBase;
        this.riskDetection = riskDetection;
        this.auctionManager = new AuctionManager(this, messageBroker);
        this.mitigationManager = mitigationManager;

        this.messageBroker.setSender(configuration.getParentNode());
        this.knowledgeBase.upsertNode(configuration.getParentNode(), KnowledgeOrigin.Direct);
        this.knowledgeBase.upsertLinks(NodeLink.fromList(configuration.getParentNode(), configuration.getUpstreamNodes()));

        this.registerHandlers();

        this.state.setCurrent(AgentState.Idle);
    }

    private void registerHandlers() {
        this.messageBroker.onMessage(this::messageHandler);
        this.onConfigurationChange().subscribe(config -> {
            this.messageBroker.broadcast(new ShareKnowledgeEvent(
                    config.getParentNode(),
                    NodeLink.fromList(config.getParentNode(), config.getUpstreamNodes()),
                    1
            ));
        });
        this.knowledgeBase.onKnowledgeUpdate().subscribe(n -> {
            log.info("{} received knowledge from {}",
                    this.configuration.current().getParentNode().getID(),
                    n.getID()
            );
            // TODO: Schedule a SearchForRisksEvent
        });
        this.auctionManager.onAuctionChanged().subscribe(a -> {
            if (a == null) return;
            this.state.setCurrent(AgentState.Auctioning);
        });
    }

    private void messageHandler(MessageResponse<?> message) {
        // TODO: Implement a event manager
        if (message.getData() instanceof Event) {
            this.eventHandler((Event) message.getData());
        }
    }

    private void eventHandler(Event event) {
        if (event instanceof ShareKnowledgeEvent e) {
            this.knowledgeBase.upsertNode(e.getNode());
            this.knowledgeBase.upsertLinks(e.getLinks());
            e.getNode().getSoftwareAssets().forEach(asset -> {
                this.knowledgeBase.upsertAsset(asset, e.getNode());
            });

            if (e.getDistance() > 1) {
//                this.shareKnowledge(e.getDistance() - 1);
                this.messageBroker.broadcast(new ShareKnowledgeEvent(e.getNode(), e.getLinks(), e.getDistance() - 1, e.getTime()));
            }
        } else if (event instanceof NodeNoLongerReachableEvent e) {
            // TODO: Implement
        } else if (event instanceof IdentifyRisksEvent e) {
            if (!this.isAvailable()) return;

            var risk = this.detectRisks();
            if (risk.isEmpty()) return;

            this.auctionManager.initiateAuction(risk.get());
        } else if (event instanceof JoinAuctionRequestEvent e) {
            if (!this.isAvailable()) {
                this.auctionManager.rejectAuction(e.getAuction());
                return;
            }
            this.auctionManager.joinAuction(e.getAuction());

            var mutation = this.findMutation(e.getAuction().getRiskReport());
            var proposal = new AuctionProposal(this.configuration.current().getParentNode(), e.getAuction(), mutation);
            this.messageBroker.send(e.getAuction().getHost(), new AuctionProposalEvent(proposal));
        } else if (event instanceof JoinAuctionAcceptEvent e) {
            this.auctionManager.auctionJoined(e.getOrigin());
        } else if (event instanceof AuctionProposalEvent e) {
            if (this.state.current() != AgentState.Auctioning) {
                log.warn("Received proposal, but is not in an auction");
                return;
            }
            log.info("Received a proposal with {} mutations", e.getProposal().getProposal().mutations.size());
            this.auctionManager.receiveProposal(e.getProposal());
        } else if (event instanceof AuctionClosedEvent e) {
            if (this.state.current() != AgentState.Auctioning) {
                log.warn("Received closing message, but was not in auction");
                return;
            }
            // TODO: Depending on the proposal go into idle or migrating
            if (e.getSelectedProposal().isEmpty()) {
                this.state.setCurrent(AgentState.Idle);
                return;
            }
            this.applyMutation(e.getSelectedProposal().get().getProposal());
        }
    }

    public SubscribableValueEvent<AgentState> onStateChange() {
        return this.state.subscribable;
    }

    public SubscribableValueEvent<AgentConfiguration> onConfigurationChange() {
        return this.configuration.subscribable;
    }

    public AgentConfiguration getConfiguration() {
        return this.configuration.current();
    }

    public IKnowledgeBase getKnowledgeBase() {
        return this.knowledgeBase;
    }

    public void shareKnowledge() {
        this.shareKnowledge(1);
    }

    public void shareKnowledge(int distance) {
        var configuration = this.configuration.current();
        var event = new ShareKnowledgeEvent(
                configuration.getParentNode(),
                NodeLink.fromList(configuration.getParentNode(), configuration.getUpstreamNodes()),
                distance);
        this.messageBroker.broadcast(event);
    }

    public Optional<RiskReport> detectRisks() {
        var attackGraph = this.riskDetection.calculateAttackGraph(this.knowledgeBase);
        var riskReports = attackGraph.findRisks();
//        var riskSelection = new HighestDamageSelector(); // TODO: Move to constructor
//        return riskSelection.select(riskReports);
        return Optional.of(riskReports.get(1));
    }

    public MutationResults findMutation(RiskReport riskReport) {
        var attackGraph = this.riskDetection.calculateAttackGraph(this.knowledgeBase);
        var mergedAttackGraph = this.riskDetection.mergeAttackGraph(attackGraph, riskReport);
        var mutation = this.mitigationManager.getMitigation(mergedAttackGraph);
        return mutation;
    }
    public void applyMutation(MutationResults mutationResults) {
        this.state.setCurrent(AgentState.Migrating);
        log.info("Starting to migrate properties");

        var configuration = this.configuration.current();
        var currentNode = configuration.getParentNode();
        mutationResults.mutations.forEach(mutation -> {
            if (mutation instanceof AttributeChange<?> attributeChange) {
                if (!attributeChange.getNode().getID().equals(currentNode.getID())) return;

                log.info("Mutating property '{}' from '{}' to '{}'",
                        attributeChange.getAttribute(),
                        currentNode.getProperty(attributeChange.getAttribute()),
                        attributeChange.getValue()
                );
                currentNode.setProperty(attributeChange.getAttribute(), attributeChange.getValue());
            }
            // TODO: Implement Migration
        });
        var updatedConfig = new AgentConfiguration(currentNode, configuration.getUpstreamNodes());
        this.configuration.setCurrent(updatedConfig);
        this.knowledgeBase.upsertNode(currentNode);

        this.state.setCurrent(AgentState.Idle);
    }

    public boolean isAvailable() {
        return this.state.current() == AgentState.Idle;
    }
}
