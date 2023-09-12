package tech.jorn.adrian.experiment;


import tech.jorn.adrian.core.observables.EventDispatcher;
import tech.jorn.adrian.experiment.features.AgentFactory;
import tech.jorn.adrian.experiment.instruments.ExperimentalAgent;
import tech.jorn.adrian.experiment.messages.Envelope;
import tech.jorn.adrian.experiment.scenarios.GrowingInfrastructureScenario;

public class ExperimentRunner {
    public static void main(String[] args) throws InterruptedException {
        var infrastructure = InfrastructureLoader.loadFromYaml("./simple-infra.yml");
        var messageDispatcher = new EventDispatcher<Envelope>();

        var scenario = new GrowingInfrastructureScenario(infrastructure, messageDispatcher, node -> AgentFactory.fromNode(infrastructure, node, messageDispatcher));
        scenario.scheduleEvents();

        var agents = AgentFactory.fromInfrastructure(infrastructure, messageDispatcher);
        agents.forEach(ExperimentalAgent::start);

        scenario.onFinished().subscribe(() -> {
            System.out.println("Done!");
            System.exit(0);
        });
    }
}

