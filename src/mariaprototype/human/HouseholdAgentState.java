package mariaprototype.human;

import javolution.util.FastTable;
import mariaprototype.human.messaging.Message;

public class HouseholdAgentState implements Message {
	private double cashTran;
	private double capital;
	private double labour;
	private double subsistenceRequirements;
	//private FastTable<Person> familyMembers = new FastTable<Person>();
	
	public void update(HouseholdAgent agent) {
		capital = agent.getCapital();
		labour = agent.getLabour();
		cashTran=agent.getPension()+agent.getBf();
		subsistenceRequirements = agent.getSubsistenceRequirements();
		
	}
	
	public double getCashTran(){
		return cashTran;
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
