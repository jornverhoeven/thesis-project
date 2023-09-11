package tech.jorn.adrian.experiment.scenarios;

import tech.jorn.adrian.core.graphs.infrastructure.Infrastructure;
import tech.jorn.adrian.core.observables.EventDispatcher;
import tech.jorn.adrian.experiment.messages.Envelope;

public class UnstableInfrastructureScenario extends Scenario {
    public UnstableInfrastructureScenario(Infrastructure infrastructure, EventDispatcher<Envelope> messageDispatcher) {
        super(infrastructure, messageDispatcher, 1000);
    }

    @Override
    public void onScheduleEvents() {

    }
}
