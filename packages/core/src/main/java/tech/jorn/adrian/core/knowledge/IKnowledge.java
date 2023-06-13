package tech.jorn.adrian.core.knowledge;

import tech.jorn.adrian.core.observables.SubscribableValueEvent;

import java.util.Date;

public interface IKnowledge {
    SubscribableValueEvent<Date> getUpdatedAt();
    void setUpdatedAt(Date updatedAt);

    KnowledgeOrigin getUpdateOrigin();
    void setUpdateOrigin(KnowledgeOrigin origin);
}
