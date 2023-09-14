package tech.jorn.adrian.risks.rules.cves;

import tech.jorn.adrian.core.graphs.knowledgebase.KnowledgeBase;
import tech.jorn.adrian.core.risks.RiskEdge;
import tech.jorn.adrian.risks.validators.PropertyValidator;

import java.util.function.Consumer;

public class EntryCve<T> extends CveRule<T> {
    public EntryCve(String cve, String property, PropertyValidator<T> validator, float exploitabilityScore) {
        super(cve, property, validator, exploitabilityScore);
    }

    @Override
    public void evaluate(KnowledgeBase knowledgeBase, Consumer<RiskEdge> attackGraph) {

    }
}
