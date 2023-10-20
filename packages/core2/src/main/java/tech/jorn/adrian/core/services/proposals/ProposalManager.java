package tech.jorn.adrian.core.services.proposals;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.jorn.adrian.core.agents.AgentState;
import tech.jorn.adrian.core.agents.IAgentConfiguration;
import tech.jorn.adrian.core.auction.Auction;
import tech.jorn.adrian.core.auction.AuctionProposal;
import tech.jorn.adrian.core.graphs.AbstractDetailedNode;
import tech.jorn.adrian.core.graphs.infrastructure.SoftwareAsset;
import tech.jorn.adrian.core.graphs.knowledgebase.KnowledgeBase;
import tech.jorn.adrian.core.graphs.knowledgebase.KnowledgeBaseEntry;
import tech.jorn.adrian.core.graphs.knowledgebase.KnowledgeBaseNode;
import tech.jorn.adrian.core.graphs.knowledgebase.KnowledgeBaseSoftwareAsset;
import tech.jorn.adrian.core.graphs.knowledgebase.KnowledgeOrigin;
import tech.jorn.adrian.core.graphs.risks.AttackGraphEntry;
import tech.jorn.adrian.core.graphs.risks.AttackGraphLink;
import tech.jorn.adrian.core.graphs.risks.AttackGraphNode;
import tech.jorn.adrian.core.mutations.AttributeChange;
import tech.jorn.adrian.core.mutations.Migration;
import tech.jorn.adrian.core.mutations.Mutation;
import tech.jorn.adrian.core.observables.ValueDispatcher;
import tech.jorn.adrian.core.properties.AbstractProperty;
import tech.jorn.adrian.core.properties.NodeProperty;
import tech.jorn.adrian.core.properties.SoftwareProperty;
import tech.jorn.adrian.core.risks.RiskReport;
import tech.jorn.adrian.core.services.InfrastructureEffector;
import tech.jorn.adrian.core.services.RiskDetection;
import tech.jorn.adrian.core.services.probability.ProductRiskProbability;
import tech.jorn.adrian.risks.rules.PropertyBasedRule;
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
    private final ValueDispatcher<AgentState> agentState;
    private final InfrastructureEffector infrastructureEffector;

    public ProposalManager(KnowledgeBase knowledgeBase, RiskDetection riskDetection, IProposalSelector proposalSelector,
            IAgentConfiguration configuration, ValueDispatcher<AgentState> agentState,
            InfrastructureEffector infrastructureEffector) {
        this.knowledgeBase = knowledgeBase;
        this.riskDetection = riskDetection;
        this.proposalSelector = proposalSelector;
        this.configuration = configuration;
        this.agentState = agentState;
        this.infrastructureEffector = infrastructureEffector;

        this.log = LogManager.getLogger(
                String.format("[%s] %s", this.configuration.getNodeID(), ProposalManager.class.getSimpleName()));
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
                    if (knowledgeNode.getKnowledgeOrigin() != KnowledgeOrigin.DIRECT) {
                        knowledgeBase.upsertNode(knowledgeNode);
                        updated = true;
                    }
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
                                    : KnowledgeBaseSoftwareAsset
                                            .fromNode((AbstractDetailedNode<SoftwareProperty<?>>) next));
                    if (knowledgeNode.getKnowledgeOrigin() != KnowledgeOrigin.DIRECT) {
                        knowledgeBase.upsertNode(knowledgeNext);
                        updated = true;
                    }
                } else {
                    knowledgeNext = knowledgeBase.findById(next.getID()).get();
                }
            }
            if (updated)
                knowledgeBase.addEdge(knowledgeNode, knowledgeNext);
        }

        // Figure out all possible mutations
        var mutations = this.generateMutations(knowledgeBase, riskReport);
        var proposals = new ArrayList<AuctionProposal>();
        mutations.forEach(mutation -> {
            var proposal = this.evaluateMutation(mutation, auction);
            if (proposal == null)
                return;
            proposals.add(proposal);
            this.log.debug("New probability {} compared to old probability {} for {}", proposal.probability(),
                    riskReport.probability(), mutation.toString());
            this.log.debug("New damage {} compared to old damage {}", proposal.newDamage(), riskReport.damage());
        });

        return proposals;
    }

    private AuctionProposal evaluateMutation(Mutation<?> mutation, Auction auction) {
        this.log.debug("Evaluating mutation {}", mutation.toString());
        KnowledgeBase clonedKnowledgeBase = knowledgeBase.clone();

        if (mutation instanceof Migration<?, ?>)
            return this.evaluateMigration((Migration<?, ?>) mutation, auction, clonedKnowledgeBase);
        else if (mutation instanceof AttributeChange<?, ?>)
            return this.evaluateAttributeChange((AttributeChange<AbstractDetailedNode<?>, ?>) mutation, auction,
                    clonedKnowledgeBase);
        return null;
    }

    private AuctionProposal evaluateMigration(Migration<?, ?> migration, Auction auction, KnowledgeBase knowledgeBase) {
        var riskReport = auction.getRiskReport();
        // First we need to perform the migration on the knowledgebase
        var software = riskReport.path().get(riskReport.path().size() - 1);
        var knowledgeSoftware = knowledgeBase.findById(software.getID());
        var host = knowledgeBase.getParents(software.getID())
                .stream()
                .filter(n -> n instanceof KnowledgeBaseNode)
                .findFirst();
        var target = knowledgeBase.findById(migration.getNode().getID());

        // Sever connection between host and software
        knowledgeBase.removeEdge(host.get(), knowledgeSoftware.get());
        knowledgeBase.removeEdge(knowledgeSoftware.get(), host.get());

        // Add new connections between target and software
        knowledgeBase.addEdge(target.get(), knowledgeSoftware.get());
        knowledgeBase.addEdge(knowledgeSoftware.get(), target.get());

        // Calculate the new attack graph
        var attackGraph = this.riskDetection.createAttackGraph(knowledgeBase);

        // Calculate the new critical path
        List<AttackGraphEntry<?>> attackGraphPath = new ArrayList();
        var sliceStart = riskReport.path().indexOf(
                riskReport.path().stream().filter(n -> target.get().getID().equals(n.getID())).findFirst().get());
        for (var i = 0; i <= sliceStart; i++) {
            var n = riskReport.path().get(i);
            var attackNode = attackGraph.findById(n.getID());
            attackNode.ifPresent(attackGraphPath::add);
        }
        var attackSoftware = attackGraph.findById(software.getID());
        attackSoftware.ifPresent(attackGraphPath::add);
        var probability = attackGraph.getProbabilityForPath(attackGraphPath, new ProductRiskProbability());
        var newDamage = riskReport.damageValue() * probability;
        return new AuctionProposal(
                this.configuration.getParentNode(),
                auction,
                migration,
                newDamage,
                probability);
    }

    private AuctionProposal evaluateAttributeChange(AttributeChange<AbstractDetailedNode<?>, ?> attributeChange,
            Auction auction, KnowledgeBase knowledgeBase) {
        var riskReport = auction.getRiskReport();
        // Apply the attribute change
        var nodeToMutate = knowledgeBase.findById(attributeChange.getNode().getID()).get();
        attributeChange.apply(nodeToMutate);
        knowledgeBase.upsertNode(nodeToMutate);

        var attackGraph = this.riskDetection.createAttackGraph(knowledgeBase);
        riskReport.graph().getNodes().forEach(node -> {
            var riskEdges = riskReport.graph().getNeighboursWithRisks(node);
            var existingEdges = attackGraph.getNeighboursWithRisks(node).stream()
                    .map(AttackGraphLink::getRisk).toList();

            riskEdges.forEach(edge -> {
                var hasBeenMutated = attributeChange.getRiskRule() == null
                        && edge.getRisk().rule().getClass().equals(attributeChange.getRiskRule().getClass());
                if (hasBeenMutated) {
                    this.log.debug("Risk {} was just updated so we will not add it again", edge.getRisk().type());
                    return;
                }

                var exists = existingEdges.stream().filter(link -> link.type().equals(edge.getRisk().type())).findAny();
                if (exists.isEmpty())
                    attackGraph.addEdge(node, edge.getNode(), edge.getRisk());
            });
        });
        List<AttackGraphEntry<?>> attackGraphPath = new ArrayList();

        riskReport.path().forEach(node -> {
            var attackNode = attackGraph.findById(node.getID());
            attackNode.ifPresent(attackGraphPath::add);
            if (attackNode.isEmpty())
                this.log.warn("Node not found in attack graph {}", node.getID());
        });
        var probability = attackGraph.getProbabilityForPath(attackGraphPath, new ProductRiskProbability());
        var newDamage = riskReport.damageValue() * probability;
        return new AuctionProposal(
                this.configuration.getParentNode(),
                auction,
                attributeChange,
                newDamage,
                probability);
    }

    public Optional<AuctionProposal> selectProposal(List<AuctionProposal> proposals, Auction auction) {
        return this.proposalSelector.select(proposals, auction.getRiskReport().damageValue() - 0.1f);
    }

    public <N extends AbstractDetailedNode<P>, P extends AbstractProperty<?>> List<Mutation<N>> generateMutations(
            KnowledgeBase knowledgeBase, RiskReport riskReport) {

        List<Mutation<N>> mutations = new java.util.ArrayList<>(List.of());
        var node = (N) knowledgeBase.findById(this.configuration.getNodeID()).get();
        var mutators = this.getMutators(riskReport, node);
        mutators.forEach(mutator -> {
            if (mutator != null && mutator.isApplicable(node)) {
                mutations.add(mutator);
            }
        });
        return mutations;
    }

    private <N extends AbstractDetailedNode<P>, P extends AbstractProperty<?>> List<Mutation<N>> getMutators(
            RiskReport riskReport, N node) {
        var mutations = new ArrayList<Mutation<N>>();

        var attackGraph = riskReport.graph();
        var criticalAsset = (AbstractDetailedNode<SoftwareProperty<?>>) riskReport.path()
                .get(riskReport.path().size() - 1);
        var n = attackGraph.findById(node.getID());
        if (n.isPresent()) {
            var risks = riskReport.graph().getNeighboursWithRisks(n.get())
                    .stream().map(AttackGraphLink::getRisk)
                    .collect(Collectors.toList());
            risks.addAll(riskReport.graph().getIncoming().stream().map(AttackGraphLink::getRisk).toList());
            risks.forEach(risk -> {
                if (risk.rule() instanceof CveRule<?> cve) {
                    var adaptation = cve.getAdaptation(node);
                    adaptation.ifPresent(mutations::add);
                } else if (risk.rule() instanceof PropertyBasedRule rule) {
                    var adaptation = rule.getAdaptation(node);
                    adaptation.ifPresent(mutations::add);
                }
            });

            this.configuration.getAssets().forEach(asset -> {
                var s = attackGraph.findById(asset.getID());
                if (s.isEmpty())
                    return;

                risks.forEach(risk -> {
                    if (!(risk.rule() instanceof CveRule<?>))
                        return;
                    var adaptation = ((CveRule<?>) risk.rule()).getAdaptation((N) s.get());
                    adaptation.ifPresent(mutations::add);
                });
            });
        }

        if (node instanceof KnowledgeBaseNode) {
            if (this.configuration.canMigrate() && riskReport.path().size() > 2) {
                var host = (N) riskReport.path().get(riskReport.path().size() - 2);
                var alreadyHosting = host.getID().equals(node.getID());
                if (!alreadyHosting)
                    mutations.add(new Migration<>(criticalAsset, host, node, 1000, 10000, null));
            }
        }
        return mutations;
    }

    public void applyProposal(AuctionProposal proposal) {
        if (!this.agentState.current().equals(AgentState.Idle)) {
            this.log.error("Can only apply proposals while idle");
            return;
        }
        this.agentState.setCurrent(AgentState.Migrating);

        var node = this.configuration.getParentNode();
        var mutation = proposal.mutation();

        try {
            Thread.sleep(Math.round(mutation.getTime()));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (mutation instanceof Migration<?, ?> migration) {
            if (migration.getFrom().getID().equals(node.getID())) {

                this.log.info("Migrating software {} : Removing from {}", migration.getAsset().getID(), node.getID());
                // Remove the software asset
                this.configuration.getAssets().removeIf(asset -> asset.getID().equals(migration.getAsset().getID()));
                infrastructureEffector.updateInfra(infra -> {
                    var from = infra.findById(node.getID()).get();
                    var asset = infra.findById(migration.getAsset().getID()).get();
                    infra.removeEdge(from, asset);
                    infra.removeEdge(asset, from);
                });
            } else {
                this.log.info("Migrating software {}: Adding to {}", migration.getAsset().getID(), node.getID());
                var asset = new SoftwareAsset(migration.getAsset().getID());
                migration.getAsset().getProperties().forEach(asset::setFromProperty);
                this.configuration.getAssets().add(asset);

                infrastructureEffector.updateInfra(infra -> {
                    var target = infra.findById(migration.getNode().getID()).get();
                    var _asset = infra.findById(migration.getAsset().getID()).get();
                    infra.addEdge(target, _asset);
                    infra.addEdge(_asset, target);
                });

                var knowledgeAsset = KnowledgeBaseSoftwareAsset.fromNode(asset);
                this.knowledgeBase.upsertNode(knowledgeAsset);
            }
            var knowledgeFrom = this.knowledgeBase.findById(migration.getFrom().getID());
            var knowledgeTarget = this.knowledgeBase.findById(migration.getNode().getID());
            var knowledgeAsset = this.knowledgeBase.findById(migration.getAsset().getID());

            this.knowledgeBase.removeEdge(knowledgeFrom.get(), knowledgeAsset.get());
            this.knowledgeBase.removeEdge(knowledgeAsset.get(), knowledgeFrom.get());
            this.knowledgeBase.addEdge(knowledgeTarget.get(), knowledgeAsset.get());
            this.knowledgeBase.addEdge(knowledgeAsset.get(), knowledgeTarget.get());
        } else {
            proposal.mutation().apply(node);
            this.knowledgeBase.upsertNode(KnowledgeBaseNode.fromNode(node));
            infrastructureEffector.updateInfra(infra -> {
                var _node = infra.findById(this.configuration.getNodeID()).get();
                proposal.mutation().apply(_node);
                infra.upsertNode(_node);
            });
        }

        this.log.debug("Done applying proposal");
        if (mutation instanceof Migration<?, ?> migration)
            this.log.debug("Software {} is now on {}", migration.getAsset().getID(), migration.getNode().getID());
        else if (mutation instanceof AttributeChange<?, ?> attributeChange) {
            this.configuration.getParentNode().getProperty(attributeChange.getNewValue().getName())
                    .ifPresent(property -> {
                        this.log.debug("Property {} is now {}", attributeChange.getNewValue().getName(), property);
                    });
            this.knowledgeBase.findById(this.configuration.getNodeID()).ifPresent(_node -> {
                _node.getProperty(attributeChange.getNewValue().getName()).ifPresent(property -> {
                    this.log.debug("Knowledge property {} is now {}", attributeChange.getNewValue().getName(),
                            property);
                });
            });
        }
        this.agentState.setCurrent(AgentState.Idle);
    }
}

@FunctionalInterface
interface IMutationFunction<N extends AbstractDetailedNode<?>> {
    Mutation<N> mutate(N node);
}
