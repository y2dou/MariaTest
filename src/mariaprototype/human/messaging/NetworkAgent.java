package mariaprototype.human.messaging;


public interface NetworkAgent {
	public enum AgentType { POLICY_AGENT, HOUSEHOLD_AGENT, MIDDLEMAN_AGENT, MARKET_AGENT, URBAN_AGENT, TOWN }
	
	public AgentType getAgentType();
	public void store(MessageEnvelope messageEnvelope);
}
