package tech.jorn.adrian.core.risks;

import tech.jorn.adrian.core.graphs.knowledgebase.KnowledgeBase;

import java.util.function.Consumer;

public abstract class RiskRule {
    public abstract void evaluate(KnowledgeBase knowledgeBase, Consumer<RiskEdge> attackGraph);
}

