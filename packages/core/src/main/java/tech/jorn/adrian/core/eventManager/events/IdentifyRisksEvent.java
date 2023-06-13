package tech.jorn.adrian.core.eventManager.events;

import java.util.Date;

public class IdentifyRisksEvent extends Event {
    public IdentifyRisksEvent() {
        this(new Date());
    }
    public IdentifyRisksEvent(Date time) {
        super(time);
    }
}
