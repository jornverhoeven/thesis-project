package tech.jorn.adrian.agent.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.jorn.adrian.agent.events.*;
import tech.jorn.adrian.core.agents.AgentState;
import tech.jorn.adrian.core.agents.IAgentConfiguration;
import tech.jorn.adrian.core.controllers.AbstractController;
import tech.jorn.adrian.core.events.Event;
import tech.jorn.adrian.core.events.EventManager;
import tech.jorn.adrian.core.graphs.base.INode;
import tech.jorn.adrian.core.graphs.knowledgebase.KnowledgeBase;
import tech.jorn.adrian.core.observables.SubscribableEvent;
import tech.jorn.adrian.core.observables.SubscribableValueEvent;
import tech.jorn.adrian.core.services.proposals.ProposalManager;
import tech.jorn.adrian.core.services.proposals.LowestDamage;
import tech.jorn.adrian.core.services.proposals.IProposalSelector;
import tech.jorn.adrian.core.services.risks.HighestRisk;
import tech.jorn.adrian.core.services.risks.IRiskSelector;
import tech.jorn.adrian.core.services.RiskDetection;

import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class RiskController extends AbstractController {
    Logger log = LogManager.getLogger(RiskController.class);

    private final RiskDetection riskDetection;
    private final IRiskSelector riskSelector;
    private final KnowledgeBase knowledgeBase;

    private Timer riskAssessmentTimer;

    public RiskController(RiskDetection riskDetection, KnowledgeBase knowledgeBase, EventManager eventManager,
            IRiskSelector riskSelector, IAgentConfiguration configuration,
            SubscribableValueEvent<AgentState> agentState) {
        super(eventManager, agentState);

        this.log = LogManager.getLogger(String.format("[%s] %s", configuration.getNodeID(), "RiskController"));

        this.riskDetection = riskDetection;
        this.knowledgeBase = knowledgeBase;
        this.riskSelector = riskSelector;

        this.agentState.subscribe(state -> {
            if (state.equals(AgentState.Idle))
                this.scheduleRiskAssessment();
        });

        this.eventManager.registerEventHandler(IdentifyRiskEvent.class, this::identifyRisk);
        this.eventManager.registerEventHandler(FoundRiskEvent.class, this::foundRiskEvent);
        this.eventManager.registerEventHandler(SelectedRiskEvent.class, this::selectedRiskEvent);
    }

    protected void identifyRisk(IdentifyRiskEvent event) {
        // if (!this.canDoRiskAssessment()) return;

        var attackGraph = this.riskDetection.createAttackGraph(this.knowledgeBase);
        var risks = this.riskDetection.identifyRisks(attackGraph);
        var risk = this.riskSelector.select(risks);

        this.log.debug("Found {} risks", risks.size());

        risks.forEach(r -> this.eventManager.emit(new FoundRiskEvent(r)));
        // risk.ifPresent(r -> this.eventManager.emit(new SelectedRiskEvent(r)));
        risk.ifPresentOrElse(
                r -> this.eventManager.emit(new SelectedRiskEvent(r)),
                () -> this.log.warn("No risk was found with the given constraints"));
    }

    protected void foundRiskEvent(FoundRiskEvent event) {
        // this.eventManager.emit(new InitiateAuctionEvent());
    }

    protected void selectedRiskEvent(SelectedRiskEvent event) {
        // if (!this.canDoRiskAssessment()) return;
        log.info("Selected risk with probability {} and damage value {} (path: {})",
                event.getRiskReport().probability(),
                event.getRiskReport().damage(),
                event.getRiskReport().path()
                        .stream()
                        .map(INode::getID)
                        .collect(Collectors.joining(" -> ")));
        this.eventManager.emit(new InitiateAuctionEvent(event.getRiskReport()));
    }

    private TimerTask createScheduledRiskAssessmentTask() {
        var log = this.log;
        var eventManager = this.eventManager;
        return new TimerTask() {
            @Override
            public void run() {
                log.warn("Searching for risks due to inactivity");
                eventManager.emit(new IdentifyRiskEvent());
                scheduleRiskAssessment();
            }
        };
    }

    private void scheduleRiskAssessment() {
        if (this.riskAssessmentTimer != null) {
            this.riskAssessmentTimer.cancel();
            this.riskAssessmentTimer = null;
        }

        try {
            this.riskAssessmentTimer = new Timer();
            var task = createScheduledRiskAssessmentTask();

            var interval = 30 * 1000;
            this.riskAssessmentTimer.schedule(task, interval);

            this.eventManager.once(Event.class, e -> {
                scheduleRiskAssessment();
            });
        } catch (Exception e) {
            log.error("Error while scheduling risk assessment");
        }
    }

    private boolean canDoRiskAssessment() {
        return this.agentState.current().equals(AgentState.Idle);
    }
}
