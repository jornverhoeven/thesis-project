package tech.jorn.adrian.agent;

import org.yaml.snakeyaml.Yaml;
import tech.jorn.adrian.core.graph.INode;
import tech.jorn.adrian.core.infrastructure.Node;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class AgentConfigurationLoader {
    static AgentConfiguration load(String filePath) {
        var yaml = new Yaml();
        InputStream inputStream = AgentConfigurationLoader.class
                .getClassLoader()
                .getResourceAsStream(filePath);
        Map<String, Object> obj = yaml.load(inputStream);

        var self = (Map<String, Object>) obj.get("self");
        var upstream = (List<Map<String, Object>>) obj.get("upstream");

        var parent = new Node((String) self.get("id"));
        var neighbours = upstream.stream()
                .map(info -> (INode) new Node((String) info.get("id")))
                .toList();

        var configuration = new AgentConfiguration(parent, neighbours, 10000);
        return configuration;
    }
}
