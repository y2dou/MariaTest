package mariaprototype.human;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import mariaprototype.MariaPriorities;
import mariaprototype.SimpleAgent;
import mariaprototype.human.messaging.EnvelopePool;
import mariaprototype.human.messaging.MessageEnvelope;
import mariaprototype.human.messaging.NetworkAgent;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import cern.jet.random.AbstractDistribution;
import cern.jet.random.Exponential;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

public class Town extends SimpleAgent implements NetworkAgent {
	private Context<?> context;
	private GeometryFactory fac;
	
	private String name;
	private Geometry location;
	
	private List<NetworkedUrbanAgent> residents = new LinkedList<NetworkedUrbanAgent>();
	
	private int numOffers = 5;
	private double lambdaOffers = 0;
	private double offerValueLow = 1000000;
	private double offerValueHigh = 10000000;
	
	private int[] offerSchedule;
	
	private AbstractDistribution xSpread;
	private AbstractDistribution ySpread;
	
	private int tickCounter = 0;
	
	public Town(HumanContext context, String name, Geometry location, AbstractDistribution xSpread, AbstractDistribution ySpread) {
		this.context = context;
		this.name = name;
		this.location = location;
		fac = new GeometryFactory();
		
		Parameters p = RunEnvironment.getInstance().getParameters();
		numOffers = (Integer) p.getValue("numOffers");
		lambdaOffers = (Double) p.getValue("lambdaOffers");
		
		double offerValueSpread = (Double) p.getValue("offerValueSpread");
		double offerValueAverage = (Double) p.getValue("offerValueAverage");
		
		offerValueLow = offerValueAverage - offerValueSpread;
		offerValueHigh = offerValueAverage + offerValueSpread;
		double runlength = (Double) p.getValue("runlength");
		
		offerSchedule = new int[(int) Math.ceil(runlength)];
		Arrays.fill(offerSchedule, 0);
		
		this.xSpread = xSpread;
		this.ySpread = ySpread;
		
		if (lambdaOffers > 0) {
			// use exponential interarrival distribution
			Exponential distribution = RandomHelper.createExponential(lambdaOffers);
			
			double counter = distribution.nextDouble();
			
			while (counter < runlength) {
				offerSchedule[(int) Math.floor(counter)] += 1;
				double interarrivalTime = distribution.nextDouble(); 
				counter += interarrivalTime;
			}
		} else {
			for (int i = 0; i < offerSchedule.length; i++) {
				offerSchedule[i] = numOffers;
			}
		}
	}
	
	public Geometry add(NetworkedUrbanAgent a) {
		Coordinate coord = location.getCoordinate();
		// coordinates given as lat/long deviations from the mean
		// ideally this would represent the actual spatial population density of the town
		Geometry agentLoc = fac.createPoint(new Coordinate(coord.x + xSpread.nextDouble(), coord.y + ySpread.nextDouble()));
		residents.add(a);
		a.setLocation(agentLoc);
		return agentLoc;
	}
	
	public boolean remove(NetworkedUrbanAgent a) {
		return residents.remove(a);
	}
	
	public String getName() {
		return name;
	}
	
	public Geometry getLocation() {
		return location;
	}
	
	public List<NetworkedUrbanAgent> getResidents() {
		return residents;
	}
	
	@Override
	public AgentType getAgentType() {
		return AgentType.TOWN;
	}
	
	@ScheduledMethod(start = 1, interval = 1, priority = MariaPriorities.CLIMATOLOGY)
	public void broadcastJobs() {
		int currentOffers = offerSchedule[tickCounter++];
		
		HumanContext hc = ((HumanContext) context);
		EnvelopePool pool = hc.getEnvelopePool();
		
		ArrayList<HouseholdAgent> households = new ArrayList<HouseholdAgent>(hc.households);
		Collections.shuffle(households);
		for (int i = 0; i < currentOffers && i < households.size(); i++) {
			MessageEnvelope messageEnvelope = pool.getEnvelope(this, households.get(i), new JobOffer(this, offerValueLow + RandomHelper.getDistribution("offerValue").nextDouble() * (offerValueHigh - offerValueLow)));
			messageEnvelope.send();
			messageEnvelope.discard();
		}
		
	}
	
	@Override
	public void store(MessageEnvelope messageEnvelope) {
		// the Town doesn't get any mail
	}
}
