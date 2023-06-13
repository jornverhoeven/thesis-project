package tech.jorn.adrian.experiments.simulation;

import tech.jorn.adrian.core.infrastructure.Link;
import tech.jorn.adrian.core.infrastructure.Node;
import tech.jorn.adrian.core.utils.MermaidGraphGenerator;

public class SimulationPrinter {
    public void print(Simulation simulation) {
        var mermaid = new MermaidGraphGenerator<Node, Link>();
        System.out.print(mermaid.print(simulation.getInfrastructure()));
    }
}
