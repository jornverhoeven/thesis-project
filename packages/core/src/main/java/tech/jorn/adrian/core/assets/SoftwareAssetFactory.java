package tech.jorn.adrian.core.assets;

import java.util.HashMap;
import java.util.Map;

public class SoftwareAssetFactory {

    public static SoftwareAsset fromMap(Map<String, Object> data) {
        var properties = new HashMap<>(data);
        properties.remove("id");
        properties.remove("name");
        properties.remove("type");
        properties.remove("host");

        SoftwareAsset asset = null;
        switch ((String) data.get("type")) {
            case "software": asset = new SoftwareComponent((String) data.get("id"), (String) data.get("name"));
        }

        if (asset != null) {
            asset.setProperties(properties);
        }
        return asset;
    }
}
