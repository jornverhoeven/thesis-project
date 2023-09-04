package tech.jorn.adrian.experiment;


import tech.jorn.adrian.core.observables.EventDispatcher;
import tech.jorn.adrian.experiment.instruments.ExperimentalAgent;
import tech.jorn.adrian.experiment.messages.Envelope;

public class SingleNodeInfra {
    public static void main(String[] args) throws InterruptedException {
        var infrastructure = InfrastructureLoader.loadFromYaml("./single-node-infra.yml");
        var messageDispatcher = new EventDispatcher<Envelope>();
        var agents = AgentFactory.fromInfrastructure(infrastructure, messageDispatcher);
        var agent = agents.get(0);

        agents.forEach(ExperimentalAgent::shareKnowledge);

//        agents.get(0).identifyRisk();
        Thread.sleep(4000);
        agents.get(1).identifyRisk();
    }

    private static class VoidMessageDispatcher extends EventDispatcher<Envelope> {
        @Override
        public void dispatch(Envelope value) {
            System.out.println("nothing");
            // Do nothing!
        }
    }
}

