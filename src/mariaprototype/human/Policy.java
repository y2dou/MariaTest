package mariaprototype.human;

import mariaprototype.human.messaging.Message;

/**
 * A policy is a list of available government programs, 
 * conditional on a household or individual satisfying certain requirements.
 * 
 * <p>
 * In addition, the policy can be used to set transport costs and other regional or global 
 * variables.
 * 
 * @author Raymond Cabrera
 *
 */
public abstract class Policy implements Message {
	// a set of payouts and conditions (state variables, transport costs)
	
	@Override
	public MessageType getMessageType() {
		return MessageType.POLICY;
	}
	
	public abstract boolean satisfiesRequirements(HouseholdAgent h);
}
