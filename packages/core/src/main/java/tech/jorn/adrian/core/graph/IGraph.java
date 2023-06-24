package tech.jorn.adrian.core.graph;

import java.util.List;
import java.util.Optional;

public interface IGraph<N extends INode, E extends IEdge<N>> {
    List<N> getNodes();
    List<E> getEdges();

    Optional<N> getNode(String nodeId);

    List<N> getParents(N node);
    List<N> getParents(String nodeId);

    List<N> getChildren(N node);
    List<N> getChildren(String nodeId);

    List<N> getNeighbours(N node);
    List<N> getNeighbours(String nodeId);
}
