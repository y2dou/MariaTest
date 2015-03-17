package mariaprototype.human;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
	
	private int numOffers = 10;
	//reads from batch file/parameter file, Yue Nov 10, 2014
	private double lambdaOffers = 0;
	private double offerValueLow = 1000000;
	private double offerValueHigh = 20000000;
	
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
		//		System.out.println("town L86,num of offers="+offerSchedule[i]);
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
		
	//	for(HouseholdAgent myAgent : households){
		//}
		Collections.sort(households,new Comparator<HouseholdAgent>(){

			@Override
			public int compare(HouseholdAgent arg0, HouseholdAgent arg1) {
		
				// TODO Auto-generated method stub
				Double obj1=(double) arg0.getJobPossibility();
				Double obj2=(double) arg1.getJobPossibility();

			
				int retval=obj1.compareTo(obj2);
				return retval;
				
			}
			//sort households based on job probability of a household. 
		});
		
		//ArrayList<HouseholdAgent> eligibleHouseholds = new ArrayList<HouseholdAgent>();
		//Collections.shuffle(households);
		for (int i = 0; i < currentOffers && i < households.size(); i++) {
		//	MessageEnvelope messageEnvelope = pool.getEnvelope(this, households.get(households.size()-i-1), new JobOffer(this, offerValueLow + RandomHelper.getDistribution("offerValue").nextDouble() * (offerValueHigh - offerValueLow)));
			//is this the message including value of wage? Yue, Oct 29, 2014
			int j=households.size()-i-1;
			double wage=households.get(j).getHusbandEdu()*316.8+households.get(j).getHusbandAge()*76.1+3238.0;
		//    double wage=10;
			//	System.out.println("hhdID="+households.get(j).getID()+" husAge="+households.get(j).getHusbandAge()
			//		+" husEdu="+households.get(j).getHusbandEdu()+" wage="+wage/100);
			//acai price in simulation is 0.0008; so the wage should be divided by 1000;
			MessageEnvelope messageEnvelope = pool.getEnvelope(this, households.get(households.size()-i-1), new JobOffer(this, wage/50));			
			//System.out.println(this.getID());
			 
		//	MessageEnvelope messageEnvelope = pool.getEnvelope(this, households.get(i), new JobOffer(this, offerValueLow + RandomHelper.getDistribution("offerValue").nextDouble() * (offerValueHigh - offerValueLow)));
			messageEnvelope.send();
			messageEnvelope.discard();
		}
		
	}
	
	@Override
	public void store(MessageEnvelope messageEnvelope) {
		// the Town doesn't get any mail
	}
}
