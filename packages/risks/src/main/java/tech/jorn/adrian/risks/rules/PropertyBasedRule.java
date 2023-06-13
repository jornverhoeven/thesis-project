package tech.jorn.adrian.risks.rules;

import tech.jorn.adrian.core.knowledge.IKnowledgeBase;
import tech.jorn.adrian.core.risks.Risk;
import tech.jorn.adrian.core.risks.RiskRule;
import tech.jorn.adrian.core.risks.RiskType;
import tech.jorn.adrian.core.risks.graph.AttackGraph;
import tech.jorn.adrian.core.risks.graph.RiskEdge;
import tech.jorn.adrian.risks.types.RiskInfrastructureNodeHasFirewall;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public abstract class PropertyBasedRule<R extends RiskType> extends RiskRule {

    private final String property;
    private final Class<R> riskClass;

    protected PropertyBasedRule(String property, Class<R> riskClass) {
        this.property = property;
        this.riskClass = riskClass;
    }

    @Override
    public List<RiskEdge> evaluate(IKnowledgeBase knowledgeBase, AttackGraph attackGraph) {
        var risks = new ArrayList<RiskEdge>();

        knowledgeBase.getNodes().forEach(node -> {
            var parents = knowledgeBase.getParents(node);

            parents.forEach(parent -> {
                if (parent.getID().equals(node.getID())) return;

                Risk risk;
                try {
                    var riskType = this.riskClass.getDeclaredConstructor().newInstance();
                    var hasProperty = (boolean) node.getProperty(this.property)
                            .orElse(false);
                    if (hasProperty) {
                        risk = new Risk(riskType, riskType.getMitigatedRiskFactor());
                    } else
                        risk = new Risk(riskType, riskType.getUnmitigatedRiskFactor());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                var edge = new RiskEdge(
                        attackGraph.findNode(node).get(), // TODO: Do some actual checking before calling
                        attackGraph.findNode(parent).get()
                );
                edge.getRisks().add(risk);
                risks.add(edge);
            });
        });
        return risks;
    }
}
