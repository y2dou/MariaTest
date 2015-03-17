package mariaprototype.human;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;

import javolution.util.FastTable;
import mariaprototype.ImageUtility;
import mariaprototype.NetworkUtility;
import mariaprototype.Range;
import mariaprototype.SimpleAgent;
import mariaprototype.WeightedSelector;
import mariaprototype.database.Database;
import mariaprototype.environmental.EnvironmentalContext;
import mariaprototype.human.messaging.EnvelopePool;
import mariaprototype.utility.GeographyUtility;
import repast.simphony.context.DefaultContext;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.environment.RunState;
import repast.simphony.space.graph.Network;

public class HumanContext extends DefaultContext<SimpleAgent> {
	protected int width;
	protected int height;
	protected double originx;
	protected double originy;
	protected double cellsize;
	
	protected WeightedSelector<Range<Integer>> plotSizeSelector;
	protected WeightedSelector<Integer> familySizeSelector;
	protected WeightedSelector<DemographicWeightedSelector> demographicSelector;
	protected WeightedSelector<Range<Integer>> educationSelector;
	protected WeightedSelector<Range<Integer>> capitalSizeSelector; 
	//Feb 04,2015, Yue,to change the initialized capital
	
	// private FastTable<HouseholdReport> householdReports = new FastTable<HouseholdReport>();
	protected FastTable<HouseholdAgent> households;
	
	protected GeographyUtility<SimpleAgent> geoUtility;
	protected EnvironmentalContext envContext;
	
	protected boolean outputImages;
	protected boolean outputNetworkCSV;
	
	
	protected double percentHeuristicHouseholds;
	protected double percentOptimalHouseholds;
	protected double percentForwardOptimalHouseholds;
	protected double percentFullForwardOptimalHouseholds;
	protected double percentChayanovHouseholds;
	protected double percentMinLabourHouseholds;
	protected double percentSubsistenceHouseholds;
	//added by Yue,Dec 5, 2014
	
	protected float pension;
	protected float bf;
	protected float alpha;
//	protected float beta;
	protected Connection conn;
	
	protected EnvelopePool envelopePool;	
	

	
	public HumanContext() {
		super();
	}
	
	public HumanContext(Object name, Object typeID) {
		super(name, typeID);
	}

	public HumanContext(Object name) {
		super(name);
	}

	public void report() {
		// use built-in RepastS methods if possible
		
		/*
		// report out household information
		double tick = RunState.getInstance().getScheduleRegistry().getModelSchedule().getTickCount();
		for (HouseholdReport h : householdReports) {
			h.append(tick);
		}
		*/
		
		for (HouseholdAgent h : households) {
			Database.getInstance().logHouseholdState(conn, h, "report");
			
			for (NetworkedUrbanAgent a : h.getLinkedHouseholds().keySet()) {
				Database.getInstance().logUrbanAgentState(conn, a, "report");
			}
		}
		
		if ((!(Boolean) RunState.getInstance().getFromRegistry("invalidRun")) && outputImages) {
			// generate PNG grid
			String tick = String.format("%02d", (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount());
			File landUseFile = new File((String) RunState.getInstance().getFromRegistry("path") + "/landUse" + tick + ".png");
			
			ImageUtility.createLandUsePNG(households, width, height, landUseFile);
		}
	}
	
	
	@SuppressWarnings("unchecked")
	public void finalReport() {
		/*
		for (HouseholdReport h : householdReports) {
			try {
				h.generateCSV(new FileOutputStream(path + "/household" + h.getHousehold().getID() + ".csv", false));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		*/
		
		Network<SimpleAgent> householdNetwork = (Network<SimpleAgent>) getProjection("Multisited Household Network");
		if ((!(Boolean) RunState.getInstance().getFromRegistry("invalidRun")) && outputNetworkCSV) {
			try {
				NetworkUtility.outputNetworkToCSV(householdNetwork, 
						new FileOutputStream((String) RunState.getInstance().getFromRegistry("path") + "/network.csv", false));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void cleanup() {
		if (RunEnvironment.getInstance().isBatch()) {
			households.clear();
			try {
				this.clear();
			} catch (Exception e) {}
		}
	}

	public EnvironmentalContext getEnvironmentalContext() {
		return envContext;
	}
	
	public EnvelopePool getEnvelopePool() {
		return envelopePool;
	}
}
