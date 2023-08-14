package tech.jorn.adrian.agent;

import java.util.Set;
import java.util.stream.Collectors;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import tech.jorn.adrian.core.mitigations.Mutation;
import tech.jorn.adrian.core.risks.RiskRule;
import tech.jorn.adrian.risks.mitigations.IMutator;

public class ResourceLoader {
    private final String PACKAGE = "tech.jorn.adrian";

    public Set<Class<? extends RiskRule>> findAllRiskRules() {
        Reflections reflections = new Reflections(PACKAGE, new SubTypesScanner(false));
        return reflections.getSubTypesOf(RiskRule.class)
                .stream()
                .filter(c -> !c.isInterface() && !c.getSimpleName().equals("PropertyBasedRule"))
                .collect(Collectors.toSet());
    }

    public Set<Class<? extends IMutator>> findAllMutators() {
        Reflections reflections = new Reflections(PACKAGE, new SubTypesScanner(false));
        return reflections.getSubTypesOf(IMutator.class)
                .stream()
                .filter(c -> !c.isInterface())
                .collect(Collectors.toSet());
    }

}
