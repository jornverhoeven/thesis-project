package tech.jorn.adrian.core.services.proposals;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.jorn.adrian.core.agents.IAgentConfiguration;
import tech.jorn.adrian.core.auction.Auction;
import tech.jorn.adrian.core.auction.AuctionProposal;
import tech.jorn.adrian.core.graphs.AbstractDetailedNode;
import tech.jorn.adrian.core.graphs.knowledgebase.KnowledgeBase;
import tech.jorn.adrian.core.graphs.knowledgebase.KnowledgeBaseEntry;
import tech.jorn.adrian.core.graphs.knowledgebase.KnowledgeBaseNode;
import tech.jorn.adrian.core.graphs.knowledgebase.KnowledgeBaseSoftwareAsset;
import tech.jorn.adrian.core.graphs.risks.AttackGraphEntry;
import tech.jorn.adrian.core.graphs.risks.AttackGraphLink;
import tech.jorn.adrian.core.graphs.risks.AttackGraphNode;
import tech.jorn.adrian.core.mutations.AttributeChange;
import tech.jorn.adrian.core.mutations.Mutation;
import tech.jorn.adrian.core.properties.AbstractProperty;
import tech.jorn.adrian.core.properties.NodeProperty;
import tech.jorn.adrian.core.properties.SoftwareProperty;
import tech.jorn.adrian.core.risks.RiskReport;
import tech.jorn.adrian.core.risks.RiskRule;
import tech.jorn.adrian.core.services.RiskDetection;
import tech.jorn.adrian.core.services.probability.ProductRiskProbability;
import tech.jorn.adrian.risks.rules.RuleInfrastructureNodeHasFirewall;
import tech.jorn.adrian.risks.rules.RuleInfrastructureNodeIsPhysicallySecured;
import tech.jorn.adrian.risks.rules.cves.CveRule;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ProposalManager {
    Logger log;

    private final KnowledgeBase knowledgeBase;
    private final RiskDetection riskDetection;
    private final IProposalSelector proposalSelector;
    private final IAgentConfiguration configuration;

    public ProposalManager(KnowledgeBase knowledgeBase, RiskDetection riskDetection, IProposalSelector proposalSelector, IAgentConfiguration configuration) {
        this.knowledgeBase = knowledgeBase;
        this.riskDetection = riskDetection;
        this.proposalSelector = proposalSelector;
        this.configuration = configuration;

        this.log = LogManager.getLogger(String.format("[%s] %s", this.configuration.getNodeID(), ProposalManager.class.getSimpleName()));
    }

    public List<AuctionProposal> findProposals(Auction auction) {
        var riskReport = auction.getRiskReport();
        // Add all new nodes to the knowledge base
        for (int i = 0; i < riskReport.path().size() - 1; i++) {
            var node = riskReport.path().get(i);
            var next = riskReport.path().get(i + 1);

            var updated = false;
            KnowledgeBaseEntry<?> knowledgeNode;
            KnowledgeBaseEntry<?> knowledgeNext;
            {
                var exists = knowledgeBase.findById(node.getID()).isPresent();
                if (!exists) {
                    knowledgeNode = KnowledgeBaseNode.fromNode((AbstractDetailedNode<NodeProperty<?>>) node);
                    knowledgeBase.upsertNode(knowledgeNode);
                    updated = true;
                } else {
                    knowledgeNode = knowledgeBase.findById(node.getID()).get();
                }
            }
            {
                var exists = knowledgeBase.findById(next.getID()).isPresent();
                if (!exists) {
                    knowledgeNext = knowledgeBase.findById(next.getID())
                            .orElse(next instanceof AttackGraphNode
                                    ? KnowledgeBaseNode.fromNode((AbstractDetailedNode<NodeProperty<?>>) next)
                                    : KnowledgeBaseSoftwareAsset.fromNode((AbstractDetailedNode<SoftwareProperty<?>>) next));
                    knowledgeBase.upsertNode(knowledgeNext);
                    updated = true;
                } else {
                    knowledgeNext = knowledgeBase.findById(next.getID()).get();
                }
            }
            if (updated) knowledgeBase.addEdge(knowledgeNode, knowledgeNext);
        }

        // Figure out all possible mutations
        var mutations = this.generateMutations(knowledgeBase, riskReport);
        var proposals = new ArrayList<AuctionProposal>();
        mutations.forEach(mutation -> {
            this.log.debug("Evaluating mutation {}", mutation.toString());
            KnowledgeBase clonedKnowledgeBase = knowledgeBase.clone();
            var nodeToMutate = clonedKnowledgeBase.findById(mutation.getNode().getID()).get();
            mutation.apply((AbstractDetailedNode<AbstractProperty<?>>) nodeToMutate);
            clonedKnowledgeBase.upsertNode(nodeToMutate);
            // TODO: Figure out how to substitute it properly

            var attackGraph = this.riskDetection.createAttackGraph(clonedKnowledgeBase);
            riskReport.graph().getNodes().forEach(node -> {
                var riskEdges = riskReport.graph().getNeighboursWithRisks(node);
                var existingEdges = attackGraph.getNeighboursWithRisks(node).stream()
                        .map(AttackGraphLink::getRisk).toList();

                riskEdges.forEach(edge -> {
                    var hasBeenMutated = edge.getRisk().rule().getClass().equals(mutation.getRiskRule().getClass());
                    if (hasBeenMutated) {
                        this.log.debug("Risk {} was just updated so we will not add it again", edge.getRisk().type());
                        return;
                    }

                    var exists = existingEdges.stream().filter(link -> link.type().equals(edge.getRisk().type())).findAny();
                    if (exists.isEmpty()) attackGraph.addEdge(node, edge.getNode(), edge.getRisk());
                });
            });
            List<AttackGraphEntry<?>> attackGraphPath = new ArrayList();

            riskReport.path().forEach(node -> {
                var attackNode = attackGraph.findById(node.getID());
                attackNode.ifPresent(attackGraphPath::add);
                if (attackNode.isEmpty()) this.log.warn("Node not found in attack graph {}", node.getID());
            });
            var probability = attackGraph.getProbabilityForPath(attackGraphPath, new ProductRiskProbability());
            var newDamage = riskReport.damageValue() * probability;
            proposals.add(new AuctionProposal(
                    this.configuration.getParentNode(),
                    auction,
                    mutation,
                    newDamage
            ));
            this.log.info("New probability {} compared to old probability {}", probability, riskReport.probability());
            this.log.info("New damage {} compared to old damage {}", probability * riskReport.damageValue(), riskReport.damage());
        });

        return proposals;
    }

    public Optional<AuctionProposal> selectProposal(List<AuctionProposal> proposals) {
        return this.proposalSelector.select(proposals);
    }

    public <N extends AbstractDetailedNode<P>, P extends AbstractProperty<?>> List<Mutation<N>> generateMutations(KnowledgeBase knowledgeBase, RiskReport riskReport) {

        List<Mutation<N>> mutations = new java.util.ArrayList<>(List.of());
        var node = (N) knowledgeBase.findById(this.configuration.getNodeID()).get();
        var mutators = this.getMutators(riskReport, node);
        mutators.forEach(mutator -> {
            if (mutator != null && mutator.isApplicable(node)) mutations.add(mutator);
        });
        return mutations;
    }

    private <N extends AbstractDetailedNode<P>, P extends AbstractProperty<?>> List<Mutation<N>> getMutators(RiskReport riskReport, N node) {
        var mutations = new ArrayList<Mutation<N>>();

        var attackGraph = riskReport.graph();
        var n = attackGraph.findById(node.getID());
        if (n.isPresent()) {
            var risks = riskReport.graph().getNeighboursWithRisks(n.get())
                    .stream().map(AttackGraphLink::getRisk)
                    .toList();
            risks.forEach(risk -> {
                if (!(risk.rule() instanceof CveRule<?>)) return;
                var adaptation = ((CveRule<?>) risk.rule()).getAdaptation(node);
                adaptation.ifPresent(mutations::add);
            });
        }

        if (node instanceof KnowledgeBaseNode) {
            mutations.add(new AttributeChange<>(node, new NodeProperty<>("hasFirewall", true), new RuleInfrastructureNodeHasFirewall()));
            mutations.add(new AttributeChange<>(node, new NodeProperty<>("isPhysicallySecured", true), new RuleInfrastructureNodeIsPhysicallySecured()));
        }
        return mutations;
    }

    public void applyProposal(AuctionProposal proposal) {
        var node = this.configuration.getParentNode();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        proposal.mutation().apply(node);
        this.knowledgeBase.upsertNode(KnowledgeBaseNode.fromNode(node));
        this.log.debug("Done applying proposal");
    }
}

@FunctionalInterface
interface IMutationFunction<N extends AbstractDetailedNode<?>> {
    Mutation<N> mutate(N node);
}

