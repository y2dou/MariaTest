package mariaprototype.human;

import mariaprototype.MariaPriorities;
import mariaprototype.SimpleAgent;
import mariaprototype.human.messaging.MessageEnvelope;
import mariaprototype.human.messaging.NetworkAgent;
import repast.simphony.engine.schedule.ScheduledMethod;

public class MiddlemanAgent extends SimpleAgent implements NetworkAgent {

	@Override
	public AgentType getAgentType() {
		return AgentType.MIDDLEMAN_AGENT;
	}
	
	@ScheduledMethod(start = 1, interval = 1, priority = MariaPriorities.MESSAGE_PASSING_1)
	public void broadcastOffer() {
		// broadcast this year's offer to the world
	}
	
	@Override
	public void store(MessageEnvelope messageEnvelope) {
		// store messages from potential clients
	}
}
