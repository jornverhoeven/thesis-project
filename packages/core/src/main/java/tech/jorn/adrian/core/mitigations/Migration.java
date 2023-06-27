package tech.jorn.adrian.core.mitigations;

import tech.jorn.adrian.core.assets.SoftwareAsset;
import tech.jorn.adrian.core.graph.INode;

public class Migration extends Mutation {
    private final SoftwareAsset asset;
    private final INode source;
    private final INode target;

    public Migration(SoftwareAsset asset, INode source, INode target) {
        super();
        this.asset = asset;
        this.source = source;
        this.target = target;
    }
}
