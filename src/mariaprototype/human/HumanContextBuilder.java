package mariaprototype.human;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javolution.util.FastTable;
import mariaprototype.MariaPriorities;
import mariaprototype.Point;
import mariaprototype.Range;
import mariaprototype.SimpleAgent;
import mariaprototype.WeightedSelector;
import mariaprototype.database.Database;
import mariaprototype.environmental.EnvironmentalContext;
import mariaprototype.environmental.LandUse;
import mariaprototype.human.messaging.EnvelopePool;
import mariaprototype.utility.GeographyUtility;
import mariaprototype.human.Policy;

import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;

import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.gis.GeographyFactoryFinder;
import repast.simphony.context.space.graph.NetworkFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.environment.RunState;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.gis.GeographyParameters;
import repast.simphony.space.graph.Network;
import repast.simphony.valueLayer.GridValueLayer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

public class HumanContextBuilder implements ContextBuilder<SimpleAgent> {
	
	public HumanContextBuilder() {
		
	}
	
	public Context<SimpleAgent> build(Context<SimpleAgent> c) {
		System.out.println("Start running model Maria...");
		HumanContext context;
		if (c == null) {
			context = new HumanContext("HumanContext");
		} else {
			try {
				context = (HumanContext) c;
			} catch (ClassCastException e) {
				context = new HumanContext("HumanContext");
			}
		}
		//context = new DefaultContext<SimpleAgent>();
		
		// load parameters
		Parameters p = RunEnvironment.getInstance().getParameters();
		context.households = new FastTable<HouseholdAgent>();
		context.envContext = (EnvironmentalContext) RunState.getInstance().getMasterContext().getSubContext("EnvironmentalContext");
		context.width = (Integer) p.getValue("width");
		context.height = (Integer) p.getValue("height");
		context.originx = (Double) p.getValue("originx");
		context.originy = (Double) p.getValue("originy");
		context.cellsize = (Double) p.getValue("cellsize");
		context.outputImages = (Boolean) p.getValue("outputEnabled") && (Boolean) p.getValue("outputImagesAsFiles");
		context.outputNetworkCSV = (Boolean) p.getValue("outputEnabled") && (Boolean) p.getValue("outputNetworkCSV");
		context.percentHeuristicHouseholds = (Double) p.getValue("percentHeuristicHouseholds");
		context.percentOptimalHouseholds = (Double) p.getValue("percentOptimalHouseholds");
		context.percentForwardOptimalHouseholds = (Double) p.getValue("percentForwardOptimalHouseholds");
		context.percentFullForwardOptimalHouseholds = (Double) p.getValue("percentFullForwardOptimalHouseholds");
		context.percentChayanovHouseholds = (Double) p.getValue("percentChayanovHouseholds");
		context.percentMinLabourHouseholds = (Double) p.getValue("percentMinLabourHouseholds");
		context.percentSubsistenceHouseholds = (Double) p.getValue("percentSubsistenceHouseholds");
		
		
		context.conn = (Connection) RunState.getInstance().getFromRegistry("connection");
		
		int demographicRandomSeed = (Integer) p.getValue("demographicRandomSeed");
		int familySizeRandomSeed = (Integer) p.getValue("familySizeRandomSeed");
	//	int plotSizeRandomSeed = (Integer) p.getValue("plotSizeRandomSeed");
		int capitalSizeRandomSeed= (Integer) p.getValue("capitalSizeRandomSeed");

		
	//	double capitalSizeRandomSeed = (Double) p.getValue("capitalSizeRandomSeed");
		int educationRandomSeed = (Integer) p.getValue("educationRandomSeed");
		//15 is the highest education year that households have; 
		
		int numHouseholds = (Integer) p.getValue("numHouseholds");
		int numPersons = (Integer) p.getValue("numPersons");
		
		double globalMultiplier = (Double) p.getValue("priceStreamMultiplier");
		
		//load pension
	//	System.out.println(p.getValue("cashTransfer"));
		double pension = (Double) p.getValue("pension");
		context.pension=(float) pension;
		double bf = (Double) p.getValue("bf");
		context.bf=(float)bf;
		double alpha=(Double) p.getValue("alpha");
	//	double beta=(Double) p.getValue("beta");
		
		context.alpha=(float) alpha;
	//	context.beta=(float) beta;
	//	System.out.println("alpha="+alpha+" beta="+(1-alpha));
		System.out.println("pension="+pension);
		System.out.println("bf="+bf);
		if (pension>0) 
		    { 
			    Policy.pensionProgram=true;
			    Policy.pensionVolume=pension;
			    }
		else {
			    Policy.pensionProgram=false;
			    Policy.pensionVolume=0;
		}
		if (bf>0)
		{
			Policy.bfProgram=true;
			Policy.bfVolume=bf;
		}
		else {
			Policy.bfProgram=false;
		    Policy.bfVolume=0;
		}
		setUpRandomDistributions();
		
		// load data: prices
		Map<LandUse, InputStream> priceLists = new HashMap<LandUse, InputStream>();
		Map<LandUse, Double> priceMultipliers = new HashMap<LandUse, Double>();
		StaticMarketAgent staticMarketAgent = new StaticMarketAgent();
		
		
		try {
			double staticAcaiPrice = (Double) p.getValue("acaiPrice");
			
			
			if (staticAcaiPrice >= 0) {
				staticMarketAgent.setPrice(LandUse.ACAI, staticAcaiPrice);
				System.out.println ("acaiPrice =" + staticAcaiPrice);
			} else {
				priceLists.put(LandUse.ACAI, new FileInputStream("auxdata/prices/acai.prices.txt"));
				
				double multiplier = (Double) p.getValue("acaiMultiplier");
				if (multiplier <= 0)
					multiplier = globalMultiplier;
				
				priceMultipliers.put(LandUse.ACAI, multiplier);
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
		try {
			double staticPrice = (Double) p.getValue("maniocPrice");
			if (staticPrice >= 0) {
				staticMarketAgent.setPrice(LandUse.MANIOCGARDEN, staticPrice);
			} else {
				priceLists.put(LandUse.MANIOCGARDEN, new FileInputStream("auxdata/prices/maniocgarden.prices.txt"));
				
				double multiplier = (Double) p.getValue("maniocMultiplier");
				if (multiplier <= 0)
					multiplier = globalMultiplier;
				
				priceMultipliers.put(LandUse.MANIOCGARDEN, multiplier);
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
		try {
			double staticPrice = (Double) p.getValue("timberPrice");
			if (staticPrice >= 0) {
				staticMarketAgent.setPrice(LandUse.FOREST, staticPrice);
			} else {
				priceLists.put(LandUse.FOREST, new FileInputStream("auxdata/prices/timber.prices.txt"));
				
				double multiplier = (Double) p.getValue("timberMultiplier");
				if (multiplier <= 0)
					multiplier = globalMultiplier;
				
				priceMultipliers.put(LandUse.FOREST, multiplier);
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
		// add market agent
		context.add(staticMarketAgent);
		
		context.add(new DynamicMarketAgent(priceLists, priceMultipliers));
		
		// load distributions
	/*	RandomHelper.registerGenerator("plotSizeSelector", plotSizeRandomSeed);
		context.plotSizeSelector = new WeightedSelector<Range<Integer>>("plotSizeSelector"); // plot size distribution, in cells
		context.plotSizeSelector.add(new Range<Integer>(0, 200), 76);
		context.plotSizeSelector.add(new Range<Integer>(201, 400), 26);
		context.plotSizeSelector.add(new Range<Integer>(401, 600), 9);
		context.plotSizeSelector.add(new Range<Integer>(601, 800), 4);
		context.plotSizeSelector.add(new Range<Integer>(801, 1000), 1);
		context.plotSizeSelector.add(new Range<Integer>(1001, 1200), 2); */
		//Yue Sept3, try to solve forest distribution
		
	//	RandomHelper.registerGenerator("capitalSizeSelector", RandomHelper.getGenerator("capitalWeightedSelector").nextInt());
		RandomHelper.registerGenerator("capitalSizeSelector", capitalSizeRandomSeed);
		context.capitalSizeSelector= new WeightedSelector<Range<Integer>>("capitalSizeSelector");
		context.capitalSizeSelector.add(new Range<Integer> (1000,2500), 0.50);
		context.capitalSizeSelector.add(new Range<Integer> (2501,5000), 0.35);
		context.capitalSizeSelector.add(new Range<Integer> (5001,7500), 0.15);
	//	context.capitalSizeSelector.add(new Range<Integer> (10000,12500), 10);
	//	context.capitalSizeSelector.add(capitalSizeSelector, capitalSizeSelector.getCumulativeProbability());
		
		RandomHelper.registerGenerator("educationSelector", educationRandomSeed);
		context.educationSelector = new WeightedSelector<Range<Integer>>("educationSelector");
		context.educationSelector.add(new Range<Integer>(0,3), 50);
		context.educationSelector.add(new Range<Integer>(4,5), 35);
		context.educationSelector.add(new Range<Integer>(6,9), 15);
		//context.educationSelector.add(new Range<Integer>(9,12), 10);
		
		
		RandomHelper.registerGenerator("familySizeSelector", familySizeRandomSeed);
		context.familySizeSelector = new WeightedSelector<Integer>("familySizeSelector");
		// context.familySizeSelector.add(0, 11); // no one in the family?
	/*	context.familySizeSelector.add(1, 39);
		context.familySizeSelector.add(2, 46);
		context.familySizeSelector.add(3, 64);
		context.familySizeSelector.add(4, 65);
		context.familySizeSelector.add(5, 30);
		context.familySizeSelector.add(6, 29);
		context.familySizeSelector.add(7, 18);
		context.familySizeSelector.add(8, 8);
		context.familySizeSelector.add(9, 4);
		context.familySizeSelector.add(10, 8);
		context.familySizeSelector.add(11, 5);
		context.familySizeSelector.add(12, 2);
		context.familySizeSelector.add(17, 1);
	*/
		context.familySizeSelector.add(1, 5);
		context.familySizeSelector.add(2, 10);
		context.familySizeSelector.add(3, 15);
		context.familySizeSelector.add(4, 20);
		context.familySizeSelector.add(5, 15);
		context.familySizeSelector.add(6, 8);
		context.familySizeSelector.add(7, 5);
		context.familySizeSelector.add(8, 6);
		context.familySizeSelector.add(9, 4);
		context.familySizeSelector.add(10, 4);
		context.familySizeSelector.add(12, 11);
		
		// I changed the family size selector, to small families, in order to run comparison.
		//the weight is based on hist of a1$hhdsize
		//Yue Dou, Sep 29, 2014
		
		RandomHelper.registerGenerator("demographicWeightedSelector", demographicRandomSeed);
		RandomHelper.registerGenerator("maleDemographicWeightedSelector", RandomHelper.getGenerator("demographicWeightedSelector").nextInt());
		RandomHelper.registerGenerator("femaleDemographicWeightedSelector", RandomHelper.getGenerator("demographicWeightedSelector").nextInt());
	
		context.demographicSelector = new WeightedSelector<DemographicWeightedSelector>("demographicWeightedSelector");
		DemographicWeightedSelector maleSelector = new DemographicWeightedSelector("maleDemographicWeightedSelector", false); // based on demographic pyramid of Paricatuba, 1990
		maleSelector.add(new Range<Integer>(0, 4), 9.72222);
		maleSelector.add(new Range<Integer>(5, 9), 8.33333);
		maleSelector.add(new Range<Integer>(10, 14), 8.33333);
		maleSelector.add(new Range<Integer>(15, 19), 5.55556);
		maleSelector.add(new Range<Integer>(20, 24), 4.16667);
		maleSelector.add(new Range<Integer>(25, 29), 5.55556);
		maleSelector.add(new Range<Integer>(35, 39), 1.38889);
		maleSelector.add(new Range<Integer>(40, 44), 4.86111);
		maleSelector.add(new Range<Integer>(50, 54), 0.69444);
		maleSelector.add(new Range<Integer>(55, 59), 0.69444);
		maleSelector.add(new Range<Integer>(60, 64), 1.38889);
	//	maleSelector.add(new Range<Integer>(80, 85), 1.38889);
		context.demographicSelector.add(maleSelector, maleSelector.getCumulativeProbability());
		
		
		
		DemographicWeightedSelector femaleSelector = new DemographicWeightedSelector("femaleDemographicWeightedSelector", true);
		femaleSelector.add(new Range<Integer>(0, 4), 10.41667);
		femaleSelector.add(new Range<Integer>(5, 9), 10.41667);
		femaleSelector.add(new Range<Integer>(10, 14), 8.33333);
		femaleSelector.add(new Range<Integer>(15, 19), 0.694444);
		femaleSelector.add(new Range<Integer>(20, 24), 2.083333);
		femaleSelector.add(new Range<Integer>(25, 29), 3.472222);
		femaleSelector.add(new Range<Integer>(30, 34), 4.166667);
		femaleSelector.add(new Range<Integer>(35, 39), 2.083333);
		femaleSelector.add(new Range<Integer>(40, 44), 0.694444);
		femaleSelector.add(new Range<Integer>(60, 64), 0.694444);
	//	femaleSelector.add(new Range<Integer>(75, 79), 2.083333);
	//	femaleSelector.add(new Range<Integer>(80, 85), 0.694444);
		context.demographicSelector.add(femaleSelector, femaleSelector.getCumulativeProbability());
		//where is this score from? and what for? Yue Dou May 14, 2014
		//now i can answer this question, they're the distribution of the data, Oct 1, 2014
		
		
		
		GridValueLayer landHolderField = new GridValueLayer("Land Holder Field", true,
				new repast.simphony.space.grid.StrictBorders(), context.width, context.height);
	//	System.out.println(context.width);
	//	System.out.println("height"+context.height);
		context.addValueLayer(landHolderField);
		
		// add networks
		Network<SimpleAgent> multisitedNetwork = NetworkFactoryFinder.createNetworkFactory(null).createNetwork(
				"Multisited Household Network", context, true);
		context.addProjection(multisitedNetwork);
		
		ContinuousSpace<SimpleAgent> personSpace = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null)
				.createContinuousSpace("Person Space", context,
						new RandomCartesianAdder<SimpleAgent>(),
						new repast.simphony.space.continuous.StrictBorders(),
						context.width, context.height);
		context.addProjection(personSpace);
		
		GeographyParameters<SimpleAgent> geoParams = new GeographyParameters<SimpleAgent>();
		
		Geography<SimpleAgent> personGeography = GeographyFactoryFinder.createGeographyFactory(null)
	            .createGeography("Person Geography", context, geoParams);
		
		
		context.envelopePool = new EnvelopePool();
		
		File projectionFile = new File("gisdata/paricatuba/Paricatuba.prj");
		
		
		// add town
		GeometryFactory fac = new GeometryFactory();
		context.add(new Town(context, "Belem", fac.createPoint(new Coordinate(-48.39435, -1.3868)), 
				RandomHelper.createUniform(-0.0888, 0.0888), RandomHelper.createUniform(-0.06705, 0.06705)));
		//context.add(new Town(context, "Ponta de Pedras", fac.createPoint(new Coordinate(-48.870833, -1.39)), 
		//		RandomHelper.createUniform(-0.00888, 0.00888), RandomHelper.createUniform(-0.006705, 0.006705), 1));
		
		// add households (must call after all other random distributions have been created)
		addHouseholdsAtRandom(context, numHouseholds, numPersons, projectionFile, multisitedNetwork, personGeography, personSpace);
    //    addHouseholdsHomogeneous(context, numHouseholds, numPersons, projectionFile, multisitedNetwork, personGeography, personSpace);
		// schedule reports
		ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
		ScheduleParameters params = ScheduleParameters.createRepeating(1, 1, MariaPriorities.REPORT);
		schedule.schedule(params, context, "report");
		
		params = ScheduleParameters.createAtEnd(MariaPriorities.FINAL_REPORT - 1);
		schedule.schedule(params, context, "finalReport");
		
		params = ScheduleParameters.createAtEnd(MariaPriorities.CLEANUP);
		schedule.schedule(params, context, "cleanup");
		
		return context;
	}
	
	protected void setUpRandomDistributions() {
		RandomHelper.registerDistribution("harvestTotalCells", RandomHelper.createUniform(0.33333d, 1d));
		
		RandomHelper.registerDistribution("isFemale", RandomHelper.createUniform(0, 1));
		RandomHelper.registerDistribution("age", RandomHelper.createUniform(4, 54));
		
	//	RandomHelper.registerDistribution("hectares", RandomHelper.createUniform(0.5, 10));
		RandomHelper.registerDistribution("hectares", RandomHelper.createUniform(0.5,5));
	//	RandomHelper.registerDistribution("hectares", 5);
		//this is the control for plot size;
		//I'll change it to a homogeneous value for now.
		//Yue, feb 19, 2015
		//and also I need to change the setting in SimpleHouseholdAgent
		RandomHelper.registerDistribution("offerValue", RandomHelper.createUniform(0, 1));
	}
	public static int randInt(int min, int max){
		//Yue wrote this for creating homogeneous hhd structure
		Random rand=new Random();
		int randomNum = rand.nextInt((max-min)+1)+min;
		return randomNum;
	}
	
	protected void addHouseholdsHomogeneous(HumanContext context, int numHouseholds, int numPersons, File projectionFile, Network<SimpleAgent> multisitedNetwork, 
			Geography<SimpleAgent> personGeography, ContinuousSpace<SimpleAgent> personSpace) {
		//add homogeneous hhd;
		CoordinateReferenceSystem sourceCRS;
		try {
			sourceCRS = CRS.parseWKT(new BufferedReader(new FileReader(projectionFile)).readLine());
		} catch (FactoryException e) {
			e.printStackTrace();
			return;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		context.geoUtility = new GeographyUtility<SimpleAgent>(personGeography, sourceCRS, context.cellsize);

	    GeometryFactory fac = new GeometryFactory();
	    
		MathTransform mf;
		try {
			mf = CRS.findMathTransform(sourceCRS, personGeography.getCRS());
		} catch (FactoryException e2) {
			e2.printStackTrace();
			return;
		}
		
		
		GridValueLayer distanceToWater = (GridValueLayer) context.envContext.getValueLayer("Distance to Water");
		GridValueLayer isLand = (GridValueLayer) context.envContext.getValueLayer("is Land");
		
		Map<Integer, Point> agentLocations = new HashMap<Integer, Point>();
		for (Integer i = 0; i < numHouseholds; i++) {
			// place agent at random, minimizing distance to water and maximizing distance from each other (secondary)

			Point bestPoint = null;
			double bestCriteriaScore = Double.MAX_VALUE;	// lower scores are better
			
			for (int j = 0; j < 100 || (bestPoint == null && j < 10000); j++) {
				Point candidate = new Point(RandomHelper.nextIntFromTo(0, context.width - 1),
						RandomHelper.nextIntFromTo(0, context.height - 1));
				
				if (isLand.get(candidate.x, candidate.y) > 0) {
					// determine criteria
					double distToWater = distanceToWater.get(candidate.x, candidate.y);
					
					double distToNearestHouse = context.width * context.height; // in cell units
					for (Point p : agentLocations.values()) {
						double distToP = p.getDistanceTo(candidate);
						if (distToP < distToNearestHouse) distToNearestHouse = distToP;
					}
					distToNearestHouse *= context.cellsize;				// convert cells to metres
					
					// combine criteria: minimize distance to water, maximize distance to houses
					double criteriaScore = distToWater - distToNearestHouse * 0.2;
					if (bestPoint == null || criteriaScore < bestCriteriaScore) {
						bestCriteriaScore = criteriaScore;
						bestPoint = candidate;
					}
				}
			}
			
			if (bestPoint != null)
				agentLocations.put(i, bestPoint);
		}
	
		RandomHelper.registerGenerator("decisionMakingMethodSelector", RandomHelper.nextInt());
		WeightedSelector<Integer> dmm = new WeightedSelector<Integer>("decisionMakingMethodSelector");
		dmm.add(1, context.percentHeuristicHouseholds);
		dmm.add(2, context.percentOptimalHouseholds);
		dmm.add(3, context.percentForwardOptimalHouseholds);
		dmm.add(4, context.percentFullForwardOptimalHouseholds);
		dmm.add(5, context.percentChayanovHouseholds);
		dmm.add(6, context.percentMinLabourHouseholds);
		dmm.add(7, context.percentSubsistenceHouseholds);
		
		if (context.percentHeuristicHouseholds <= 0 && 
				context.percentOptimalHouseholds <= 0 && 
				context.percentForwardOptimalHouseholds <= 0 &&
				context.percentFullForwardOptimalHouseholds <= 0 &&
				context.percentChayanovHouseholds <= 0 &&
				context.percentMinLabourHouseholds <=0 &&
				context.percentSubsistenceHouseholds <=0 ) {
			return;
		}
		
		List<HouseholdAgent> households = new ArrayList<HouseholdAgent>(numHouseholds);
		
		for (Map.Entry<Integer, Point> e : agentLocations.entrySet()) {
			HouseholdAgent h;
			switch(dmm.sample()) {
			case 1:
				h = new HeuristicHouseholdAgent(e.getKey());
				break;
			case 2:
				h = new LinearOptimizingHouseholdAgent(e.getKey());
				break;
			case 3:
				h = new ForwardThinkingLinearOptimizingHouseholdAgent(e.getKey());
				break;
			case 4:
				h = new FullForwardLPHouseholdAgent(e.getKey());
				break;
			case 5:
				h = new ChayanovHouseholdAgent(e.getKey());
				break;
			case 6:
				h= new MinLabourHouseholdAgent (e.getKey());
				break;
			case 7:
				h = new SubsistenceHouseholdAgent (e.getKey());
				break;
			default:
				throw new UnsupportedOperationException("Invalid decision-making method selected.");
			}
			
			// add household to context, list of households, space
			context.add(h);
			
			// addToNetwork(context, h, multisitedNetwork);
			
			context.households.add(h);
			personSpace.moveTo(h, e.getValue().x, e.getValue().y);
			
			{
				com.vividsolutions.jts.geom.Point point = fac.createPoint(context.geoUtility.getCoordinates(e.getValue().x, e.getValue().y, context.originx, context.originy, context.cellsize));
				try {
					Geometry geom = JTS.transform(point, mf);
					//Geometry geom = WWUtils.projectGeometryToWGS84(point, sourceCRS);
					personGeography.move(h, geom);
				} catch (MismatchedDimensionException e1) {
					e1.printStackTrace();
				} catch (TransformException e1) {
					e1.printStackTrace();
				}
			}
			//until here, it's all the same;
			h.setCapital(5000d); // set hhd capital homogeneous, all 5000;
			
			// initialize household
			h.init(context, e.getValue().x, e.getValue().y);
			
			households.add(h);
		}
		
		// initialize households
		// sample adults and children 
		List<Integer> males = new LinkedList<Integer>();
		List<Integer> females = new LinkedList<Integer>();
		List<Integer> boys = new LinkedList<Integer>();
		List<Integer> girls = new LinkedList<Integer>();
		
		for (int i = 0; i < numHouseholds; i++) {
			DemographicWeightedSelector dws = context.demographicSelector.sample();
			Range<Integer> ageRange = dws.sample();
		//	Integer age = RandomHelper.nextIntFromTo(ageRange.getLower(), ageRange.getUpper());
		   
		   int age= randInt(25,30);
		 
		   boolean rnd = new Random().nextBoolean();
		   if (rnd){
			   females.add(age);
			//   System.out.println(females.get(females.size()-1));
		   } else {
			   males.add(age);
		   } 
		   females.add(age);
		   //to set up a homogeneous household structure for testing, Yue, march 3, 2015;
		}
		for (int i=numHouseholds;i<2*numHouseholds;i++){
			int age= randInt(25,30);
			males.add(age);
		}
		
		for (int i=2*numHouseholds; i< numPersons;i++){
			int age = randInt(1,7);
		//	System.out.println("kid age="+age);
			boolean rnd = new Random().nextBoolean();
			if (rnd) {
				girls.add(age);
			} else {
				boys.add(age);
			}
		}
		
		Collections.sort(males);
		Collections.sort(females);
		Collections.sort(boys);
		Collections.sort(girls);
		
		// FIXME: this does NOT work for sensitivity analysis
		
		Collections.shuffle(households);
		
		// distribute males
		for (HouseholdAgent h : households) {
			if (!males.isEmpty()) {
				int age=males.get(males.size()-1);
				Person person = new Person(false, age);
				males.remove(males.size()-1);
				context.add(person);
				h.add(person);
		//		System.out.println("male add age="+person.getAge());
			}
		}
		
		// distribute females
		for (HouseholdAgent h : households) {

			if (!females.isEmpty()) {
				int age = females.get(females.size()-1);
				Person person = new Person(true, age);
			//	System.out.println("female="+females.get(females.size()-1) );
				females.remove(females.size()-1);
				context.add(person);
				h.add(person);
			}
		}
		
		//distribute kids, Yue Feb 19, 2015
		while (!boys.isEmpty()){
	//		System.out.println("boys size="+boys.size());
		for (HouseholdAgent h:households) {
			if(!boys.isEmpty()){
				Person person = new Person(false,boys.get(boys.size()-1));
				boys.remove(boys.size()-1);
		//		System.out.println(person.getAge());
				context.add(person);
				h.add(person);
		//		System.out.println("add boy age="+person.getAge());
			}
		}
		}
		
		while (!girls.isEmpty()) {
	//		System.out.println(girls.size());
		for (HouseholdAgent h:households) {	
			if(!girls.isEmpty()){
				Person person = new Person(true,girls.get(girls.size()-1));
				girls.remove(girls.size()-1);
				context.add(person);
				h.add(person);
		//		System.out.println("add girl age="+person.getAge());
			} }
		}
		
		for (HouseholdAgent h:households)
			for(Person p:h.familyMembers)
			{ 				
					Range<Integer> ws = context.educationSelector.sample();		
				//	Integer edu = RandomHelper.nextIntFromTo(ws.getLower(), ws.getUpper());
					Integer edu = RandomHelper.nextIntFromTo(4, 7);
				//	System.out.println("Line 588= lower"+ws.getLower()+" upper="+ws.getUpper());
					boolean schoolAttendence = new Random().nextBoolean();					
					if (p.getAge()<8) {p.setEducation(0);}					
					else {
						if (p.getAge()<=18&& new Random().nextDouble()<0.4) {
							p.setEducation((p.getAge()-7));
				              p.setSchoolAttendence(true);				             
						}
						if (p.getAge()<=18 && new Random().nextDouble()>0.6) {
							 p.setEducation(new Random().nextInt((p.getAge()-7)));	
							    p.setSchoolAttendence(false);							    
						}
						if (p.getAge()>18) {
							p.setEducation(edu);
							 p.setSchoolAttendence(false);
						}
					}
					p.calculateLabour();
			//		System.out.println(p.getEducation());								
			}
		
		for (HouseholdAgent h : households)
			Database.getInstance().logNewHousehold(context.conn, h);
	}
	
	protected void addHouseholdsAtRandom(HumanContext context, int numHouseholds, int numPersons, File projectionFile, Network<SimpleAgent> multisitedNetwork, 
			Geography<SimpleAgent> personGeography, ContinuousSpace<SimpleAgent> personSpace) {
		CoordinateReferenceSystem sourceCRS;
		try {
			sourceCRS = CRS.parseWKT(new BufferedReader(new FileReader(projectionFile)).readLine());
		} catch (FactoryException e) {
			e.printStackTrace();
			return;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		context.geoUtility = new GeographyUtility<SimpleAgent>(personGeography, sourceCRS, context.cellsize);

	    GeometryFactory fac = new GeometryFactory();
	    
		MathTransform mf;
		try {
			mf = CRS.findMathTransform(sourceCRS, personGeography.getCRS());
		} catch (FactoryException e2) {
			e2.printStackTrace();
			return;
		}
		
		
		GridValueLayer distanceToWater = (GridValueLayer) context.envContext.getValueLayer("Distance to Water");
		GridValueLayer isLand = (GridValueLayer) context.envContext.getValueLayer("is Land");
		
		Map<Integer, Point> agentLocations = new HashMap<Integer, Point>();
		for (Integer i = 0; i < numHouseholds; i++) {
			// place agent at random, minimizing distance to water and maximizing distance from each other (secondary)

			Point bestPoint = null;
			double bestCriteriaScore = Double.MAX_VALUE;	// lower scores are better
			
			for (int j = 0; j < 100 || (bestPoint == null && j < 10000); j++) {
				Point candidate = new Point(RandomHelper.nextIntFromTo(0, context.width - 1),
						RandomHelper.nextIntFromTo(0, context.height - 1));
				
				if (isLand.get(candidate.x, candidate.y) > 0) {
					// determine criteria
					double distToWater = distanceToWater.get(candidate.x, candidate.y);
					
					double distToNearestHouse = context.width * context.height; // in cell units
					for (Point p : agentLocations.values()) {
						double distToP = p.getDistanceTo(candidate);
						if (distToP < distToNearestHouse) distToNearestHouse = distToP;
					}
					distToNearestHouse *= context.cellsize;				// convert cells to metres
					
					// combine criteria: minimize distance to water, maximize distance to houses
					double criteriaScore = distToWater - distToNearestHouse * 0.2;
					if (bestPoint == null || criteriaScore < bestCriteriaScore) {
						bestCriteriaScore = criteriaScore;
						bestPoint = candidate;
					}
				}
			}
			
			if (bestPoint != null)
				agentLocations.put(i, bestPoint);
		}
	
		RandomHelper.registerGenerator("decisionMakingMethodSelector", RandomHelper.nextInt());
		WeightedSelector<Integer> dmm = new WeightedSelector<Integer>("decisionMakingMethodSelector");
		dmm.add(1, context.percentHeuristicHouseholds);
		dmm.add(2, context.percentOptimalHouseholds);
		dmm.add(3, context.percentForwardOptimalHouseholds);
		dmm.add(4, context.percentFullForwardOptimalHouseholds);
		dmm.add(5, context.percentChayanovHouseholds);
		dmm.add(6, context.percentMinLabourHouseholds);
		dmm.add(7, context.percentSubsistenceHouseholds);
	//	System.out.println("Just Check");
		
		if (context.percentHeuristicHouseholds <= 0 && 
				context.percentOptimalHouseholds <= 0 && 
				context.percentForwardOptimalHouseholds <= 0 &&
				context.percentFullForwardOptimalHouseholds <= 0 &&
				context.percentChayanovHouseholds <= 0 &&
				context.percentMinLabourHouseholds <=0 &&
				context.percentSubsistenceHouseholds <=0
				) {
			return;
		}
	//	System.out.println("Just Check 1");
		List<HouseholdAgent> households = new ArrayList<HouseholdAgent>(numHouseholds);
		
		for (Map.Entry<Integer, Point> e : agentLocations.entrySet()) {
			HouseholdAgent h;
			switch(dmm.sample()) {
			case 1:
				h = new HeuristicHouseholdAgent(e.getKey());
				break;
			case 2:
			//	System.out.println(e.getValue());
				h = new LinearOptimizingHouseholdAgent(e.getKey());
				break;
			case 3:
				h = new ForwardThinkingLinearOptimizingHouseholdAgent(e.getKey());
				break;
			case 4:
				h = new FullForwardLPHouseholdAgent(e.getKey());
				break;
			case 5:
				h = new ChayanovHouseholdAgent(e.getKey());
				break;
			case 6:
				h= new MinLabourHouseholdAgent (e.getKey());
				break;
			case 7:
				h = new SubsistenceHouseholdAgent (e.getKey());
		//		System.out.println("hhd has been built");
				break;
			default:
				throw new UnsupportedOperationException("Invalid decision-making method selected.");
			}
	//		System.out.println("Just Check 2");
			// add household to context, list of households, space
			context.add(h);
			
			// addToNetwork(context, h, multisitedNetwork);
			
			context.households.add(h);
			personSpace.moveTo(h, e.getValue().x, e.getValue().y);
			
			{
				com.vividsolutions.jts.geom.Point point = fac.createPoint(context.geoUtility.getCoordinates(e.getValue().x, e.getValue().y, context.originx, context.originy, context.cellsize));
				try {
					Geometry geom = JTS.transform(point, mf);
					//Geometry geom = WWUtils.projectGeometryToWGS84(point, sourceCRS);
					personGeography.move(h, geom);
				} catch (MismatchedDimensionException e1) {
					e1.printStackTrace();
				} catch (TransformException e1) {
					e1.printStackTrace();
				}
			}
			
			// initialize household
			h.init(context, e.getValue().x, e.getValue().y);
			
			households.add(h);
	//		System.out.println("Just Check 3");
		}
		
		// initialize households
		// sample 144 individuals, 27 families, 21 households
		List<Integer> males = new LinkedList<Integer>();
		List<Integer> females = new LinkedList<Integer>();
		
		for (int i = 0; i < numPersons; i++) {
			DemographicWeightedSelector dws = context.demographicSelector.sample();
			Range<Integer> ageRange = dws.sample();
			Integer age = RandomHelper.nextIntFromTo(ageRange.getLower(), ageRange.getUpper());
	//	   System.out.println("lower="+ageRange.getLower()+" upper="+ageRange.getUpper());
			if (dws.isFemale()) {
				females.add(age);
			} else {
				males.add(age);
			}
		
		}
		
		Collections.sort(males);
		Collections.sort(females);
		
		// FIXME: this does NOT work for sensitivity analysis
		
		Collections.shuffle(households);
		
		// distribute males
		for (HouseholdAgent h : households) {
			
			if (!males.isEmpty()) {
				int age=males.get(males.size()-1);
				Person person = new Person(false, age);
				males.remove(males.size()-1);
				context.add(person);
				h.add(person);
		//		System.out.println("male add age="+person.getAge());
			}
		}
		
		// distribute females
		for (HouseholdAgent h : households) {
			
			//int i=households.size();
			if (!females.isEmpty()) {
				int age = females.get(females.size()-1);
				Person person = new Person(true, age);
			//	System.out.println("female="+females.get(females.size()-1) );
				females.remove(females.size()-1);
				context.add(person);
				h.add(person);
				
		//		System.out.println("female add age="+person.getAge());
			}
		}
		
		// distribute remaining agents
		List<Integer> people = new LinkedList<Integer>();
		for (Integer i : males) {
			people.add(-i); // add males
		}
		people.addAll(females);
		for (Integer i : people) {
			// get random household
			HouseholdAgent h = households.get(RandomHelper.nextIntFromTo(0, households.size() - 1));
			
			Person person = new Person (i >= 0, Math.abs(i));
			context.add(person);
			h.add(person);
		//	System.out.println("hhdsize="+h.familyMembers.size());
		}
		
		for (HouseholdAgent h:households)
			for(Person p:h.familyMembers)
			{ 				
					Range<Integer> ws = context.educationSelector.sample();		
					Integer edu = RandomHelper.nextIntFromTo(ws.getLower(), ws.getUpper());
				//	Integer edu = RandomHelper.nextIntFromTo(4, 7);
				//	System.out.println("Line 879= "+edu);
					boolean schoolAttendence = new Random().nextBoolean();					
					if (p.getAge()<8) {p.setEducation(0);}					
					else {
						if (p.getAge()<=18&& new Random().nextDouble()<0.4) {
							p.setEducation((p.getAge()-7));
				              p.setSchoolAttendence(true);				             
						}
						if (p.getAge()<=18 && new Random().nextDouble()>0.6) {
							 p.setEducation(new Random().nextInt((p.getAge()-7)));	
							    p.setSchoolAttendence(false);							    
						}
						if (p.getAge()>18) {
							p.setEducation(edu);
							 p.setSchoolAttendence(false);
						}
					}
					p.calculateLabour();
			//		System.out.println(p.getEducation());								
			}
		//initialize capital randomly, Feb 4, 2015, Yue
		for (HouseholdAgent h:households) {
			Range<Integer> ws=context.capitalSizeSelector.sample();
		//	Range<Integer> capitalRange = ws.
			Integer capital=RandomHelper.nextIntFromTo(ws.getLower(), ws.getUpper());
		//	System.out.println(ws.getLower()+"  "+ws.getUpper());
		//	System.out.println("L903="+capital);
			h.setCapital(capital);
		//	h.setCapital(10000d);
			//sofar i haven't calculate capital empirically, so i'll use a normal distribution
		//	int capital=(int) (new Random().nextGaussian()*1000+10000);
		//	h.setCapital(capital);
		}		
		
		for (HouseholdAgent h : households)
			Database.getInstance().logNewHousehold(context.conn, h);
	}
	
	protected void addHouseholdsFromRaster(HumanContext context, int numPersons, File householdProjectionFile, FileInputStream stream, Network<SimpleAgent> multisitedNetwork, 
			Geography<SimpleAgent> personGeography, ContinuousSpace<SimpleAgent> personSpace) {
		CoordinateReferenceSystem sourceCRS;
		try {
			sourceCRS = CRS.parseWKT(new BufferedReader(new FileReader(householdProjectionFile)).readLine());
		} catch (FactoryException e) {
			e.printStackTrace();
			return;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		context.geoUtility = new GeographyUtility<SimpleAgent>(personGeography, sourceCRS, context.cellsize);

	    GeometryFactory fac = new GeometryFactory();
		MathTransform mf;
		try {
			mf = CRS.findMathTransform(sourceCRS, personGeography.getCRS());
		} catch (FactoryException e2) {
			e2.printStackTrace();
			return;
		}
		
		Map<Integer, Point> agentLocations;
		AgentRasterFile agentFile = new AgentRasterFile(stream);
		agentLocations = agentFile.getAgents();
		context.width = agentFile.getWidth();
		context.height = agentFile.getHeight();
		context.cellsize = agentFile.getCellSize();
		
		RandomHelper.registerGenerator("decisionMakingMethodSelector", RandomHelper.nextInt());
	
		WeightedSelector<Integer> dmm = new WeightedSelector<Integer>("decisionMakingMethodSelector");
		dmm.add(1, context.percentHeuristicHouseholds);
		dmm.add(2, context.percentOptimalHouseholds);
		dmm.add(3, context.percentForwardOptimalHouseholds);
		dmm.add(4, context.percentFullForwardOptimalHouseholds);
		dmm.add(5, context.percentChayanovHouseholds);
		dmm.add(6, context.percentMinLabourHouseholds);
		dmm.add(7, context.percentSubsistenceHouseholds);
		
		if (context.percentHeuristicHouseholds <= 0 && 
				context.percentOptimalHouseholds <= 0 && 
				context.percentForwardOptimalHouseholds <= 0 &&
				context.percentFullForwardOptimalHouseholds <= 0 &&
				context.percentChayanovHouseholds <= 0 &&
				context.percentMinLabourHouseholds <=0 &&
				context.percentSubsistenceHouseholds <=0
				) {
			return;
		}
		
		List<HouseholdAgent> households = new LinkedList<HouseholdAgent>();
		
		for (Map.Entry<Integer, Point> e : agentLocations.entrySet()) {
			HouseholdAgent h;
			switch(dmm.sample()) {
			case 1:
				h = new HeuristicHouseholdAgent(e.getKey());
				break;
			case 2:
				h = new LinearOptimizingHouseholdAgent(e.getKey());
				break;
			case 3:
				h = new ForwardThinkingLinearOptimizingHouseholdAgent(e.getKey());
				break;
			case 4:
				h = new FullForwardLPHouseholdAgent(e.getKey());
				break;
			case 5:
				h= new ChayanovHouseholdAgent (e.getKey());
				break;
			case 6:
				h= new MinLabourHouseholdAgent (e.getKey());
				break;
			case 7:
				h = new SubsistenceHouseholdAgent (e.getKey());
				break;
				//added by Yue, Dec 5th, 2014;
			default:
				throw new UnsupportedOperationException("Invalid decision-making method selected.");
			}
			//This is the switch for different decision-making methods. I can mix them later i think, Yue Dou May 14, 2014
			// add household to context, list of households, space
			context.add(h);
			
			// addToNetwork(context, h, multisitedNetwork);
			
			context.households.add(h);
			personSpace.moveTo(h, e.getValue().x, e.getValue().y);
			
			{
				com.vividsolutions.jts.geom.Point point = fac.createPoint(context.geoUtility.getCoordinates(e.getValue().x, e.getValue().y, agentFile.getOriginx(), agentFile.getOriginy(), agentFile.getCellSize()));
				try {
					Geometry geom = JTS.transform(point, mf);
					if (geom == null) System.out.println("NULL!");
					else System.out.println("NOT NULL!");
					personGeography.move(h, geom);
				} catch (MismatchedDimensionException e1) {
					e1.printStackTrace();
				} catch (TransformException e1) {
					e1.printStackTrace();
				}
			}
			
		//	h.setCapital(10000d); //Original place
			
			// initialize household
			h.init(context, e.getValue().x, e.getValue().y);
			households.add(h);
		//	households.add(h);
			//h.setCashTran();
		}
		
		// initialize households
		// sample 144 individuals, 27 families, 21 households
		List<Integer> males = new LinkedList<Integer>();
		List<Integer> females = new LinkedList<Integer>();
		
		for (int i = 0; i < numPersons; i++) {
			DemographicWeightedSelector dws = context.demographicSelector.sample();
			Range<Integer> ageRange = dws.sample();
			Integer age = RandomHelper.nextIntFromTo(ageRange.getLower(), ageRange.getUpper());
			if (dws.isFemale()) {
				females.add(age);
			} else {
				males.add(age);
			}
		}
		
		Collections.sort(males);
		Collections.sort(females);
		
		Collections.shuffle(households);
		
		// distribute males
		for (HouseholdAgent h : households) {
			if (!males.isEmpty()) {
				Person person = new Person(false, males.get(males.size() - 1));
				context.add(person);
				h.add(person);
			}
		}
		
		// distribute females
		for (HouseholdAgent h : households) {
			if (!females.isEmpty()) {
				Person person = new Person(true, females.get(females.size() - 1));
				context.add(person);
				h.add(person);
			}
		}
		
		// distribute remaining agents
		List<Integer> people = new LinkedList<Integer>();
		for (Integer i : males) {
			people.add(-i); // add males
		}
		people.addAll(females);
		for (Integer i : people) {
			// get random household
			HouseholdAgent h = households.get(RandomHelper.nextIntFromTo(0, households.size() - 1));
			
			Person person = new Person (i >= 0, Math.abs(i));
			context.add(person);
			h.add(person);
		//	System.out.println("hhdsize="+h.familyMembers.size());
		}
		for (HouseholdAgent h:households)
			for(Person p:h.familyMembers)
			{ 
				for (int i=0;i<h.familyMembers.size();i++)
			{
					Range<Integer> ws = context.educationSelector.sample();		
					Integer edu = RandomHelper.nextIntFromTo(ws.getLower(), ws.getUpper());
					boolean schoolAttendence = new Random().nextBoolean();
					
					if (p.getAge()>18)
						{p.setEducation(edu);
						 p.setSchoolAttendence(false);
						 
						}
					if (p.getAge()<8) {p.setEducation(0);}
						
					else if (p.getAge()>=8 && new Random().nextDouble()<0.8)
				         	{ p.setEducation((p.getAge()-7));
				              p.setSchoolAttendence(true);}
					else {
					    	    p.setEducation(new Random().nextInt((p.getAge()-7)));	
							    p.setSchoolAttendence(false);}
					}
				p.calculateLabour();
					
			}
			
		//initialize capital randomly, Feb 4, 2015, Yue
		for (HouseholdAgent h:households) {
			Range<Integer> ws=context.capitalSizeSelector.sample();
			Integer capital=RandomHelper.nextIntFromTo(ws.getLower(), ws.getUpper());
			h.setCapital(capital);
		}
		for (HouseholdAgent h : households)
			Database.getInstance().logNewHousehold(context.conn, h);
		    
	}
	
	@SuppressWarnings("unused")
	private void addToNetwork(HumanContext context, HouseholdAgent h, Network<SimpleAgent> multisitedNetwork) {
		/*
		// add household to network (B-A with randomness)
		double totalDegree = multisitedNetwork.getDegree() / 2d;	// account for double edges
		if (totalDegree > 0) {
			for (HouseholdAgent target : context.households) {
				double rDegree;
				try {
					rDegree = multisitedNetwork.getInDegree(target);
				} catch (NullPointerException e1) {
					rDegree = 0;
				}
				
				if (RandomHelper.nextDoubleFromTo(0, totalDegree) < (rDegree / totalDegree)) {
					double weight = RandomHelper.nextDoubleFromTo(0, 1);
					multisitedNetwork.addEdge(new RepastEdge<SimpleAgent>(h, target, true, weight));
					multisitedNetwork.addEdge(new RepastEdge<SimpleAgent>(target, h, true, weight));
				}
			}
		}
		
		// pick random target with high probability
		if (context.households.size() > 1 && RandomHelper.nextDoubleFromTo(0, 1) < 0.8) {
			HouseholdAgent randomTarget = context.households.get(RandomHelper.nextIntFromTo(0, context.households.size() - 1));
			if (multisitedNetwork.getEdge(h, randomTarget) == null) {
				double weight = RandomHelper.nextDoubleFromTo(0, 1);
				multisitedNetwork.addEdge(new RepastEdge<SimpleAgent>(h, randomTarget, true, weight));
				multisitedNetwork.addEdge(new RepastEdge<SimpleAgent>(randomTarget, h, true, weight));
			}
		}
		*/
	}
}
