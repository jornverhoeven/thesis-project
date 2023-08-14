package tech.jorn.adrian.experiments;

import org.apache.logging.log4j.LogManager;

import tech.jorn.adrian.agent.AdrianAgent;
import tech.jorn.adrian.agent.ResourceLoader;
import tech.jorn.adrian.agent.mitigation.*;
import tech.jorn.adrian.core.IIdentifiable;
import tech.jorn.adrian.core.graph.INode;
import tech.jorn.adrian.core.infrastructure.Node;
import tech.jorn.adrian.core.messaging.IMessageBroker;
import tech.jorn.adrian.core.messaging.MessageResponse;
import tech.jorn.adrian.core.risks.RiskRule;
import tech.jorn.adrian.core.risks.detection.BasicRiskDetector;
import tech.jorn.adrian.core.risks.detection.RiskReport;
import tech.jorn.adrian.core.utils.MermaidAttackGraphGenerator;
import tech.jorn.adrian.core.utils.MermaidGraphGenerator;
import tech.jorn.adrian.experiments.simulation.SimulationGenerator;
import tech.jorn.adrian.experiments.utils.MessageQueue;
import tech.jorn.adrian.risks.mitigations.PropertyBasedMutator;
import tech.jorn.adrian.risks.rules.PropertyBasedRule;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ExperimentOne {
    public static void main(String[] args) {
        var log = LogManager.getLogger(ExperimentOne.class);

        var riskRules = getRiskRules();
        log.debug("Found {} risk rules", riskRules.size());

        var infrastructureFile = "./simple-infra.yml";
        log.debug("Initializing experiment one with infrastructure file: {}", infrastructureFile);

        var generator = new SimulationGenerator();
        var simulation = generator
                .withAgentFactory(configuration -> {
                    var messageBroker = new NoCommunicationBroker();
                    var riskDetection = new BasicRiskDetector(riskRules);
                    var mitigationManager = new MitigationManager(getMitigationStrategies(riskRules), getMutationSelector());

                    return new ExperimentalAgent(configuration, messageBroker, riskDetection, mitigationManager);
                })
                .fromFile(infrastructureFile);

        var agents = simulation.getAgents();
        log.debug("Infrastructure loaded with {} agents", agents.size());

        Map<String, RiskReport> reports = new HashMap<>();
        agents.forEach(a -> {
            a.getRiskDetection().onRisksFound().subscribe(riskReports -> riskReports.forEach(r -> reports.put(r.getPathRepresentation(), r)));

        });

        log.info("Starting simulation for experiment one.");

        agents.forEach(AdrianAgent::shareKnowledge);

        var mermaid = new MermaidAttackGraphGenerator();
        var agentA = agents.get(0);

        System.out.println(mermaid.print(agentA.getRiskDetection().calculateAttackGraph(agentA.getKnowledgeBase())));

        agents.forEach(AdrianAgent::detectRisks);


        log.info("Simulation over");
        log.debug("Found {} risks in total", reports.size());
    }

    static private List<RiskRule> getRiskRules() {
        var loader = new ResourceLoader();
        var rules = loader.findAllRiskRules();
        return rules.stream()
                .map(c -> {
                    try {
                        return c.getDeclaredConstructor().newInstance();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }

    static private List<IMutationStrategy> getMitigationStrategies(List<RiskRule> riskRules) {
        return riskRules.stream()
                    .filter(r -> r instanceof PropertyBasedRule<?>)
                    .map(r -> new PropertyBasedMutator<>((PropertyBasedRule<?>) r))
                    .map(r -> (IMutationStrategy) new SingletonMutationStrategy(r))
                    .toList();
    }
    static private IMutationSelector getMutationSelector() {
        return new RandomRiskMutationSelector();
    }
}

class NoCommunicationBroker implements IMessageBroker {
    public NoCommunicationBroker() {
        // NOOP
    }

    @Override
    public <M> void broadcast(M message) {
        // NOOP
    }

    @Override
    public <M> void send(IIdentifiable target, M message) {
        // NOOP
    }

    @Override
    public <M, R> void send(IIdentifiable target, M message, Consumer<MessageResponse<R>> callback) {
        // NOOP
    }

    @Override
    public <M> void onMessage(Consumer<MessageResponse<M>> message) {
        // NOOP
        // TODO: Maybe receive messages from the experiment controller
    }

    @Override
    public void setSender(INode node) {
        // NOOP
    }
}
