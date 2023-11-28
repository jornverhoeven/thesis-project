package tech.jorn.adrian.experiment.scenarios;

import tech.jorn.adrian.core.graphs.infrastructure.Infrastructure;
import tech.jorn.adrian.core.observables.EventDispatcher;
import tech.jorn.adrian.experiment.instruments.ExperimentalAgent;
import tech.jorn.adrian.experiment.messages.Envelope;

import java.util.List;
import java.util.Queue;

public class MixedScenario extends Scenario {
    public MixedScenario(Infrastructure infrastructure, EventDispatcher<Envelope> messageDispatcher) {
        super(infrastructure, messageDispatcher, 1000);
    }

    @Override
    public void onScheduleEvents(Queue<ExperimentalAgent> agents) {

    }
}
