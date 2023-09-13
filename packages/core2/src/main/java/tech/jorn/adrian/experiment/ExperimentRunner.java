package tech.jorn.adrian.experiment;


import tech.jorn.adrian.core.graphs.infrastructure.Infrastructure;
import tech.jorn.adrian.core.observables.EventDispatcher;
import tech.jorn.adrian.experiment.features.*;
import tech.jorn.adrian.experiment.instruments.ExperimentalAgent;
import tech.jorn.adrian.experiment.messages.Envelope;
import tech.jorn.adrian.experiment.scenarios.GrowingInfrastructureScenario;
import tech.jorn.adrian.experiment.scenarios.NoChangeScenario;
import tech.jorn.adrian.experiment.scenarios.Scenario;

public class ExperimentRunner {
    private static final String infrastructureFile = "./simple-infra.yml";

    public static void main(String[] args) throws InterruptedException {
//        a1();
//        b1();
        b1();
    }


    public static void runTest(Infrastructure infrastructure, FeatureSet featureSet, Scenario scenario) {

        var agentFactory = new AgentFactory(featureSet);

        scenario.scheduleEvents();

        var agents = agentFactory.fromInfrastructure(infrastructure);
        agents.forEach(ExperimentalAgent::start);

        scenario.onFinished().subscribe(() -> {
            System.out.println("Done!");
            System.exit(0);
        });
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

