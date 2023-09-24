package tech.jorn.adrian.experiment;


import org.apache.logging.log4j.LogManager;
import tech.jorn.adrian.agent.events.ApplyProposalEvent;
import tech.jorn.adrian.agent.events.FoundRiskEvent;
import tech.jorn.adrian.agent.services.BasicRiskDetection;
import tech.jorn.adrian.core.agents.IAgent;
import tech.jorn.adrian.core.agents.IAgentConfiguration;
import tech.jorn.adrian.core.graphs.MermaidGraphRenderer;
import tech.jorn.adrian.core.graphs.base.GraphLink;
import tech.jorn.adrian.core.graphs.infrastructure.Infrastructure;
import tech.jorn.adrian.core.graphs.infrastructure.InfrastructureEntry;
import tech.jorn.adrian.core.graphs.infrastructure.InfrastructureNode;
import tech.jorn.adrian.core.graphs.knowledgebase.KnowledgeBase;
import tech.jorn.adrian.core.graphs.knowledgebase.KnowledgeBaseNode;
import tech.jorn.adrian.core.graphs.knowledgebase.KnowledgeBaseSoftwareAsset;
import tech.jorn.adrian.core.graphs.risks.AttackGraph;
import tech.jorn.adrian.core.graphs.risks.AttackGraphEntry;
import tech.jorn.adrian.core.graphs.risks.AttackGraphLink;
import tech.jorn.adrian.core.observables.EventDispatcher;
import tech.jorn.adrian.core.risks.RiskReport;
import tech.jorn.adrian.core.services.probability.ProductRiskProbability;
import tech.jorn.adrian.experiment.features.*;
import tech.jorn.adrian.experiment.instruments.ExperimentalAgent;
import tech.jorn.adrian.experiment.messages.Envelope;
import tech.jorn.adrian.experiment.scenarios.*;
import tech.jorn.adrian.risks.RiskLoader;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ExperimentRunner {
    private static int tick = 0;

    public static void main(String[] args) throws InterruptedException {
        System.out.println(Arrays.stream(args).collect(Collectors.joining(", ")));
        var file = args[0];
        var infrastructure = InfrastructureLoader.loadFromYaml(file);
        var messageDispatcher = new EventDispatcher<Envelope>();
        var features = getFeatureSet(args[2], messageDispatcher);
        var agentFactory = new AgentFactory(features);
        var scenario = getScenario(args[1], infrastructure, messageDispatcher, (node) -> agentFactory.fromNode(infrastructure, node));

        runTest(infrastructure, features, scenario);
    }

    public static Scenario getScenario(String input, Infrastructure infrastructure, EventDispatcher<Envelope> messageDispatcher, Function<InfrastructureNode, IAgent> agentFactory) {
        switch (input) {
            case "risk-introduction": return new IntroduceRiskScenario(infrastructure, messageDispatcher);
            case "growing": return new GrowingInfrastructureScenario(infrastructure, messageDispatcher, agentFactory);
            case "unstable": return new UnstableInfrastructureScenario(infrastructure, messageDispatcher);
            case "mixed": return new MixedScenario(infrastructure, messageDispatcher);
            case "no-chance":
            default:
                return new NoChangeScenario(infrastructure, messageDispatcher);
        }
    }

    public static FeatureSet getFeatureSet(String input, EventDispatcher<Envelope> messageDispatcher) {
        switch(input) {
            case "knowledge-sharing":
                return new NoAuctionFeatureSet(messageDispatcher);
            case "local":
                return new NoCommunicationFeatureSet();
            case "auctioning":
            case "full":
            default:
                return new FullFeatureSet(messageDispatcher);
        }
    }


    public static void runTest(Infrastructure infrastructure, FeatureSet featureSet, Scenario scenario) {
        var log = LogManager.getLogger(ExperimentRunner.class);

        calculateRisksInInfrastructure(infrastructure);
        renderInfrastructure(infrastructure);

        var startTime = new Date().getTime();

        var agentFactory = new AgentFactory(featureSet);

        scenario.scheduleEvents();

        log.debug("Creating agents");
        var agents = agentFactory.fromInfrastructure(infrastructure);

        var csv = new HashMap<String, List<Object>>();
        var task = registerMetricCollection(agents, infrastructure, csv);
        var timer = new Timer();
        timer.scheduleAtFixedRate(task, 5 * 1000, 5 * 1000);

        log.debug("Starting agents");

        Runnable onFinished = () ->  {
            log.info("Finished scenario in {}ms", new Date().getTime() - startTime);
            task.run(); // Collect the final measurements

            try {
                writeToCSV(agents, csv, new Date().getTime() - startTime);
                calculateRisksInInfrastructure(infrastructure);
                renderInfrastructure(infrastructure);
            } catch (IOException e) {
                log.error("Could not write measures");
            }
            System.exit(0);
        };
        scenario.onFinished().subscribe(onFinished);

        agents.forEach(IAgent::start);


    }

    public static void writeToCSV(List<ExperimentalAgent> agents, Map<String, List<Object>> data, long endTime) throws IOException {
        var fileWriter = new FileWriter("./metrics.csv");
        var writer = new PrintWriter(fileWriter);

        writer.printf("timestamps;%s;%s\n", IntStream.range(0, (int) (endTime / 5000)).mapToObj(i -> String.valueOf(5 * (i + 1) * 1000)).collect(Collectors.joining(";")), endTime);
        writer.printf("messages-total;%s\n", data.get("messages-total").stream()
                .map(Object::toString)
                .collect(Collectors.joining(";")));
        agents.forEach(agent -> {
            var id = agent.getConfiguration().getNodeID();
            writer.printf("messages-%s;%s\n", id, data.get("messages-" + id).stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(";")));
        });

        writer.printf("proposals-total;%s\n", data.get("proposals-total").stream()
                .map(Object::toString)
                .collect(Collectors.joining(";")));
        agents.forEach(agent -> {
            var id = agent.getConfiguration().getNodeID();
            writer.printf("proposals-%s;%s\n", id, data.get("proposals-" + id).stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(";")));
        });

        writer.printf("riskCount-global;%s\n", data.get("riskCount-global").stream()
                .map(Object::toString)
                .collect(Collectors.joining(";")));
        writer.printf("riskCount-total;%s\n", data.get("riskCount-total").stream()
                .map(Object::toString)
                .collect(Collectors.joining(";")));
        agents.forEach(agent -> {
            var id = agent.getConfiguration().getNodeID();
            writer.printf("riskCount-%s;%s\n", id, data.get("riskCount-" + id).stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(";")));
        });

        writer.printf("riskUnique-global;%s\n", data.get("riskUnique-global").stream()
                .map(Object::toString)
                .collect(Collectors.joining(";")));
        writer.printf("riskUnique-total;%s\n", data.get("riskUnique-total").stream()
                .map(Object::toString)
                .collect(Collectors.joining(";")));

        writer.printf("riskDamage-global;%s\n", data.get("riskDamage-global").stream()
                .map(Object::toString)
                .collect(Collectors.joining(";")));
        agents.forEach(agent -> {
            var id = agent.getConfiguration().getNodeID();
            writer.printf("riskDamage-%s;%s\n", id, data.get("riskDamage-" + id).stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(";")));
            writer.printf("riskDamage-%%-%s;%s\n", id, data.get("riskDamage-%-" + id).stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(";")));
        });

        writer.close();
    }

    public static TimerTask registerMetricCollection(List<ExperimentalAgent> agents, Infrastructure infrastructure, HashMap<String, List<Object>> csv) {
        var messageCount = new ConcurrentHashMap<IAgent, AtomicInteger>();
        var proposalCount = new HashMap<IAgent, AtomicInteger>();
        var riskCount = new HashMap<IAgent, AtomicInteger>();
        var riskUnique = new HashMap<String, RiskReport>();

//        log.debug("Registering metric events");
        agents.forEach(agent -> {
            agent.getMessageBroker().registerMessageHandler(message -> {
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
        csv.put("riskCount-global", new ArrayList<>());
        csv.put("riskUnique-total", new ArrayList<>());
        csv.put("riskUnique-global", new ArrayList<>());
        csv.put("riskDamage-global", new ArrayList<>());
        agents.forEach(agent -> {
            var id = agent.getConfiguration().getNodeID();
            csv.put("messages-" + id, new ArrayList<>());
            csv.put("proposals-" + id, new ArrayList<>());
            csv.put("riskCount-" + id, new ArrayList<>());
            csv.put("riskDamage-" + id, new ArrayList<>());
            csv.put("riskDamage-%-" + id, new ArrayList<>());
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

                var risks = calculateRisksInInfrastructure(infrastructure);
                var riskSet = new HashSet<String>();
                var riskDamageGlobal = risks.stream().mapToDouble(RiskReport::damage).sum();
                risks.forEach(risk -> riskSet.add(risk.toString()));

                csv.compute("riskCount-global", (k, list) -> {
                    list.add(risks.size());
                    return list;
                });
                csv.compute("riskUnique-global", (k, list) -> {
                    list.add(riskSet.size());
                    return list;
                });
                csv.compute("riskDamage-global", (k, list) -> {
                    list.add(String.format("%.2f", riskDamageGlobal));
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

                    var attackGraph = agent.getRiskDetection().getLastAttackGraph();
                    if (attackGraph == null) attackGraph = agent.getRiskDetection().createAttackGraph(agent.getKnowledgeBase());

                    var agentRisks = agent.getRiskDetection().identifyRisks(attackGraph);

                    csv.compute("riskCount-" + id, (k, list) -> {
                        list.add(agentRisks.size());
                        return list;
                    });
                    var riskDamage = agentRisks.stream().mapToDouble(RiskReport::damage).sum();
                    csv.compute("riskDamage-" + id, (k, list) -> {
                        list.add(String.format("%.2f", riskDamage));
                        return list;
                    });
                    csv.compute("riskDamage-%-" + id, (k, list) -> {
                        list.add(String.format("%.4f", riskDamage / riskDamageGlobal));
                        return list;
                    });
                });

                tick++;
            }
        };
    }

    private static List<RiskReport> calculateRisksInInfrastructure(Infrastructure infrastructure) {
        var knowledgeBase = new KnowledgeBase();
        infrastructure.listNodes().forEach(node -> {
            var knowledge = KnowledgeBaseNode.fromNode(node);
            knowledgeBase.upsertNode(knowledge);
        });
        infrastructure.listSoftwareAssets().forEach(asset -> {
            var knowledge = KnowledgeBaseSoftwareAsset.fromNode(asset);
            knowledgeBase.upsertNode(knowledge);
        });
        knowledgeBase.getNodes().forEach(node -> {
            var neighbours = infrastructure.getNeighbours(node.getID());
            neighbours.forEach(neighbour -> {
                var knowledgeNeighbour = knowledgeBase.findById(neighbour.getID()).get();
                knowledgeBase.addEdge(node, knowledgeNeighbour);
            });
        });

        var riskDetection = new BasicRiskDetection(RiskLoader.listRisks(), new ProductRiskProbability(), null);
        var attackGraph = riskDetection.createAttackGraph(knowledgeBase);
        var risks = riskDetection.identifyRisks(attackGraph);
//        risks.forEach(risk -> System.out.println(risk.toString()));

        renderAttackGraph(null, attackGraph, tick);
        return risks;
    }

    private static void renderAttackGraph(IAgentConfiguration configuration, AttackGraph graph, int graphCount) {
        var filename = String.format("./graphs/attackGraph-%s-%d.mmd", configuration == null ? "global" : configuration.getNodeID(), graphCount);
        try {
            var writer = new FileWriter(filename);
            var graphRender = new MermaidGraphRenderer<AttackGraphEntry<?>, AttackGraphLink<AttackGraphEntry<?>>>();
            var mmdGraph = graphRender.render(graph);
            writer.write("%% " + graphCount * 5000);
            writer.write(mmdGraph);
            writer.close();
        } catch (IOException e) {
            System.err.println("SOMETHING WENT WRONG OUTPUTTING GRAPH " + e.toString());
            throw new RuntimeException(e);
        }
    }

    private static void renderInfrastructure(Infrastructure infrastructure) {
        var filename = String.format("./graphs/infrastructure-%d.mmd", tick * 5000);
        try {
            var writer = new FileWriter(filename);
            var graphRender = new MermaidGraphRenderer<InfrastructureEntry<?>, GraphLink<InfrastructureEntry<?>>>();
            var mmdGraph = graphRender.render(infrastructure);
            writer.write("%% " + tick * 5000);
            writer.write(mmdGraph);
            writer.close();
        } catch (IOException e) {
            System.err.println("SOMETHING WENT WRONG OUTPUTTING GRAPH " + e.toString());
            throw new RuntimeException(e);
        }
    }
}

