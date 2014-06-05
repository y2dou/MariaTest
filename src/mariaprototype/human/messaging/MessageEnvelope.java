package mariaprototype.human.messaging;


public class MessageEnvelope {
	private EnvelopePool pool;
	
	private NetworkAgent source;
	private NetworkAgent destination;
	private Message contents;
	
	public MessageEnvelope(EnvelopePool pool) {
		this.pool = pool;
	}
	
	public void send() {
		destination.store(this);
	}
	
	public void send(NetworkAgent receiver) {
		receiver.store(this);
	}
	
	public void discard() {
		source = null;
		destination = null;
		contents = null;
		pool.getAvailableEnvelopes().add(this);
	}

	public EnvelopePool getPool() {
		return pool;
	}

	public NetworkAgent getSource() {
		return source;
	}

	public void setSource(NetworkAgent source) {
		this.source = source;
	}

	public NetworkAgent getDestination() {
		return destination;
	}

	public void setDestination(NetworkAgent destination) {
		this.destination = destination;
	}

	public Message getContents() {
		return contents;
	}

	public void setContents(Message contents) {
		this.contents = contents;
	}
	
	
}
