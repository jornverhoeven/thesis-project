package tech.jorn.adrian.core.utils;

import tech.jorn.adrian.core.graph.IEdge;
import tech.jorn.adrian.core.graph.IGraph;
import tech.jorn.adrian.core.graph.INode;

import java.util.stream.Collectors;

public class MermaidGraphGenerator<N extends INode, E extends IEdge<N>> {
    public String print(IGraph<N, E> graph) {
        var result = "flowchart LR\n";

        result += graph.getNodes().stream()
                .map(this::printNode)
                .collect(Collectors.joining("\n"));

        result += "\n\n";
        result += graph.getEdges().stream()
                .map(this::printEdge)
                .collect(Collectors.joining("\n"));

        return result + "\n";
    }

    protected String printNode(N node) {
        return String.format("\t%s[%s]", node.getID(), node.getName());
    }

    protected String printEdge(E edge) {
        return String.format("\t%s --> %s", edge.getParent().getID(), edge.getChild().getID());
    }
}
