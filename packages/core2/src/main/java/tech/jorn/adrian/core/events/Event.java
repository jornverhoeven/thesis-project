package tech.jorn.adrian.core.events;

import java.io.Serializable;
import java.util.Date;

public abstract class Event implements Serializable {
    private Date time;

    protected Event() {
        this(new Date());
    }
    protected Event(Date time) {
        this.time = time;
    }

    public Date getTime() {
        return this.time;
    }
}
