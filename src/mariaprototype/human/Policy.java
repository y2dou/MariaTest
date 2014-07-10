package mariaprototype.human;

import com.sun.xml.internal.bind.CycleRecoverable.Context;

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
 * The cash transfer Program is setted here, as a global variable; 
 * to decide whether or not farm agents can get the additional cash;
 */
public abstract class Policy implements Message {
	// a set of payouts and conditions (state variables, transport costs)
	public  static boolean cashTransferProgram=true;
	public static double cashTransferVolume=0.0;
	
	
	@Override
	public MessageType getMessageType() {
		return MessageType.POLICY;
	}
	
	
	/*public void setCashTransferVolume(){
		if (cashTransferProgram)
		
			cashTransferVolume=100;
		else 
			cashTransferVolume=0;
	}*/
	public abstract boolean satisfiesRequirements(HouseholdAgent h);
}
