package tech.jorn.adrian.core.mitigations;

import tech.jorn.adrian.core.assets.SoftwareAsset;
import tech.jorn.adrian.core.graph.INode;

public class Migration extends Mitigation {
    private final SoftwareAsset asset;
    private final INode target;

    public Migration(SoftwareAsset asset, INode target) {
        super();
        this.asset = asset;
        this.target = target;
    }
}
