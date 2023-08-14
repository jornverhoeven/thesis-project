package tech.jorn.adrian.experiment;


import tech.jorn.adrian.core.observables.EventDispatcher;
import tech.jorn.adrian.experiment.messages.Envelope;

public class ExperimentTwo {
    public static void main(String[] args) {
        var infrastructure = InfrastructureLoader.loadFromYaml("./simple-infra.yml");
        var messageDispatcher = new EventDispatcher<Envelope>();
        var agents = AgentFactory.fromInfrastructure(infrastructure, messageDispatcher);

        agents.forEach(ExperimentalAgent::shareKnowledge);
    }
}
