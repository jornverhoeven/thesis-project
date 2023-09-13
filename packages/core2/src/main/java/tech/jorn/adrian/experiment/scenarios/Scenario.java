package tech.jorn.adrian.experiment.scenarios;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.jorn.adrian.agent.AdrianAgent;
import tech.jorn.adrian.core.agents.IAgent;
import tech.jorn.adrian.core.graphs.infrastructure.Infrastructure;
import tech.jorn.adrian.core.observables.*;
import tech.jorn.adrian.experiment.messages.Envelope;

import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Supplier;

public abstract class Scenario {
    Logger log = LogManager.getLogger(Scenario.class);

    private final int maxExecutionTime;
    protected final Infrastructure infrastructure;
    protected final EventDispatcher<Envelope> messageDispatcher;


    protected final FlagDispatcher finished = new FlagDispatcher();
    protected final EventDispatcher<IAgent> newAgent = new EventDispatcher<>();
    protected final EventDispatcher<IAgent> removeAgent = new EventDispatcher<>();
    private Timer timeoutHandle = null;

    public Scenario(Infrastructure infrastructure, EventDispatcher<Envelope> messageDispatcher, int maxExecutionTime) {
        this.infrastructure = infrastructure;
        this.messageDispatcher = messageDispatcher;
        this.maxExecutionTime = maxExecutionTime;
    }

    private void scheduleExecutionTimeout() {
        this.after(this.maxExecutionTime, () -> {
            this.log.error("The scenario did not executing within the time limit of {}ms", this.maxExecutionTime);
            System.exit(1);
        });
    }

    protected void waitForSilence(int timeout, Runnable onSilence) {
        Supplier<TimerTask> taskFactory = () -> new TimerTask() {
            @Override
            public void run() {
                onSilence.run();
            }
        };
        this.timeoutHandle = new Timer();
        this.timeoutHandle.schedule(taskFactory.get(), timeout);

        this.messageDispatcher.subscribable.subscribe(() -> {
            this.timeoutHandle.cancel();
            this.timeoutHandle.purge();
            this.timeoutHandle = new Timer();
            this.timeoutHandle.schedule(taskFactory.get(), timeout);
        });
    }

    protected void after(int delay, Runnable action) {
        var timer = new Timer();
        var task = new TimerTask() {
            @Override
            public void run() {
                action.run();
            }
        };
        timer.schedule(task, delay);
    }

    public void scheduleEvents() {
        this.log.debug("Scheduling scenario events for {}", this.getClass().getSimpleName());

        this.scheduleExecutionTimeout();
        this.onScheduleEvents();

        this.log.info("All events are scheduler for {}", this.getClass().getSimpleName());
    }

    public abstract void onScheduleEvents();

    public SubscribableFlagEvent onFinished() {
        return this.finished.subscribable;
    }
    public boolean isFinished() {
        return this.finished.isRaised();
    }

    public SubscribableEvent<IAgent> onNewAgent() {
        return this.newAgent.subscribable;
    }
    public SubscribableEvent<IAgent> onAgentRemoved() {
        return this.removeAgent.subscribable;
    }
}
