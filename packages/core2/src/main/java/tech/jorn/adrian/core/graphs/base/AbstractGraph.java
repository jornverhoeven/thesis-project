package tech.jorn.adrian.core.graphs.base;

import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractGraph<N extends INode, L extends GraphLink<N>> implements IGraph<N> {
    protected final Set<N> nodes = new HashSet<>();
    protected final Map<N, List<L>> adjacent = new HashMap<>();

    @Override
    public void upsertNode(N node) {
        nodes.add(node); // This might be a bit extra ash we are updating the keys of the map
        adjacent.put(node, adjacent.getOrDefault(node, new ArrayList<>()));
    }

    @Override
    public void removeNode(N node) {
        this.nodes.remove(node);
        this.adjacent.values().forEach(link -> link.removeIf(l -> node.equals(l.getNode())));
        this.adjacent.remove(node);
    }

    @Override
    public void removeNode(String id) {
        var node = this.findById(id);
        node.ifPresent(this::removeNode);
    }

    @Override
    public void addEdge(N from, N to) {
        var adj = this.adjacent.getOrDefault(from, new ArrayList<>());
        adj.add((L) new GraphLink<>(to));
        this.adjacent.put(from, adj);
    }

    @Override
    public Optional<N> findById(String id) {
        return this.adjacent.keySet().stream()
                .filter(n -> n.getID().equals(id))
                .findFirst();
    }

    @Override
    public List<N> getNeighbours(N node) {
        return this.adjacent.get(node).stream()
                .map(GraphLink::getNode)
                .collect(Collectors.toList());
    }

    @Override
    public List<N> getNeighbours(String id) {
        var node = this.findById(id);
        return node
                .map(this::getNeighbours)
                .orElse(null);
    }

    public List<N> getParents(N node) {
        var parents = new ArrayList<N>();
        this.adjacent.forEach((key, children) -> {
            if (children.contains(node)) parents.add(key);
        });
        return parents;
    }

    public List<N> getParents(String id) {
        var node = this.findById(id);
        if (node.isEmpty()) return List.of();
        return this.getParents(node.get());
    }

    @Override
    public Set<N> getNodes() {
        return this.nodes;
    }
}

