package tech.jorn.adrian.experiment;


import tech.jorn.adrian.core.observables.EventDispatcher;
import tech.jorn.adrian.experiment.features.AgentFactory;
import tech.jorn.adrian.experiment.instruments.ExperimentalAgent;
import tech.jorn.adrian.experiment.messages.Envelope;
import tech.jorn.adrian.experiment.scenarios.NoChangeScenario;

public class SingleNodeInfra {
    public static void main(String[] args) throws InterruptedException {
        var infrastructure = InfrastructureLoader.loadFromYaml("./single-node-infra.yml");
        var messageDispatcher = new EventDispatcher<Envelope>();
        var scenario = new NoChangeScenario(infrastructure, messageDispatcher);

        scenario.scheduleEvents();

        var agents = AgentFactory.fromInfrastructure(infrastructure, messageDispatcher);
        agents.forEach(ExperimentalAgent::start);

        scenario.onFinished().subscribe(() -> {
            System.out.println("Done!");
            System.exit(0);
        });
    }
}

