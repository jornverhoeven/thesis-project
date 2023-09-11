package tech.jorn.adrian.core.services.proposals;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.jorn.adrian.agent.controllers.RiskController;
import tech.jorn.adrian.core.agents.IAgentConfiguration;
import tech.jorn.adrian.core.auction.Auction;
import tech.jorn.adrian.core.auction.AuctionProposal;
import tech.jorn.adrian.core.graphs.AbstractDetailedNode;
import tech.jorn.adrian.core.graphs.knowledgebase.KnowledgeBase;
import tech.jorn.adrian.core.graphs.knowledgebase.KnowledgeBaseNode;
import tech.jorn.adrian.core.graphs.risks.AttackGraphEntry;
import tech.jorn.adrian.core.mutations.AttributeChange;
import tech.jorn.adrian.core.mutations.Mutation;
import tech.jorn.adrian.core.properties.NodeProperty;
import tech.jorn.adrian.core.risks.RiskReport;
import tech.jorn.adrian.core.risks.RiskRule;
import tech.jorn.adrian.core.services.RiskDetection;
import tech.jorn.adrian.core.services.probability.ProductRiskProbability;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProposalManager {
    Logger log = LogManager.getLogger(ProposalManager.class);

    private final KnowledgeBase knowledgeBase;
    private final RiskDetection riskDetection;
    private final IProposalSelector proposalSelector;
    private final IAgentConfiguration configuration;

    public ProposalManager(KnowledgeBase knowledgeBase, RiskDetection riskDetection, IProposalSelector proposalSelector, IAgentConfiguration configuration) {
        this.knowledgeBase = knowledgeBase;
        this.riskDetection = riskDetection;
        this.proposalSelector = proposalSelector;
        this.configuration = configuration;
    }

    public List<AuctionProposal> findProposals(Auction auction) {
        var riskReport = auction.getRiskReport();
        // Add all new nodes to the knowledge base
        for (int i = 0; i < riskReport.path().size() - 1; i++) {
            var node = riskReport.path().get(i);
            var next = riskReport.path().get(i + 1);

            var exists = knowledgeBase.findById(node.getID()).isPresent();
            if (exists) continue;

            var knowledgeNode = KnowledgeBaseNode.fromNode((AbstractDetailedNode<NodeProperty<?>>) node);
            var knowledgeNext = knowledgeBase.findById(next.getID())
                    .orElse(KnowledgeBaseNode.fromNode((AbstractDetailedNode<NodeProperty<?>>) next));

            knowledgeBase.upsertNode(knowledgeNode);
            knowledgeBase.upsertNode(knowledgeNext);
            knowledgeBase.addEdge(knowledgeNode, knowledgeNext);
        }

        // Figure out all possible mutations
        var mutations = this.generateMutations(knowledgeBase, riskReport);
        var proposals = new ArrayList<AuctionProposal>();
        mutations.forEach(mutation -> {
            this.log.debug("Evaluating mutation {}", mutation.toString());
            KnowledgeBase clonedKnowledgeBase = knowledgeBase.clone();
            var nodeToMutate = clonedKnowledgeBase.findById(mutation.getNode().getID()).get();
            mutation.apply(nodeToMutate);
            clonedKnowledgeBase.upsertNode(nodeToMutate);
            // TODO: Figure out how to substitute it properly

            var attackGraph = this.riskDetection.createAttackGraph(clonedKnowledgeBase);
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

    public <N extends AbstractDetailedNode<?>> List<Mutation<N>> generateMutations(KnowledgeBase knowledgeBase, RiskReport riskReport) {
        var mutators = this.getMutators();

        List<Mutation<N>> mutations = new java.util.ArrayList<>(List.of());
        var node = knowledgeBase.findById(this.configuration.getNodeID()).get();
        mutators.forEach(mutator -> {
            var mutation = (Mutation<N>) mutator.mutate(node);
            if (mutation != null && mutation.isApplicable((N) node)) mutations.add(mutation);
        });
        return mutations;
    }

    private List<IMutationFunction<AbstractDetailedNode<?>>> getMutators() {
        return List.of(
                (AbstractDetailedNode<?> node) -> {
                    if (!(node instanceof KnowledgeBaseNode)) return null;
                    return new AttributeChange<>(node, new NodeProperty<>("hasFirewall", true));
                },
                (AbstractDetailedNode<?> node) -> {
                    if (!(node instanceof KnowledgeBaseNode)) return null;
                    return new AttributeChange<>(node, new NodeProperty<>("isPhysicallySecured", true));
                }
        );
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

