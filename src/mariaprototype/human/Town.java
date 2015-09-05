package mariaprototype.human;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

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
		//this is a int array
		// ceil(double a)
		/*Returns the smallest (closest to negative infinity) 
		 * double value that is greater than or equal to the argument 
		 * and is equal to a mathematical integer.*/
		/*.ceil is to round up of a double 
		 * math.ceil(34.4)->35;
		 * there's another one which is round math.round(34.4)->34.
		 * */
		
		Arrays.fill(offerSchedule, 0);
	//	fill(int[] a, int val)
	//	Assigns the specified int value to each element of the specified array of ints.
		
		this.xSpread = xSpread;
		this.ySpread = ySpread;
		
		if (lambdaOffers > 0) {
			// use exponential interarrival distribution
			Exponential distribution = RandomHelper.createExponential(lambdaOffers);
			
			double counter = distribution.nextDouble();
			System.out.println("counter"+counter);
			while (counter < runlength) {
				offerSchedule[(int) Math.floor(counter)] += 1;
				double interarrivalTime = distribution.nextDouble(); 
				counter += interarrivalTime;
		//		System.out.println("counter in Loop "+counter);
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
	
/*	@ScheduledMethod(start = 1, interval = 1, priority = MariaPriorities.CLIMATOLOGY)
	public void broadcastJobs() {
		int currentOffers = offerSchedule[tickCounter++];
		
		HumanContext hc = ((HumanContext) context);
		EnvelopePool pool = hc.getEnvelopePool();
		
		ArrayList<HouseholdAgent> households = new ArrayList<HouseholdAgent>(hc.households);

		//remove households who already have a job;
		for ( int j = 0 ; j < households.size() ; j++ ){
			//check how many linkedHHD are there already, to reduce offer number
			if (!households.get(j).getLinkedHouseholds().isEmpty()){
			currentOffers --; 
			households.remove(j); //remove household who already has a salary from the current job offer. 
			}	
		}
		
	//	for(HouseholdAgent myAgent : households){
		//}
		Collections.sort(households, new Comparator<HouseholdAgent>(){

			@Override
			public int compare(HouseholdAgent arg0, HouseholdAgent arg1) {
		
				// TODO Auto-generated method stub
				Double obj1=(double) arg0.getJobPossibility();
				Double obj2=(double) arg1.getJobPossibility();

			
				int retval=obj1.compareTo(obj2);
				return retval;
				
			}
			//sort households based on job probabil4ity of a household. 
		});
		
		
		for (int i = 0; i < currentOffers && i < households.size(); i++) {
						//is this the message including value of wage? Yue, Oct 29, 2014
			Random r= new Random();
			int j=households.size()-i-1;
			double wage=households.get(j).getHusbandEdu()*316.8+households.get(j).getHusbandAge()*76.1+3238.0;
		
			MessageEnvelope messageEnvelope = pool.getEnvelope(this, households.get(households.size()-i-1), new JobOffer(this, wage/50));			
			messageEnvelope.send();
			messageEnvelope.discard();
		}
		
	} */
	
	@ScheduledMethod(start = 1, interval = 1, priority = MariaPriorities.CLIMATOLOGY)	
	public void broadcastJobs() {
	//	int currentOffers = offerSchedule[tickCounter++];
		int currentOffers = 3;
		/*the change of giving out jobs:
		 * as long as hhd edu is bigger than a certain number, 
		 * they can get a job;
		 * by doing this, the overall job offer is associated with the average education level, 
		 * rather than a fixed number */
		//change made by Yue, August 13, 2015
		// then I'm losing the agent interactions--competing for jobs.
		Random randomGenerator = new Random();
		//this random generator is to compare with the job probability;
		HumanContext hc = ((HumanContext) context);
		EnvelopePool pool = hc.getEnvelopePool();
		ArrayList<HouseholdAgent> households = new ArrayList<HouseholdAgent>(hc.households);
		Collections.sort(households, new Comparator<HouseholdAgent>(){

			@Override
			public int compare(HouseholdAgent arg0, HouseholdAgent arg1) {
		
				// TODO Auto-generated method stub
				Double obj1=(double) arg0.getJobPossibility();
				Double obj2=(double) arg1.getJobPossibility();

			
				int retval=obj1.compareTo(obj2);
				return retval;
				
			}
			//sort households based on job probabil4ity of a household. 
		});
		
		//check how many linkedHHD are there already, to reduce offer number
				for ( int j = 0 ; j < households.size() ; j++ )
				{
					if (!households.get(j).getLinkedHouseholds().isEmpty()  )
					{
					currentOffers --; 
					households.remove(j); //remove household who already has a salary from the current job offer. 
					 }	
				}
				
			for (int i = 0; i < currentOffers && i < households.size(); i++) 
			{ //	int jobPossibility = (int) (households.get(i).getJobPossibility()*100.0);
			  //  System.out.println("job Possibility = "+jobPossibility);
				if(randomGenerator.nextDouble()<=households.get(i).getJobPossibility());
					{
					//	System.out.println(households.get(i).getJobPossibility());
						double wage=households.get(i).getHusbandEdu()*316.8+households.get(i).getHusbandAge()*76.1+3238.0;
		            	MessageEnvelope messageEnvelope = pool.getEnvelope(this, households.get(households.size()-i-1), new JobOffer(this, wage/50));
		            	messageEnvelope.send();
						messageEnvelope.discard();
					}}
					
				
		
}
	
	
	@Override
	public void store(MessageEnvelope messageEnvelope) {
		// the Town doesn't get any mail
	}
}
