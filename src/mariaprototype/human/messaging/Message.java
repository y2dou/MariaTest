package mariaprototype.human.messaging;

public interface Message {
	public enum MessageType { POLICY, HOUSEHOLD_STATE, MARKET_PRICES, MIDDLEMAN_OFFER, JOB_OFFER }
	
	public MessageType getMessageType();
}
