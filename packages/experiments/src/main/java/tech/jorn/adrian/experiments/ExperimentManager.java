package tech.jorn.adrian.experiments;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.jorn.adrian.core.eventManager.events.IdentifyRisksEvent;
import tech.jorn.adrian.core.messaging.Message;
import tech.jorn.adrian.experiments.simulation.SimulationGenerator;

import java.util.concurrent.atomic.AtomicInteger;

public class ExperimentManager {
    public static void main(String[] args) {
        Logger log = LogManager.getLogger(ExperimentManager.class);
        var generator = new SimulationGenerator();
        var simulation = generator.fromFile("./simple-infra.yml");

        var messageCount = new AtomicInteger();
        var messageQueue = simulation.getMessageQueue();
        messageQueue.onMessage().subscribe(m -> {
            messageCount.addAndGet(1);
            log.info("Received message {} ({}) for node ({}). count: {}",
                    m.getData().getClass().getSimpleName(),
                    m.getId(),
                    m.getRecipient().getID(),
                    messageCount.get()
            );
        });

        for (int i = 0; i < simulation.getAgents().size(); i++) {
            var agent = simulation.getAgents().get(i);
            int finalI = i;
            agent.onStateChange().subscribe(state -> {
                log.info("Changing state for agent ({}:{}) to ({})",
                        finalI,
                        agent.getConfiguration().getParentNode().getID(),
                        state.name()
                );
            });
        }


        var agents = simulation.getAgents();
        // Share initial information
        agents.forEach(a -> {
            log.info("Requesting knowledge sharing for {}", a.getConfiguration().getParentNode().getName());
            a.shareKnowledge();
        });

//        var mermaid = new MermaidGraphGenerator<Node, Link>();
//        var knowledge = new MermaidKnowledgeBaseGenerator();
//        System.out.println();
//        System.out.print(mermaid.print(simulation.getInfrastructure()));

//        simulation.getAgents().forEach(agent -> {
//            try {
//                var file = new FileWriter(String.format("./graphs/%s.mmd", agent.getConfiguration().getParentNode().getID()));
//                file.write("%% " + agent.getConfiguration().getParentNode().getID() + "\n");
//                file.write(knowledge.print(agent.getKnowledgeBase()));
//                file.close();
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        });

        messageQueue.push(new Message<>(
                new IdentifyRisksEvent(),
                null,
                agents.get(3).getConfiguration().getParentNode())
        );
//        var riskReport = simulation.getAgents().get(3).detectRisks();
//        var mmdAttack = new MermaidAttackGraphGenerator();
//        System.out.println("---\ntitle: Attack graph for node D\n---");
//        System.out.println(mmdAttack.print(attackGraph));

        log.info("Test");
    }
}
