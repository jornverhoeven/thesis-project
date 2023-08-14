package tech.jorn.adrian.agent.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.jorn.adrian.agent.events.FoundRiskEvent;
import tech.jorn.adrian.agent.events.IdentifyRiskEvent;
import tech.jorn.adrian.agent.events.SelectedRiskEvent;
import tech.jorn.adrian.core.controllers.AbstractController;
import tech.jorn.adrian.core.events.EventManager;
import tech.jorn.adrian.core.graphs.knowledgebase.KnowledgeBase;
import tech.jorn.adrian.core.risks.RiskReport;
import tech.jorn.adrian.core.services.IRiskSelector;
import tech.jorn.adrian.core.services.RiskDetection;

import java.util.List;
import java.util.Optional;

public class RiskController extends AbstractController {
    Logger log = LogManager.getLogger(RiskController.class);

    private final RiskDetection riskDetection;
    private final IRiskSelector riskSelector;
    private final KnowledgeBase knowledgeBase;

    public RiskController(RiskDetection riskDetection, KnowledgeBase knowledgeBase, EventManager eventManager) {
        this(riskDetection, knowledgeBase, eventManager, new IRiskSelector() {
            @Override
            public Optional<RiskReport> select(List<RiskReport> risks) {
                return Optional.empty();
            }
        });
    }
    public RiskController(RiskDetection riskDetection, KnowledgeBase knowledgeBase, EventManager eventManager, IRiskSelector riskSelector) {
        super(eventManager);

        this.riskDetection = riskDetection;
        this.knowledgeBase = knowledgeBase;
        this.riskSelector = riskSelector;

        this.eventManager.registerEventHandler(IdentifyRiskEvent.class, this::identifyRisk);
        this.eventManager.registerEventHandler(FoundRiskEvent.class, this::foundRiskEvent);
    }

    protected void identifyRisk(IdentifyRiskEvent event) {
        var attackGraph = this.riskDetection.createAttackGraph(this.knowledgeBase);
        var risks = this.riskDetection.identifyRisks(attackGraph);
        var risk = this.riskSelector.select(risks);

        risks.forEach(r -> this.eventManager.emit(new FoundRiskEvent(r)));
        risk.ifPresent(r -> this.eventManager.emit(new SelectedRiskEvent(r)));
    }

    protected void foundRiskEvent(FoundRiskEvent event) {
//        this.eventManager.emit(new InitiateAuctionEvent());
    }
}

