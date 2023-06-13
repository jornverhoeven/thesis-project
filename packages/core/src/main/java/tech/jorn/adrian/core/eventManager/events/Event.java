package tech.jorn.adrian.core.eventManager.events;

import java.io.Serializable;
import java.util.Date;

public abstract class Event implements Serializable {
    private Date time;

    Event(Date time) {
        this.time = time;
    }

    public Date getTime() {
        return this.time;
    }
}
