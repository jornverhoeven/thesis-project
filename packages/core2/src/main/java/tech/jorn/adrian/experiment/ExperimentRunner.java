package tech.jorn.adrian.experiment;


import org.apache.logging.log4j.LogManager;
import tech.jorn.adrian.agent.AdrianAgent;
import tech.jorn.adrian.agent.events.ApplyProposalEvent;
import tech.jorn.adrian.agent.events.FoundRiskEvent;
import tech.jorn.adrian.core.agents.IAgent;
import tech.jorn.adrian.core.graphs.infrastructure.Infrastructure;
import tech.jorn.adrian.core.observables.EventDispatcher;
import tech.jorn.adrian.core.risks.RiskReport;
import tech.jorn.adrian.experiment.features.*;
import tech.jorn.adrian.experiment.instruments.ExperimentalAgent;
import tech.jorn.adrian.experiment.messages.Envelope;
import tech.jorn.adrian.experiment.scenarios.NoChangeScenario;
import tech.jorn.adrian.experiment.scenarios.Scenario;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ExperimentRunner {
    private static final String infrastructureFile = "./simple-infra.yml";

    public static void main(String[] args) throws InterruptedException {
//        a1();
//        b1();
        c1();
    }


    public static void runTest(Infrastructure infrastructure, FeatureSet featureSet, Scenario scenario) {
        var log = LogManager.getLogger(ExperimentRunner.class);
        var startTime = new Date().getTime();

        var agentFactory = new AgentFactory(featureSet);

        scenario.scheduleEvents();

        log.debug("Creating agents");
        var agents = agentFactory.fromInfrastructure(infrastructure);

        var csv = new HashMap<String, List<Integer>>();
        var task = registerMetricCollection(agents, csv);
        var timer = new Timer();
        timer.scheduleAtFixedRate(task, 5 * 1000, 5 * 1000);

        log.debug("Starting agents");
        agents.forEach(AdrianAgent::start);

        scenario.onFinished().subscribe(() -> {
            log.info("Finished scenario in {}ms", new Date().getTime() - startTime);
            task.run();

            try {
                var scenarioName = scenario.getClass().getSimpleName();
                var featureName = featureSet.getClass().getSimpleName();
                var testIdentifier = scenarioName + "-" + featureName;
                writeToCSV(testIdentifier, agents, csv, new Date().getTime() - startTime);
            } catch (IOException e) {
                log.error("Could not write measures");
            }
            System.exit(0);
        });
    }

    public static void writeToCSV(String testIdentifier, List<ExperimentalAgent> agents, Map<String, List<Integer>> data, long endTime) throws IOException {
        var fileWriter = new FileWriter("./output/" + testIdentifier + ".csv");
        var writer = new PrintWriter(fileWriter);

        writer.printf("timestamps,%s,%s\n", IntStream.range(0, (int) (endTime / 5000)).mapToObj(i -> String.valueOf(5 * (i+1) * 1000)).collect(Collectors.joining(",")), endTime);
        writer.printf("messages-total,%s\n", data.get("messages-total").stream()
                .map(Object::toString)
                .collect(Collectors.joining(",")));
        agents.forEach(agent -> {
            var id = agent.getConfiguration().getNodeID();
            writer.printf("messages-%s,%s\n", id, data.get("messages-" + id).stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(",")));
        });

        writer.printf("proposals-total,%s\n", data.get("proposals-total").stream()
                .map(Object::toString)
                .collect(Collectors.joining(",")));
        agents.forEach(agent -> {
            var id = agent.getConfiguration().getNodeID();
            writer.printf("proposals-%s,%s\n", id, data.get("proposals-" + id).stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(",")));
        });

        writer.printf("riskCount-total,%s\n", data.get("riskCount-total").stream()
                .map(Object::toString)
                .collect(Collectors.joining(",")));
        agents.forEach(agent -> {
            var id = agent.getConfiguration().getNodeID();
            writer.printf("riskCount-%s,%s\n", id, data.get("riskCount-" + id).stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(",")));
        });

        writer.printf("riskUnique-total,%s\n", data.get("riskUnique-total").stream()
                .map(Object::toString)
                .collect(Collectors.joining(",")));
        writer.close();
    }

    public static TimerTask registerMetricCollection(List<ExperimentalAgent> agents, HashMap<String, List<Integer>> csv) {
        var messageCount = new HashMap<IAgent, AtomicInteger>();
        var proposalCount = new HashMap<IAgent, AtomicInteger>();
        var riskCount = new HashMap<IAgent, AtomicInteger>();
        var riskUnique = new HashMap<String, RiskReport>();

//        log.debug("Registering metric events");
        agents.forEach(agent -> {
            agent.getMessageBroker().onMessage(message -> {
                var count = messageCount.computeIfAbsent(agent, k -> new AtomicInteger());
                count.incrementAndGet();
            });
            agent.getEventManager().registerEventHandler(ApplyProposalEvent.class, event -> {
                var count = proposalCount.computeIfAbsent(agent, k -> new AtomicInteger());
                count.incrementAndGet();
            });
            agent.getEventManager().registerEventHandler(FoundRiskEvent.class, event -> {
                var count = riskCount.computeIfAbsent(agent, k -> new AtomicInteger());
                count.incrementAndGet();

                riskUnique.put(event.getRiskReport().toString(), event.getRiskReport());
            });
        });

        csv.put("messages-total", new ArrayList<>());
        csv.put("proposals-total", new ArrayList<>());
        csv.put("riskCount-total", new ArrayList<>());
        csv.put("riskUnique-total", new ArrayList<>());
        agents.forEach(agent -> {
            var id = agent.getConfiguration().getNodeID();
            csv.put("messages-" + id, new ArrayList<>());
            csv.put("proposals-" + id, new ArrayList<>());
            csv.put("riskCount-" + id, new ArrayList<>());
//            csv.put("riskUnique-" + id, new ArrayList<>());
        });

        return new TimerTask() {

            @Override
            public void run() {
                csv.compute("messages-total", (k, list) -> {
                    list.add(messageCount.values().stream().mapToInt(AtomicInteger::get).sum());
                    return list;
                });
                csv.compute("proposals-total", (k, list) -> {
                    list.add(proposalCount.values().stream().mapToInt(AtomicInteger::get).sum());
                    return list;
                });
                csv.compute("riskCount-total", (k, list) -> {
                    list.add(riskCount.values().stream().mapToInt(AtomicInteger::get).sum());
                    return list;
                });
                csv.compute("riskUnique-total", (k, list) -> {
                    list.add(riskUnique.size());
                    return list;
                });
                agents.forEach(agent -> {
                    var id = agent.getConfiguration().getNodeID();
                    csv.compute("messages-" + id, (k, list) -> {
                        list.add(messageCount.getOrDefault(agent, new AtomicInteger(0)).get());
                        return list;
                    });
                    csv.compute("proposals-" + id, (k, list) -> {
                        list.add(proposalCount.getOrDefault(agent, new AtomicInteger(0)).get());
                        return list;
                    });
                    csv.compute("riskCount-" + id, (k, list) -> {
                        list.add(riskCount.getOrDefault(agent, new AtomicInteger(0)).get());
                        return list;
                    });
                });
            }
        };
    }


    public static void a1() {
        var infrastructure = InfrastructureLoader.loadFromYaml(ExperimentRunner.infrastructureFile);
        var messageDispatcher = new EventDispatcher<Envelope>();

        ExperimentRunner.runTest(infrastructure, new NoCommunicationFeatureSet(), new NoChangeScenario(infrastructure, messageDispatcher));
    }

    public static void b1() {
        var infrastructure = InfrastructureLoader.loadFromYaml(ExperimentRunner.infrastructureFile);
        var messageDispatcher = new EventDispatcher<Envelope>();

        ExperimentRunner.runTest(infrastructure, new NoAuctionFeatureSet(messageDispatcher), new NoChangeScenario(infrastructure, messageDispatcher));
    }

    public static void c1() {
        var infrastructure = InfrastructureLoader.loadFromYaml(ExperimentRunner.infrastructureFile);
        var messageDispatcher = new EventDispatcher<Envelope>();

        ExperimentRunner.runTest(infrastructure, new FullFeatureSet(messageDispatcher), new NoChangeScenario(infrastructure, messageDispatcher));
    }
}

