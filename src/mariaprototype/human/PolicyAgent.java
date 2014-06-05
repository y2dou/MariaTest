package mariaprototype.human;

import mariaprototype.SimpleAgent;
import mariaprototype.human.messaging.EnvelopePool;
import mariaprototype.human.messaging.MessageEnvelope;
import mariaprototype.human.messaging.NetworkAgent;
import repast.simphony.context.Context;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.collections.IndexedIterable;

public class PolicyAgent extends SimpleAgent implements NetworkAgent {
	private EnvelopePool policyEnvelopePool;
	private Policy policy;
	
	public PolicyAgent() {
		policyEnvelopePool = new EnvelopePool();
		//policy = new Policy();
	}
	
	@Override
	public AgentType getAgentType() {
		return AgentType.POLICY_AGENT;
	}
	
	// @ScheduledMethod(start = 1, interval = 1, priority = MariaPriorities.MESSAGE_PASSING_1)
	@SuppressWarnings("unchecked")
	public void enactPolicy() {
		// broadcast this year's policy to the world
		
		// set policy (read from file)
		MessageEnvelope messageEnvelope = policyEnvelopePool.getBroadcastEnvelope(this, policy);
		
		// send policy
		Context<HouseholdAgent> context = ContextUtils.getContext(this);
		IndexedIterable<HouseholdAgent> iter = context.getObjects(HouseholdAgent.class);
		
		while (iter.iterator().hasNext()) {
			HouseholdAgent h = iter.iterator().next();
			messageEnvelope.send(h);
		}
		
		messageEnvelope.discard();
	}
	
	@Override
	public void store(MessageEnvelope messageEnvelope) {}
}
