package tech.jorn.adrian.core.graph;

import tech.jorn.adrian.core.infrastructure.Node;
import tech.jorn.adrian.core.observables.ValueDispatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractGraph<N extends INode, E extends IEdge<N>> implements IGraph<N, E> {
    protected ValueDispatcher<List<N>> nodes = new ValueDispatcher<>(new ArrayList<>());
    protected ValueDispatcher<List<E>> edges = new ValueDispatcher<>(new ArrayList<>());

    @Override
    public List<N> getNodes() {
        return this.nodes.current();
    }

    @Override
    public List<E> getEdges() {
        return this.edges.current();
    }

    @Override
    public Optional<N> getNode(String nodeId) {
        return this.nodes.current().stream()
                .filter(n -> n.getID().equals(nodeId))
                .findAny();
    }

    @Override
    public List<N> getParents(N node) {
        return this.getParents(node.getID());
    }

    @Override
    public List<N> getParents(String nodeId) {
        return this.edges.current().stream()
                .filter(e -> e.getChild().getID().equals(nodeId))
                .map(IEdge::getParent)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public List<N> getChildren(N node) {
        return this.getChildren(node.getID());
    }

    @Override
    public List<N> getChildren(String nodeId) {
        return this.edges.current().stream()
                .filter(e -> e.getParent().getID().equals(nodeId))
                .map(IEdge::getChild)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public List<N> getNeighbours(N node) {
        return this.getNeighbours(node.getID());
    }

    @Override
    public List<N> getNeighbours(String nodeId) {
        return this.edges.current().stream()
                .filter(e -> e.getChild().getID().equals(nodeId) || e.getParent().getID().equals(nodeId))
                .flatMap(e -> Stream.of(e.getParent(), e.getChild()))
                .distinct()
                .filter(n -> !n.getID().equals(nodeId))
                .collect(Collectors.toList());
    }

    public List<N> depthFirstSearch(N from, N to) {
        return depthFirstSearch(from, to, new ArrayList<>(), new ArrayList<>());
    }
    public List<N> depthFirstSearch(N from, N to, List<N> stack, List<N> visited) {
        stack.add(from);
        visited.add(from);

        if ( from.getID().equals(to.getID()) ) {
            return stack;
        } else {
            for (var child : this.getChildren((N) from)) {
                if (!visited.contains(child)) {
                    var result = this.depthFirstSearch(child, to, stack, visited);
                    if (!result.isEmpty()) return result;
                }
            }
        }
        visited.remove(from);
        stack.remove(from);
        return stack;// TODO: Maybe return []?
    }

}
