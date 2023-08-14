package tech.jorn.adrian.experiment;


import tech.jorn.adrian.core.observables.EventDispatcher;
import tech.jorn.adrian.experiment.messages.Envelope;

public class SingleNodeInfra {
    public static void main(String[] args) {
        var infrastructure = InfrastructureLoader.loadFromYaml("./single-node-infra.yml");
        var messageDispatcher = new VoidMessageDispatcher();
        var agents = AgentFactory.fromInfrastructure(infrastructure, messageDispatcher);
        var agent = agents.get(0);

        agent.identifyRisk();
    }


    private static class VoidMessageDispatcher extends EventDispatcher<Envelope> {
        @Override
        public void dispatch(Envelope value) {
            System.out.println("nothing");
            // Do nothing!
        }
    }
}

