package mariaprototype.human.messaging;

import javolution.util.FastTable;

public class EnvelopePool {
	private FastTable<MessageEnvelope> availableEnvelopes;
	
	public EnvelopePool() {
		availableEnvelopes = new FastTable<MessageEnvelope>();
	}

	public MessageEnvelope getBroadcastEnvelope(NetworkAgent source, Message contents) {
		return getEnvelope(source, null, contents);
	}
	
	public MessageEnvelope getEnvelope(NetworkAgent source, NetworkAgent destination, Message contents) {
		if (availableEnvelopes.isEmpty()) {
			availableEnvelopes.add(new MessageEnvelope(this));
		}
		
		MessageEnvelope env = availableEnvelopes.getFirst();
		
		env.setSource(source);
		env.setDestination(destination);
		env.setContents(contents);
		
		return env;
	}
	
	public FastTable<MessageEnvelope> getAvailableEnvelopes() {
		return availableEnvelopes;
	}
}
