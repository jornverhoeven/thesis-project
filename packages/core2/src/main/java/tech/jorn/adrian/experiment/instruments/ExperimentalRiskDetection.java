package tech.jorn.adrian.experiment.instruments;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.jorn.adrian.agent.AdrianAgent;
import tech.jorn.adrian.agent.services.BasicRiskDetection;
import tech.jorn.adrian.core.graphs.risks.AttackGraph;
import tech.jorn.adrian.core.risks.RiskEdge;
import tech.jorn.adrian.core.risks.RiskRule;
import tech.jorn.adrian.core.services.RiskDetection;
import tech.jorn.adrian.core.services.probability.IRiskProbabilityCalculator;

import java.util.List;
import java.util.function.Consumer;

public class ExperimentalRiskDetection extends BasicRiskDetection {
    Logger log = LogManager.getLogger(ExperimentalRiskDetection.class);

    private AdrianAgent agent;

    public ExperimentalRiskDetection(List<RiskRule> riskRules, IRiskProbabilityCalculator probabilityCalculator) {
        super(riskRules, probabilityCalculator);
    }

    public void setAgent(AdrianAgent agent) {

        this.agent = agent;
        this.log = LogManager.getLogger(String.format("[%s] %s", agent.getConfiguration().getNodeID(), RiskDetection.class.getSimpleName()));
    }

    @Override
    public Consumer<RiskEdge> createRiskDispatcher(AttackGraph attackGraph) {
        var original = super.createRiskDispatcher(attackGraph);
        return e -> {
            // TODO: Measure the risk edges!
            this.log.debug("Found a risk edge: {} {} {} {}", e.from().getID(), e.to().getID(), e.risk().type(), e.risk().factor());
            original.accept(e);
        };
    }
}
