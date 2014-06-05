package mariaprototype.human;

import java.util.Map.Entry;

import javolution.util.FastMap;
import mariaprototype.human.messaging.Message;
import mariaprototype.human.messaging.MessageEnvelope;
import mariaprototype.human.messaging.NetworkAgent;

/**
 * A <code>HouseholdAgent</code> based on heuristics provided by Miguel and Eduardo.
 * 
 * @author Raymond Cabrera
 *
 */
public class NetworkedHeuristicHouseholdAgent extends HeuristicHouseholdAgent {
	private FastMap<NetworkAgent, Message> mailbox = new FastMap<NetworkAgent, Message>();
	private FastMap<HouseholdAgent, HouseholdAgentState> networkStates = new FastMap<HouseholdAgent, HouseholdAgentState>();
	
	//private double costPerLabourUnit;
	
	public NetworkedHeuristicHouseholdAgent() {
		super();
	}
	
	public NetworkedHeuristicHouseholdAgent(int id) {
		super(id);
	}
	
	@Override
	public void init(HumanContext humanContext, int... coordinates) {
		super.init(humanContext, coordinates);
		
		// set default labour unit price
		//costPerLabourUnit = capital / labour;
	}
	
	@Override
	public void messagePassing1() {
		broadcastState();
	}
	
	@Override
	public void plan() {
		super.plan();
		
		// trade capital
		
		// calculate average capital: trade to move closer to labour-weighted average
		
		// punish/reward reciprocity?
	}
	
	@Override
	protected void processHouseholdMessage(Entry<NetworkAgent, Message> e) {
		// household message contains the household's state
		networkStates.put((HouseholdAgent) e.getKey(), (HouseholdAgentState) e.getValue());
	}
	
	@Override
	public void messagePassing2() {
		broadcastState();
	}
	
	@Override
	public void intermediate() {
		processMessages();
		
		// adjust reciprocity based on deviation from labour-weighted average
	}
	
	@Override
	public void messagePassing3() {
		broadcastState();
	}
	
	@Override
	public void retrospect() {
		processMessages();	// result of messages passed during stage 3!
		
		// adjust reciprocity based on deviation from labour-weighted average
		
	}

	@Override
	public void store(MessageEnvelope messageEnvelope) {
		mailbox.put(messageEnvelope.getSource(), messageEnvelope.getContents());
	}
	
}
