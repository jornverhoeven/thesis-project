package tech.jorn.adrian.agent;

public class AdrianAgentRunner {
    public static void main(String[] args) {
        var configuration = AgentConfigurationLoader.load("agent.yml");
        var agent = new AdrianAgent(configuration, null, null, null);
    }
}










