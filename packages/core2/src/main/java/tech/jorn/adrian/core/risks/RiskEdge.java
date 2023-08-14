package tech.jorn.adrian.core.risks;

import tech.jorn.adrian.core.graphs.risks.AttackGraphEntry;

public record RiskEdge(AttackGraphEntry<?> from, AttackGraphEntry<?> to, Risk risk) {
}
