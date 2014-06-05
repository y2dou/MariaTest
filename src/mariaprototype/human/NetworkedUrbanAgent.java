package mariaprototype.human;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

import javolution.util.FastMap;
import mariaprototype.MariaPriorities;
import mariaprototype.SimpleAgent;
import mariaprototype.human.messaging.Message;
import mariaprototype.human.messaging.MessageEnvelope;
import mariaprototype.human.messaging.NetworkAgent;
import repast.simphony.engine.schedule.ScheduledMethod;

import com.vividsolutions.jts.geom.Geometry;

public class NetworkedUrbanAgent extends SimpleAgent implements NetworkAgent {
	private FastMap<NetworkAgent, Message> mailbox = new FastMap<NetworkAgent, Message>();
	private List<HouseholdAgent> networkedHouseholds;
	
	private Geometry location;
	
	private Town employer;
	private Person person;
	private double capital = 0;
	
	private double wage; // annual
	
	private Color color;
	
	public NetworkedUrbanAgent(Person person) {
		this.person = person;
		networkedHouseholds = new LinkedList<HouseholdAgent>();
	}
	
	public void addNetworkedHousehold(HouseholdAgent h) {
		networkedHouseholds.add(h);
	}
	
	public boolean removeNetworkedHousehold(HouseholdAgent h) {
		return networkedHouseholds.remove(h);
	}
	
	public Town getEmployer() {
		return employer;
	}

	public void setEmployer(Town employer) {
		this.employer = employer;
	}
	
	public double getCapital() {
		return capital;
	}

	public Geometry getLocation() {
		return location;
	}
	
	public void setLocation(Geometry location) {
		this.location = location;
	}
	
	public double getWage() {
		return wage;
	}
	
	public void setWage(double wage) {
		this.wage = wage;
	}
	
	public Person getPerson() {
		return person;
	}

	@ScheduledMethod(start = 1, interval = 1, priority = MariaPriorities.ACTION)
	public void earn() {
		if (person != null)
			capital += wage;
	}
	
	@ScheduledMethod(start = 1, interval = 1, priority = MariaPriorities.HARVEST + 1)
	public void shareWealthPreHarvest() {
		double sharedCapital = capital / 2; // for now, networked households remit 100% of their capital
		double sharedPerHousehold = sharedCapital / (double) networkedHouseholds.size();
		
		// get connected households
		for (HouseholdAgent h : networkedHouseholds) {
			h.setCapital(h.getCapital() + sharedPerHousehold);
		}
		
		capital -= sharedCapital;
	}
	
	@ScheduledMethod(start = 1, interval = 1, priority = MariaPriorities.HARVEST - 1)
	public void shareWealthPostHarvest() {
		double sharedCapital = capital; // for now, networked households remit 100% of their capital
		double sharedPerHousehold = sharedCapital / (double) networkedHouseholds.size();
		
		// get connected households
		for (HouseholdAgent h : networkedHouseholds) {
			h.setCapital(h.getCapital() + sharedPerHousehold);
		}
		
		capital -= sharedCapital;
	}
	
	public Person removePerson() {
		Person p = person;
		person = null;
		return p;
	}
	
	public Color getColor() {
		return color;
	}
	
	public void setColor(Color color) {
		this.color = color;
	}
	
	@Override
	public AgentType getAgentType() {
		return AgentType.URBAN_AGENT;
	}
	
	@Override
	public void store(MessageEnvelope messageEnvelope) {
		mailbox.put(messageEnvelope.getSource(), messageEnvelope.getContents());
	}
}
