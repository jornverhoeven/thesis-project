package tech.jorn.adrian.core.assets;

public class SoftwareComponent extends SoftwareAsset {
    public SoftwareComponent(String id) {
        super(id, "Software Component " + id);
    }
    public SoftwareComponent(String id, String name) {
        super(id, name);
    }
}
