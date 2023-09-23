package tech.jorn.adrian.experiment.scenarios;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.jorn.adrian.core.graphs.infrastructure.Infrastructure;
import tech.jorn.adrian.core.observables.EventDispatcher;
import tech.jorn.adrian.experiment.messages.Envelope;

public class IntroduceRiskScenario extends Scenario {
    Logger log = LogManager.getLogger(IntroduceRiskScenario.class);

    public IntroduceRiskScenario(Infrastructure infrastructure, EventDispatcher<Envelope> messageDispatcher) {
        super(infrastructure, messageDispatcher, 20 * 60 * 1000 /* Twenty Minutes */);
    }

    @Override
    public void onScheduleEvents() {
        this.after(3 * 60 * 1000, () -> {
            var asset = infrastructure.listSoftwareAssets().get(1);
            this.log.info("Introducing Risk to asset {}", asset.getID());
            asset.setProperty("sdk-google/cloud_iot_device_sdk_for_embedded_c-version", "1.0.2");

            var node = infrastructure.listNodes().get(0);
            this.log.info("Introducing Risk to node {}", node.getID());
            node.setProperty("firmware-qualcomm/apq8096-version", "2.1.2");

        });
        this.after(4 * 60 * 1000, () -> {
            this.log.debug("Waiting for silence");
            this.waitForSilence(10 * 1000, this.finished::raise);
        });
    }
}
