package tech.jorn.adrian.core.risks.detection;

import java.util.List;
import java.util.Optional;

public class HighestDamageSelector implements IRiskSelector {
    @Override
    public Optional<RiskReport> select(List<RiskReport> risks) {
        return risks.stream()
                .max((a, b) -> Float.compare(a.getDamage(), b.getDamage()));
    }
}
