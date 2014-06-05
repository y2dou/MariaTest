package mariaprototype.human;

import mariaprototype.human.messaging.Message;

public class JobOffer implements Message {
	private double wage;
	private Town town;

	@Override
	public MessageType getMessageType() {
		return MessageType.JOB_OFFER;
	}
	
	public JobOffer(Town town, double wage) {
		this.town = town;
		this.wage = wage;
	}
	
	public Town getTown() {
		return town;
	}
	
	public double getWage() {
		return wage;
	}
}
