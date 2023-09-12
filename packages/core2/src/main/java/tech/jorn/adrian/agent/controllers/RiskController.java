package tech.jorn.adrian.agent.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.jorn.adrian.agent.events.*;
import tech.jorn.adrian.core.controllers.AbstractController;
import tech.jorn.adrian.core.events.EventManager;
import tech.jorn.adrian.core.graphs.base.INode;
import tech.jorn.adrian.core.graphs.knowledgebase.KnowledgeBase;
import tech.jorn.adrian.core.services.proposals.ProposalManager;
import tech.jorn.adrian.core.services.proposals.LowestDamage;
import tech.jorn.adrian.core.services.proposals.IProposalSelector;
import tech.jorn.adrian.core.services.risks.HighestRisk;
import tech.jorn.adrian.core.services.risks.IRiskSelector;
import tech.jorn.adrian.core.services.RiskDetection;

import java.util.stream.Collectors;


public class RiskController extends AbstractController {
    Logger log = LogManager.getLogger(RiskController.class);

    private final RiskDetection riskDetection;
    private final ProposalManager proposalManager;
    private final IRiskSelector riskSelector;
    private final IProposalSelector proposalSelector;
    private final KnowledgeBase knowledgeBase;

    public RiskController(RiskDetection riskDetection, KnowledgeBase knowledgeBase, ProposalManager proposalManager, EventManager eventManager) {
        this(riskDetection, knowledgeBase, proposalManager, eventManager, new HighestRisk(1.0f), new LowestDamage(0.1f));
    }

    public RiskController(RiskDetection riskDetection, KnowledgeBase knowledgeBase, ProposalManager proposalManager, EventManager eventManager, IRiskSelector riskSelector, IProposalSelector proposalSelector) {
        super(eventManager);

        this.riskDetection = riskDetection;
        this.knowledgeBase = knowledgeBase;
        this.proposalManager = proposalManager;
        this.riskSelector = riskSelector;
        this.proposalSelector = proposalSelector;

        this.eventManager.registerEventHandler(IdentifyRiskEvent.class, this::identifyRisk);
        this.eventManager.registerEventHandler(FoundRiskEvent.class, this::foundRiskEvent, true);
        this.eventManager.registerEventHandler(SelectedRiskEvent.class, this::selectedRiskEvent, true);
    }

    protected void identifyRisk(IdentifyRiskEvent event) {
        var attackGraph = this.riskDetection.createAttackGraph(this.knowledgeBase);
        var risks = this.riskDetection.identifyRisks(attackGraph);
        var risk = this.riskSelector.select(risks);

        this.log.debug("Found {} risks", risks.size());

        risks.forEach(r -> this.eventManager.emit(new FoundRiskEvent(r)));
//        risk.ifPresent(r -> this.eventManager.emit(new SelectedRiskEvent(r)));
        risk.ifPresentOrElse(
                r -> this.eventManager.emit(new SelectedRiskEvent(r)),
                () -> this.log.warn("No risk was found with the given constraints"));
    }

    protected void foundRiskEvent(FoundRiskEvent event) {
//        this.eventManager.emit(new InitiateAuctionEvent());
    }

    protected void selectedRiskEvent(SelectedRiskEvent event) {
        log.info("Selected risk with probability {} and damage value {} (path: {})",
                event.getRiskReport().probability(),
                event.getRiskReport().damage(),
                event.getRiskReport().path()
                        .stream()
                        .map(INode::getID)
                        .collect(Collectors.joining(" -> ")));
        this.eventManager.emit(new InitiateAuctionEvent(event.getRiskReport()));
    }

}

