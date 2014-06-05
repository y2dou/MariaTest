package mariaprototype.human;

import mariaprototype.human.messaging.Message;

public class HouseholdAgentState implements Message {
	private double capital;
	private double labour;
	private double subsistenceRequirements;
	
	public void update(HouseholdAgent agent) {
		capital = agent.getCapital();
		labour = agent.getLabour();
		subsistenceRequirements = agent.getSubsistenceRequirements();
	}
	
	public double getCapital() {
		return capital;
	}
	
	public double getLabour() {
		return labour;
	}
	
	public double getSubsistenceRequirements() {
		return subsistenceRequirements;
	}
	
	@Override
	public MessageType getMessageType() {
		return MessageType.HOUSEHOLD_STATE;
	}
}
