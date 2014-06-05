package mariaprototype.human.messaging;

public class MiddlemanOffer implements Message {
	private double commission;
	
	public MiddlemanOffer() {
		
	}
	
	public double getCommission() {
		return commission;
	}
	
	public void setCommission(double commission) {
		this.commission = commission;
	}
	
	@Override
	public MessageType getMessageType() {
		return MessageType.MIDDLEMAN_OFFER;
	}

}
