package tech.jorn.adrian.core.utils;

import tech.jorn.adrian.core.graph.IGraph;
import tech.jorn.adrian.core.knowledge.KEdge;
import tech.jorn.adrian.core.knowledge.KNode;
import tech.jorn.adrian.core.knowledge.KnowledgeEntry;
import tech.jorn.adrian.core.knowledge.KnowledgeOrigin;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MermaidKnowledgeBaseGenerator extends MermaidGraphGenerator<KnowledgeEntry, KEdge> {

    @Override
    public String print(IGraph<KnowledgeEntry, KEdge> graph) {
        var result = super.print(graph);


        // Render Assets
        result += "\n";
        result += graph.getNodes().stream().flatMap(n -> {
            if (!(n instanceof KNode)) return Stream.empty();
            return ((KNode) n).getSoftwareAssets().stream().map(softwareAsset -> {
                return String.format("\t%s([%s])\n\t%s --> %s", softwareAsset.getID(), softwareAsset.getName(), n.getID(), softwareAsset.getID());
            });
        }).collect(Collectors.joining("\n"));

        // Render asset properties
        result += "\n\n";
        result += graph.getNodes().stream().flatMap(n -> {
            if (!(n instanceof KNode)) return Stream.empty();
            return ((KNode) n).getSoftwareAssets().stream().flatMap(softwareAsset -> {
                return softwareAsset.getProperties().stream().map(prop -> {
                    var key = softwareAsset.getID() + ":" + prop.getName();
                    return String.format("\t%05x{{%s : %s}}\n\t%05x -.-o %s", key.hashCode(), prop.getName(), prop.getValue(), key.hashCode(), softwareAsset.getID());
                });
            });
        }).collect(Collectors.joining("\n"));

        return result + "\n";
    }

    @Override
    protected String printNode(KnowledgeEntry node) {
        if (node.getUpdateOrigin().equals(KnowledgeOrigin.Derived)) {
            return super.printNode(node) +
                    String.format("\n\tstyle %s stroke-dasharray: 5 5", node.getID());
        }
        return super.printNode(node);
    }

    @Override
    protected String printEdge(KEdge edge) {
        if (edge.getChild().getUpdateOrigin().equals(KnowledgeOrigin.Derived)
                || edge.getParent().getUpdateOrigin().equals(KnowledgeOrigin.Derived))
            return String.format("\t%s -.-> %s", edge.getParent().getID(), edge.getChild().getID());
//        if (edge.getUpdateOrigin().equals(KnowledgeOrigin.Direct))
//            return String.format("\t%s ==> %s", edge.getParent().getID(), edge.getChild().getID());
//        if (edge.getUpdateOrigin().equals(KnowledgeOrigin.Indirect))
//            return String.format("\t%s -.-> %s", edge.getParent().getID(), edge.getChild().getID());
        return super.printEdge(edge);
    }
}
