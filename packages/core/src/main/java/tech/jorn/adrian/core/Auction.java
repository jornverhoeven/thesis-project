package tech.jorn.adrian.core;

import tech.jorn.adrian.core.risks.detection.RiskReport;

import java.util.Date;
import java.util.List;

public class Auction {
    private final String id;
    private final Date startDate;
    private final IIdentifiable host;
    private final List<IIdentifiable> participants;

    public Auction(String id, IIdentifiable host, List<IIdentifiable> participants, RiskReport riskReport) {
        this.id = id;
        this.host = host;
        this.participants = participants;
        this.startDate = new Date();
    }

    public String getId() {
        return id;
    }

    public IIdentifiable getHost() {
        return this.host;
    }

    public Date getStartDate() {
        return startDate;
    }

    public List<IIdentifiable> getParticipants() {
        return participants;
    }
}

