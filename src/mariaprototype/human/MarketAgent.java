package mariaprototype.human;

import mariaprototype.SimpleAgent;
import mariaprototype.human.messaging.MessageEnvelope;
import mariaprototype.human.messaging.NetworkAgent;

public class MarketAgent extends SimpleAgent implements NetworkAgent {
	public MarketAgent() {
		
	}

	@Override
	public void store(MessageEnvelope messageEnvelope) {
	}

	@Override
	public AgentType getAgentType() {
		return AgentType.MARKET_AGENT;
	}
}
