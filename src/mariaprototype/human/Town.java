package mariaprototype.human;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javolution.util.FastMap;

import mariaprototype.MariaPriorities;
import mariaprototype.SimpleAgent;
import mariaprototype.human.messaging.EnvelopePool;
import mariaprototype.human.messaging.MessageEnvelope;
import mariaprototype.human.messaging.NetworkAgent;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.environment.RunState;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.graph.RepastEdge;
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
	Map<Integer, Integer> offerLists = new HashMap<Integer, Integer> ();
	private List<NetworkedUrbanAgent> residents = new LinkedList<NetworkedUrbanAgent>();
	
	private int numOffers = 0;
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
		offerLists.clear();
		
		if ( numOffers >= 0) {		 				
			offerLists.put( -1, numOffers);
		//	System.out.println("  Constant Job Offer: " + numOffers);
		}
		else if ( numOffers < 0 ){
			
			//XXX: take all values in the file and load them into the map
			String fileName = "auxdata/policy/offerTest.txt";	
			String line= null;
			
			try {
				FileReader fileReader = new FileReader(fileName);
				BufferedReader bufferedReader = 
	                new BufferedReader(fileReader);
                int ticks = 1;
	            while((line = bufferedReader.readLine()) != null) {
	//            	context.pensionLists.put(new Double(ticks), new Double(line.trim()));        
	            	offerLists.put(new Integer(ticks), new Integer(line.trim())); 
	            	++ticks;
	      //    	System.out.println(ticks+" "+new Double(line.trim()));
	            }   
				
	            bufferedReader.close();
			}
			catch (FileNotFoundException ex){
				System.out.println("Offer Exception");
			}
			
			catch(IOException ex) {
				System.out.println("Offer Exception 2");
			}		
				}
		System.out.println("  Job Offers: "+ offerLists.toString());
		double offerValueSpread = (Double) p.getValue("offerValueSpread");
		double offerValueAverage = (Double) p.getValue("offerValueAverage");
		
		offerValueLow = offerValueAverage - offerValueSpread;
		offerValueHigh = offerValueAverage + offerValueSpread;
		double runlength = (Double) p.getValue("runlength");
		
	//	offerSchedule = new int[(int) Math.ceil(runlength)];
		//this is a int array
		// ceil(double a)
		/*Returns the smallest (closest to negative infinity) 
		 * double value that is greater than or equal to the argument 
		 * and is equal to a mathematical integer.*/
		/*.ceil is to round up of a double 
		 * math.ceil(34.4)->35;
		 * there's another one which is round math.round(34.4)->34.
		 * */
		this.xSpread = xSpread;
		this.ySpread = ySpread;
		
	//	Arrays.fill(offerSchedule, 0);
	//	fill(int[] a, int val)
	//	Assigns the specified int value to each element of the specified array of ints.
		
		
	/*	
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
		*/
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
	//	int currentOffers = offerSchedule[tickCounter++];
		int currentOffers = 0;
		if (!offerLists.containsKey(-1)){
			double tick = RunState.getInstance().getScheduleRegistry().getModelSchedule().getTickCount();
			currentOffers = offerLists.get((int)tick).intValue();
		}
		else {
			currentOffers = numOffers;
		}	
	//	System.out.println(" current offers: "+ currentOffers);
	//	int currentOffers = 3;
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
	//	List<NetworkedUrbanAgent> recallSolutions = new LinkedList<NetworkedUrbanAgent>();
     //     recallSolutions.addAll(this.getResidents());
          int residentSize = 0;
          for (int i = 0 ; i < households.size(); i++)
           {  residentSize += households.get(i).getLinkedHouseholds().size(); 
              }
      //   		System.out.println("residents size: "+this.getResidents().size());
          int guard = residentSize - currentOffers ;
          // if existing off-farming jobs is bigger than current offers, start recalling back; 
          if ( guard > 0 ) {     	  
      //  	  System.out.println(RunState.getInstance().getScheduleRegistry().getModelSchedule().getTickCount()+ " guard size: "+guard);
        	 int houseList = households.size() -1;       	 
        	 while (households.get(houseList) != null && guard >0){
               // iterator 
        		// int size = households.get(houseList).getLinkedHouseholds().size();
        		    while (households.get(houseList).getLinkedHouseholds().keySet().iterator().hasNext()) {

        		//    	System.out.println(households.get(houseList).getLinkedHouseholds().size());
        		        	NetworkedUrbanAgent tmp = households.get(houseList).getLinkedHouseholds().keySet().iterator().next();
        		//    	System.out.println(tmp.toString());
        		    	households.get(houseList).recall(tmp, "broadcast");
        		  //  	households.get(houseList).getLinkedHouseholds().keySet().iterator().next().removeNetworkedHousehold(households.get(houseList));
        		    	guard --;
        		//         	System.out.println("guard --: "+ guard);
        		 }	
        		    houseList --; 
        		 }
        	 currentOffers = 0;
        	 }
          
          
		else {
		   for ( int j = 0 ; j < households.size() ; j++ )
				{
					if (!households.get(j).getLinkedHouseholds().isEmpty()  )
					{
					currentOffers -= households.get(j).getLinkedHouseholds().size(); 
				//	households.remove(j); //remove household who already has a salary from the current job offer. 
					 }	
				}
		}
	   // 	System.out.println(" current avaiable offers: "+ currentOffers);	
	    	
	    //	protected FastMap<NetworkedUrbanAgent, RepastEdge<SimpleAgent>> linkedHouseholds = new FastMap<NetworkedUrbanAgent, RepastEdge<SimpleAgent>>();
	    
	        	
	    		for ( int i = 0; i < households.size()&& currentOffers > 0; i++) {
	    			if( randomGenerator.nextDouble()<= households.get(i).getJobPossibility());
					{
					//	System.out.println(households.get(i).getJobPossibility());
						double wage=households.get(i).getHusbandEdu()*316.8+households.get(i).getHusbandAge()*76.1+3238.0;

		            //	MessageEnvelope messageEnvelope = pool.getEnvelope(this, households.get(households.size()-i-1), new JobOffer(this, wage/5));
						MessageEnvelope messageEnvelope = pool.getEnvelope(this, households.get(i), new JobOffer(this, wage/5));
		            	messageEnvelope.send();
						messageEnvelope.discard();
						currentOffers--;
					}
	    		}   	
/*			for (int i = 0; i < currentOffers && i < households.size(); i++) 
			{ 	
				if( randomGenerator.nextDouble()<= households.get(i).getJobPossibility());
					{
					//	System.out.println(households.get(i).getJobPossibility());
						double wage=households.get(i).getHusbandEdu()*316.8+households.get(i).getHusbandAge()*76.1+3238.0;
					//	System.out.println("wage: "+wage);
		            //	MessageEnvelope messageEnvelope = pool.getEnvelope(this, households.get(households.size()-i-1), new JobOffer(this, wage/5));
						MessageEnvelope messageEnvelope = pool.getEnvelope(this, households.get(i), new JobOffer(this, wage/5));
		            	messageEnvelope.send();
						messageEnvelope.discard();
						currentOffers--;
					}
					}*/
	//		System.out.println(" left offers: "+ currentOffers);	
				
		
}
	
	
	@Override
	public void store(MessageEnvelope messageEnvelope) {
		// the Town doesn't get any mail
	}
}
