package tech.jorn.adrian.core.risks.detection;

import java.util.List;
import java.util.Optional;

public interface IRiskSelector {
    Optional<RiskReport> select(List<RiskReport> risks);
}
