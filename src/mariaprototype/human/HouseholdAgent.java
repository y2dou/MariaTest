package mariaprototype.human;

import java.awt.Color;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import javolution.util.FastMap;
import javolution.util.FastTable;
import mariaprototype.MariaPriorities;
import mariaprototype.Point;
import mariaprototype.SimpleAgent;
import mariaprototype.database.Database;
import mariaprototype.environmental.House;
import mariaprototype.environmental.LandCell;
import mariaprototype.environmental.LandUse;
import mariaprototype.environmental.LandscapeCell;
import mariaprototype.human.messaging.EnvelopePool;
import mariaprototype.human.messaging.MarketPrices;
import mariaprototype.human.messaging.MessageEnvelope;
import mariaprototype.human.messaging.NetworkAgent;
import mariaprototype.utility.XYGenerator;
import repast.simphony.annotate.AgentAnnot;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.environment.RunState;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.SpatialException;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridDimensions;
import repast.simphony.valueLayer.GridValueLayer;

/**
 * Base HouseholdAgent class, which should encompass common non-decision-making
 * attributes of every household.
 * 
 * This class can be extended by:
 * <ul>
 * <li>Deterministic agent
 * <ul>
 * <li>Rule-based agent</li>
 * <li>Fuzzy (JESS) rule-based agent</li>
 * </ul>
 * </li>
 * <li>Adaptive agent
 * <ul>
 * <li>Learning fuzzy (JESS) rule-based agent</li>
 * <li>Simulated annealing (based on age)</li>
 * <li>Simulated annealing w/ individual voting (based on age)</li>
 * </ul>
 * </li>
 * <li>Imitative agent
 * <ul>
 * <li>Nearest neighbour</li>
 * <li>...other examples from FEARLUS</li>
 * </ul>
 * </li>
 * <li>Heuristic agent</li>
 * </ul>
 * 
 * @author arcabrer
 * 
 */
@AgentAnnot(displayName = "HouseholdAgentAnnot")
public abstract class HouseholdAgent extends SimpleAgent implements
		NetworkAgent {
	private static final AtomicInteger idGenerator = new AtomicInteger(0);
	private int id;
	private Color color;

	protected Context<?> context;
	protected HouseholdAgentState lastState;

	protected Point location;
    protected double annualIncome; 
	//Yue, this is to track annual income, especially to solve subsistence problem.
	protected double capital;
	protected double labour;
	protected double totalLabour;
	//the total labour in hhd, include kids and elders who receive ct.
	protected double subsistenceRequirement;
	protected double subsistenceAcaiRequirement;
	protected double subsistenceManiocRequirement;
	protected double pension;
	// add cash transfer to hhd agent, June 17, 2014;
	protected double bf;
	private double aveFemaleEdu;
	private double husbandEdu;
	private int husbandAge;
	private double utility;
	private double alpha;
	// private double beta;
	private double wage = 0.0;
	private double perCapitaIncome;
	private int hhdSize;
    protected boolean upLand=false;


	public void setUtility(double utility) {
		this.utility = utility;
	}

	public double getUtility() {
		return this.utility;
	}

	private double jobProb;
	// this is the average female household member's education level, Yue, Oct
	// 27, 2014;

	protected FastTable<Person> familyMembers = new FastTable<Person>();

	protected Map<GridDimensions, MyLandCell> tenure = new FastMap<GridDimensions, MyLandCell>();
	// protected FastTable<MyLandCell> tenure = new FastTable<MyLandCell>();

	protected double labourMultiplier = 1;
	protected double capitalMultiplier = 1;
	protected double forestFallowLabour = 0;
	protected double forestFallowCost = 0;
	protected double fallowLabour = 1;
	protected double fallowCost = 1;
	protected double acaiLabour = 1;
	protected double acaiCost = 1;
	protected double maniocLabour = 1;
	protected double maniocCost = 1;
	protected double maintainAcaiLabour = 1;
	protected double maintainAcaiCost = 1;
	protected double maintainManiocLabour = 1;
	protected double maintainManiocCost = 1;
	protected double harvestAcaiLabour = 1;
	protected double harvestManiocLabour = 1;
	protected double harvestTimberLabour = 1;

	protected List<JobOffer> jobOffers;

	// harvest (retrospective) variables
	protected double acaiYield = 0;
	protected double maniocYield = 0;
	protected double timberYield = 0;

	protected Connection conn;

	protected FastMap<NetworkedUrbanAgent, RepastEdge<SimpleAgent>> linkedHouseholds = new FastMap<NetworkedUrbanAgent, RepastEdge<SimpleAgent>>();

	public HouseholdAgent() {
		this(idGenerator.getAndIncrement());
		initialize();
	}

	public HouseholdAgent(int id) {
		this.id = id;
		initialize();
	}

	protected void initialize() {
		labourMultiplier = (Double) RunEnvironment.getInstance()
				.getParameters().getValue("labourMultiplier");
		capitalMultiplier = (Double) RunEnvironment.getInstance()
				.getParameters().getValue("capitalMultiplier");
		forestFallowLabour = (Double) RunEnvironment.getInstance()
				.getParameters().getValue("forestFallowLabour");
		forestFallowCost = (Double) RunEnvironment.getInstance()
				.getParameters().getValue("forestFallowCost");
		fallowLabour = (Double) RunEnvironment.getInstance().getParameters()
				.getValue("fallowLabour");
		fallowCost = (Double) RunEnvironment.getInstance().getParameters()
				.getValue("fallowCost");
		acaiLabour = (Double) RunEnvironment.getInstance().getParameters()
				.getValue("acaiLabour");
		acaiCost = (Double) RunEnvironment.getInstance().getParameters()
				.getValue("acaiCost");
		maniocLabour = (Double) RunEnvironment.getInstance().getParameters()
				.getValue("maniocLabour");
		maniocCost = (Double) RunEnvironment.getInstance().getParameters()
				.getValue("maniocCost");
		maintainAcaiLabour = (Double) RunEnvironment.getInstance()
				.getParameters().getValue("maintainAcaiLabour");
		maintainAcaiCost = (Double) RunEnvironment.getInstance()
				.getParameters().getValue("maintainAcaiCost");
		maintainManiocLabour = (Double) RunEnvironment.getInstance()
				.getParameters().getValue("maintainManiocLabour");
		maintainManiocCost = (Double) RunEnvironment.getInstance()
				.getParameters().getValue("maintainManiocCost");
		harvestAcaiLabour = (Double) RunEnvironment.getInstance()
				.getParameters().getValue("harvestAcaiLabour");
		harvestManiocLabour = (Double) RunEnvironment.getInstance()
				.getParameters().getValue("harvestManiocLabour");
		harvestTimberLabour = (Double) RunEnvironment.getInstance()
				.getParameters().getValue("harvestTimberLabour");
	/*	if (Policy.pensionProgramStatic) {
			pension = (Double) RunEnvironment.getInstance().getParameters()
			.getValue("pension");
		}
		else {
			pension = 0;
		}
		if (Policy.bfProgramStatic) {
		   bf = (Double) RunEnvironment.getInstance().getParameters().getValue("bf");
		}
		else {
			bf = 0;
		}*/
		
		alpha = (Double) RunEnvironment.getInstance().getParameters().getValue(
				"alpha");
		// beta = (Double)
		// RunEnvironment.getInstance().getParameters().getValue("beta");
		color = new Color((id * 1291 + 1297) % 256, (id * 2267 + 337) % 256,
				(id * 1553 + 3) % 256);
		jobOffers = new LinkedList<JobOffer>();
	
		conn = (Connection) RunState.getInstance()
				.getFromRegistry("connection");
	}

	@Override
	public final int getID() {
		return id;
	}

	public Point getLocation() {
		return location;
	}

	// this add(Person) function is for data initialization, it doesn't update
	// everytime.
	public final void add(Person person) {
		familyMembers.add(person);
		// System.out.println("Member age <100");
	}

	// update familyMember every tick, to remove who are too old
	// edited by Yue, July 9, 2014
	// @ScheduledMethod(start = 1, interval = 1, priority =
	// MariaPriorities.DATA_PREPARATION)


@ScheduledMethod(start = 1, interval = 1, priority = MariaPriorities.AGENT_UPDATE_STATUES)
	public void demography() {
		double deathProb = 0.0;
		boolean reproduceCapability = false;
		boolean isFemale = RandomHelper.getDistribution("isFemale").nextInt() == 1;// random
																					// generate
																					// the
																					// gender

		for (Person p : this.familyMembers) {
			if (p.getAge() > 50) {
				switch (p.getAgeRange()) {
				case 9:
					deathProb = 0.005;
					break;
				case 10:
					deathProb = 0.010;
					break;
				case 11:
					deathProb = 0.026;
					break;
				case 12:
					deathProb = 0.098;
					break;
				}
				if (new Random().nextDouble() < deathProb)
				// generate a random number to compare with the death
				// probability,
				// which is calculated from Table 3223, in
				// population_deathrate.xlsx
				{
					this.familyMembers.remove(p);
					// System.out.println("death"+" id="+this.id+" size="+this.familyMembers.size());
			//		 System.out.println("one person died: "+this.familyMembers.size());
			//		 age();
				}
			} else {
				if (p.getReproduce()) {
					Person pp = new Person(isFemale, 0);
					// this.familyMembers.add(pp);
					this.familyMembers.addLast(pp);
					// System.out.println("id="+this.id+"  a baby is born!!!Boooo  "+this.familyMembers.size());
				}
			}
		}
	}

@ScheduledMethod(start = 1, interval = 1, priority = MariaPriorities.AGENT_UPDATE_STATUES)
//@ScheduledMethod(start = 1, interval = 1, priority = 100)
  public void updateStatues() {
	    age();	
		updateEducation(); //update school years;
		setLabour();	
		setPension();
		setBf();
	}

	public void age(){
	//	int n = this.familyMembers.size();
	//	System.out.println("family size="+this.familyMembers.size());
		
	    int age;

	    for (Person p : this.familyMembers) {
			age = p.getAge();
			p.setAge(age+1);
			p.setAgeRange(p.getAge());
			p.calculatePension();
			p.calculateBf(age);
			p.setSubsistenceUnit(age);
		}
		
		if (this.getLinkedHouseholds().size() > 0) {
			List<NetworkedUrbanAgent> checkHus = new LinkedList<NetworkedUrbanAgent>();
			checkHus.addAll(this.getLinkedHouseholds().keySet());
			Iterator<NetworkedUrbanAgent> recallIter = checkHus.iterator();

			while (recallIter.hasNext()) {
				Person p = recallIter.next().getPerson();
				p.setAge(p.getAge()+1);
			}
		}
	}
	public void updateEducation() {
		// children under age and adults, their education is zero or ++;
		/*
		 * here're two situations: (1) there is BF program or not (2) there is
		 * BF program but hhd have it or not
		 */
		double tick = RunState.getInstance().getScheduleRegistry().getModelSchedule().getTickCount();
		double bfPolicy = 0;
		int n = this.familyMembers.size();
		
		// case One, no bolsaFamilia
	    if (Policy.bfLists.containsKey(-1.0)) {
	    	bfPolicy = Policy.bfLists.get(-1.0).doubleValue();
	    }
	    else {
	    	bfPolicy = Policy.bfLists.get(tick).doubleValue() ;
	    }
	    
	//    System.out.println("bfPolicy = "+bfPolicy);
	    
		if (bfPolicy == 0) {
			if (this.perCapitaIncome >= Policy.perCapitaIncomeThreshold) {
				// CaseOne.one family income is larger than perCapitaThreshold
				for (int i = 0; i < n; i++) {
					Person p = this.familyMembers.get(i);
					if (p.getAge() > 18) {
						p.setEducation(p.getEducation());
						p.setSchoolAttendence(false);
					} else if (p.getAge() < 7) {
						p.setEducation(0);
						p.setSchoolAttendence(false);
					}
					/*
					 * else if (new Random().nextDouble()<0.6){
					 * p.setEducation(p.getEducation()+1);//rich family can
					 * afford kids to go to school
					 * p.setSchoolAttendence(true);//change it to true for the
					 * labour summary; }
					 */
					else {
						double prob = new Random().nextDouble();
						switch (p.getEduLevel()) {
						case 1:
							if (prob < 0.6) {
								p.setEducation(p.getEducation() + 1);
								p.setSchoolAttendence(true);
							}
							break;
						case 2:
							if (prob < 0.4) {
								p.setEducation(p.getEducation() + 1);
								p.setSchoolAttendence(true);
							}
							break;
						case 3:
							if (prob < 0.2) {
								p.setEducation(p.getEducation() + 1);
								p.setSchoolAttendence(true);
							}
							break;
						}
					}

					// System.out.println(p.isSchoolAttendence());
				}
			} else {// CaseOne.two family income is smaller than
					// perCapitaThreshold
				for (int i = 0; i < n; i++) {
					Person p = this.familyMembers.get(i);
					if (p.getAge() > 18) {
						p.setEducation(p.getEducation());
						p.setSchoolAttendence(false);
					} else if (p.getAge() < 7) {
						p.setEducation(0);
						p.setSchoolAttendence(false);
					} else {
						double prob = new Random().nextDouble();
						switch (p.getEduLevel()) {
						case 1:
							if (prob < 0.4) {
								p.setEducation(p.getEducation() + 1);
								p.setSchoolAttendence(true);
							}
							break;
						case 2:
							if (prob < 0.2) {
								p.setEducation(p.getEducation() + 1);
								p.setSchoolAttendence(true);
							}
							break;
						case 3:
							if (prob < 0.1) {
								p.setEducation(p.getEducation() + 1);
								p.setSchoolAttendence(true);
							}
							break;
						}
					}
					// System.out.println(p.getEducation());
				}
			}
		}
		// CaseTwo, when there is bolsa Familia
		if (bfPolicy > 0) {
			// if
			// (this.perCapitaIncome>=Policy.perCapitaIncomeThreshold&&this.bf>0)
			// {
			// here i think it should only be check perCapitaIncome
			if (this.perCapitaIncome >= Policy.perCapitaIncomeThreshold) {
				// CaseOne.one family income is larger than perCapitaThreshold
				for (int i = 0; i < n; i++) {
					Person p = this.familyMembers.get(i);
					if (p.getAge() > 18) {
						p.setEducation(p.getEducation());
						p.setSchoolAttendence(false);
					} else if (p.getAge() < 7) {
						p.setEducation(0);
						p.setSchoolAttendence(false);
					} else
					/*
					 * if (new Random().nextDouble()<0.6){
					 * p.setEducation(p.getEducation()+1);
					 * p.setSchoolAttendence(true);}
					 */
					{
						double prob = new Random().nextDouble();
						switch (p.getEduLevel()) {
						case 1:
							if (prob < 0.6) {
								p.setEducation(p.getEducation() + 1);
								p.setSchoolAttendence(true);
							}
							break;
						case 2:
							if (prob < 0.4) {
								p.setEducation(p.getEducation() + 1);
								p.setSchoolAttendence(true);
							}
							break;
						case 3:
							if (prob < 0.2) {
								p.setEducation(p.getEducation() + 1);
								p.setSchoolAttendence(true);
							}
							break;
						}
					}
				}
			} else {
				for (int i = 0; i < n; i++) {
					Person p = this.familyMembers.get(i);
					if (p.getAge() > 18) {
						p.setEducation(p.getEducation());
						p.setSchoolAttendence(false);
					} else {
						if (p.getAge() < 7) {
							p.setEducation(0);
							p.setSchoolAttendence(false);
						} else {
							double prob = new Random().nextDouble();
							switch (p.getEduLevel()) {
							case 1: {
								p.setEducation(p.getEducation() + 1);
								p.setSchoolAttendence(true);
							}
								break;
							case 2: {
								p.setEducation(p.getEducation() + 1);
								p.setSchoolAttendence(true);
							}
								break;
							case 3:
								if (prob < 0.5) {
									p.setEducation(p.getEducation() + 1);
									p.setSchoolAttendence(true);
								}
								break;
							}
						}

					}
				}
			}
		}

		for (int i = 0; i < n; i++) {
			Person p = this.familyMembers.get(i);
			p.calculateLabour();// labor and education has to be calculated
								// after education update.
			p.setEduLevel(p.getEducation());
			p.setAgeEdu(p.getAge(), p.getEduLevel());

		}

	}
	

	/*
	 * public void reproduce (){ //check if there's eligible woman in this
	 * household boolean reproduceCapability=false; for ( Person
	 * p:this.familyMembers){ if(p.getGenderAge()>20 && p.getGenderAge()<50) {
	 * reproduceCapability=true; } }
	 * 
	 * if (reproduceCapability==true) { boolean isFemale=
	 * RandomHelper.getDistribution("isFemale").nextInt() == 1; Person pp= new
	 * Person (isFemale,0); //random generate the gender if (new
	 * Random().nextDouble() < 0.5) { this.familyMembers.add(pp);} //50% chance
	 * that this member will be added to the household. //
	 * System.out.println("aveFemaleEdu="
	 * +this.aveFemaleEdu+";husbandedu="+this.husbandEdu
	 * +";probability="+jobProb); } else { System.out.println(" can't reproduce"
	 * ); } }
	 */

	protected final void resetLabour() {
		labour = 0;
		for (Person p : familyMembers) {
			p.calculateLabour();
			labour += p.getLabour();
		}
	}

	protected final void resetHarvestCount() {
		acaiYield = 0;
		maniocYield = 0;
		timberYield = 0;
	}

	/**
	 * Initialize the household agent.
	 * 
	 * @param coordinates
	 */
	public void init(HumanContext humanContext, int... coordinates) {
		location = new Point(coordinates[0], coordinates[1]);
		humanContext.envContext.add(new House(coordinates));
	}

	@ScheduledMethod(start = 1, interval = 1, priority = MariaPriorities.MESSAGE_PASSING_1)
	public abstract void messagePassing1(); // send initial messages: send
											// current state

	@ScheduledMethod(start = 1, interval = 1, priority = MariaPriorities.PLANNING)
	public abstract void plan(); // process messages? plan out current+future

	@ScheduledMethod(start = 1, interval = 1, priority = MariaPriorities.MESSAGE_PASSING_2)
	public abstract void messagePassing2(); // send messages

	@ScheduledMethod(start = 1, interval = 1, priority = MariaPriorities.INTERMEDIATE)
	public abstract void intermediate(); // post-economic adjustments to plan

	@ScheduledMethod(start = 1, interval = 1, priority = MariaPriorities.MESSAGE_PASSING_3)
	public abstract void messagePassing3(); // send messages

	/**
	 * Implements *land use* change and allocation.
	 */
	@ScheduledMethod(start = 1, interval = 1, priority = MariaPriorities.ACTION)
	public abstract void act(); // implement current

	@ScheduledMethod(start = 1, interval = 1, priority = MariaPriorities.MESSAGE_PASSING_4)
	public abstract void messagePassing4(); // send messages

	@ScheduledMethod(start = 1, interval = 1, priority = MariaPriorities.HARVEST)
	public abstract void harvest(); // send messages

	@ScheduledMethod(start = 1, interval = 1, priority = MariaPriorities.RETROSPECT)
	public abstract void retrospect(); // send messages

	@Override
	public void store(MessageEnvelope message) {
	}

	@SuppressWarnings("unchecked")
	protected final void broadcastState() {
		EnvelopePool pool = (EnvelopePool) context.getObjects(
				EnvelopePool.class).get(0);

		lastState.update(this);
		MessageEnvelope messageEnvelope = pool.getBroadcastEnvelope(this,
				lastState);

		// send lastState
		Network<HouseholdAgent> network = (Network<HouseholdAgent>) context
				.getProjection("Multisited Household Network");
		Iterable<RepastEdge<HouseholdAgent>> edges = network.getEdges(this);
		Iterator<RepastEdge<HouseholdAgent>> iter = edges.iterator();
		while (iter.hasNext()) {
			RepastEdge<HouseholdAgent> edge = iter.next();
			if (!edge.isDirected() || edge.getSource() == this) {
				messageEnvelope.send((NetworkAgent) edge.getTarget());
			}
		}
		messageEnvelope.discard();
	}

	@Override
	public final AgentType getAgentType() {
		return AgentType.HOUSEHOLD_AGENT;
	}

	public int getHhdSize() {
		int n = this.familyMembers.size();
		if (this.getLinkedHouseholds().size() > 0) {
			List<NetworkedUrbanAgent> checkHus = new LinkedList<NetworkedUrbanAgent>();
			checkHus.addAll(this.getLinkedHouseholds().keySet());
			Iterator<NetworkedUrbanAgent> recallIter = checkHus.iterator();

			while (recallIter.hasNext()) {
				Person p = recallIter.next().getPerson();
				n = n + 1;
				// System.out.println(this.id);
			}
		}

		return n;
	}

	public final double getCapital() {
		// this.setCashTran();
		// System.out.println("setCashTran="+this.cashTran);
		capital = capital + this.getWage();
//				- this.getSubsistenceRequirements();
		// this.getPension()+this.getBf()-this.getSubsistenceRequirements();
		// System.out.println("tick="+RunState.getInstance().getScheduleRegistry().getModelSchedule().getTickCount()+" hhdID "+this.getID()+" getWage="+this.getWage());
		return capital;

	}

	public void setCapital(double capital) {
		// setCapital only happens at the initialization stage;
		// it's not called every stage;
		// this.setCashTran();
		// this.capital = capital+this.getCashTran();
		// i don't want to add a new variable called totalCapital, there will be
		// too many revision;
		// This way can include cash Transfer into capital;
		// Yue
		// Oct 13, I'm going to move the cash transfer into capital at decision
		// making part, as a source of income.
		this.capital = capital;
	}

	public double getWage() {
		// System.out.println();
		return wage;
	}

	public void setWage(double wage) {
		this.wage = wage;
	}

	// public void setPerCapitaIncome(double perCapitaIncome){
	// this.perCapitaIncome=perCapitaIncome;
	// }
	public double getPerCapitaIncome() {
		// int n=this.familyMembers.size();
		int n = this.getHhdSize();
		perCapitaIncome = (this.capital - this.pension - this.bf) / n;
		return perCapitaIncome;
		//this is the livelihood per capita income
	}

	public void setHusband() {

		ArrayList<Person> ps = new ArrayList<Person>();

		for (int i = 0; i < this.familyMembers.size(); i++) {

			Person p = this.familyMembers.get(i);
			if (p == null) {

			}
			if (!p.isFemale()) {

				// ps.add(i, p);
				ps.add(p);

			}
		}

		if (this.getLinkedHouseholds().size() > 0) {
			List<NetworkedUrbanAgent> checkHus = new LinkedList<NetworkedUrbanAgent>();
			checkHus.addAll(this.getLinkedHouseholds().keySet());
			Iterator<NetworkedUrbanAgent> recallIter = checkHus.iterator();

			while (recallIter.hasNext()) {
				Person p = recallIter.next().getPerson();
				if (!p.isFemale()) {
					ps.add(p);
				}
			}
		}
		Collections.sort(ps, new Comparator<Person>() {

			@Override
			public int compare(Person arg0, Person arg1) {
				// TODO Auto-generated method stub

				int obj0 = arg0.getAge();
				int obj1 = arg1.getAge();
				int retval = ((Integer) obj0).compareTo(obj1);

				return retval;
			}
			// sort family members based on the age. which order?
		});

		this.husbandEdu = ps.get(ps.size() - 1).getEducation();
		this.husbandAge = ps.get(ps.size() - 1).getAge();

	}

	public double getHusbandEdu() {
		return husbandEdu;
	}

	public int getHusbandAge() {
		return husbandAge;
	}

	public double getJobPossibility() {
		double e = java.lang.Math.E;
		setHusband();
		aveFemaleEdu = this.getAveFemaleEdu();
		double t = -1.463 + 0.064 * aveFemaleEdu + 0.148 * this.husbandEdu;
		jobProb = Math.pow(e, t) / (Math.pow(e, t) + 1);
		this.jobProb = jobProb;
		// System.out.println("aveFemaleEdu="+aveFemaleEdu+";husbandedu="+this.husbandEdu+";probability="+jobProb);
		return jobProb;
	}
	
	public void setLabour(){
		labour = 0;
		totalLabour=0;
		for (Person p : this.familyMembers) {
			p.calculateLabour();			
			totalLabour +=p.getTotalLabour();
	//		if (p.getPension()==0 && !p.isSchoolAttendence())     
				labour +=p.getLabour();			
		}

		if (this.getLinkedHouseholds().size() > 0) {
			List<NetworkedUrbanAgent> checkHus = new LinkedList<NetworkedUrbanAgent>();
			checkHus.addAll(this.getLinkedHouseholds().keySet());
			Iterator<NetworkedUrbanAgent> recallIter = checkHus.iterator();

			while (recallIter.hasNext()) {
				Person p = recallIter.next().getPerson();
				p.calculateLabour();
				labour += p.getLabour();
				totalLabour +=p.getTotalLabour();
			}
		}

	}

	public double getLabour() {
		return labour;
	}
	
	public double getTotalLabour(){
		return totalLabour;
	}

	public void setPension(){
		double pension = 0;
		int n = this.familyMembers.size();
		for (int i = 0; i < n; i++) {
			pension += this.familyMembers.get(i).getPension();
		}
		this.pension = pension;
		capital +=pension;
	//	System.out.println("set Pension: "+ pension);
	}
	
	public double getPension() {
		// System.out.println("getCashTran="+cashTran);
		
		// System.out.println("tick="+RunState.getInstance().getScheduleRegistry().getModelSchedule().getTickCount()+"  hhdID="+this.getID()+" cashTrans="+cashTran);
		
	
	//	capital += pension;
		return pension;

	}

	public void setBf() {
	//	System.out.println("tick="+RunState.getInstance().getScheduleRegistry().getModelSchedule().getTickCount()+" bolsa familia="+bf);
		double bf = 0;
		int n = familyMembers.size();
		if (this.perCapitaIncome < Policy.perCapitaIncomeThreshold) {
			// looks like 500 is a reasonble threshold, otherwise there's no
			// family eligible
			// to check if this family is eligible to get the bolsa familia;
			// Bolsa Fam�lia currently gives families with per-capita monthly
			// income below $140 BRL (poverty line, ~$56 USD)
			// a monthly stipend of $32 BRL (~$13 USD) per vaccinated child (<
			// 16 years old) attending school (up to 5),
			// which =84 annual income in this model;
			// if pension is 20in program=500*12 in reality;324times
			// then bf=32*12/324=15
			int j = 0;
			for (int i = 0; i < n; i++) {
				if (j < 5 && familyMembers.get(i).getBf() > 0) {
					bf += familyMembers.get(i).getBf();
					j++;
				}
			}
			// System.out.println("bf="+bf);
		}
		this.bf = bf;
		// if(bf>0)
		// {
	//	 System.out.println("tick="+RunState.getInstance().getScheduleRegistry().getModelSchedule().getTickCount()+
	//	 "  hhdID="+this.getID()+" bolsa familia="+bf);
		capital +=bf;
	}
	
	public double getBf() {

		return bf;

	}

/*	public final double getSubsistenceRequirements() {
		// Feb 04, 2015, Yue
		String className = this.getClass().getSimpleName();

		double subsistenceRequirements = 0;
		for (int i = 0; i < this.familyMembers.size(); i++) {
			Person p = this.familyMembers.get(i);
			p.setSubsistenceUnit(p.getAge());
			subsistenceRequirements += p.getSubsistenceUnit();
		}

		if (this.getLinkedHouseholds().size() > 0) {
			List<NetworkedUrbanAgent> checkHus = new LinkedList<NetworkedUrbanAgent>();
			checkHus.addAll(this.getLinkedHouseholds().keySet());
			Iterator<NetworkedUrbanAgent> recallIter = checkHus.iterator();

			while (recallIter.hasNext()) {
				Person p = recallIter.next().getPerson();
				p.setSubsistenceUnit(p.getAge());
				subsistenceRequirements += p.getSubsistenceUnit();
			} 
		
		}
         
		// System.out.println("class name: " + className);
	//	if (className.equals("SubsistenceHouseholdAgent")&&this.maniocYield>0) {
		if (className.equals("SubsistenceHouseholdAgent")) {	
			return 0.7 * subsistenceRequirements;
			//if household produces manioc, then their subsistence requirement is only partly.
		} else {
			return subsistenceRequirements;
		}

		//second version, Yue, Sept 6, 2015
		//Idea is that hhd except subsistence households have to pay 
		//price*SubsistenceAmount 
		String className = this.getClass().getSimpleName();
		double subsistenceRequirements = 0;
		
//		subsistenceRequirements = this.getSubAcaiRequirement()* marketPrices.getPrice(acai);
	} */

	public double getSubsistenceAcaiRequirement() {
		double subsistenceAcaiRequirement = 0;
		for (int i = 0; i < this.familyMembers.size(); i++) {
			Person p = this.familyMembers.get(i);
			p.setSubsistenceAcaiUnit(p.getAge());
			subsistenceAcaiRequirement += p.getSubsistenceAcaiUnit();
		}

		if (this.getLinkedHouseholds().size() > 0) {
			List<NetworkedUrbanAgent> checkHus = new LinkedList<NetworkedUrbanAgent>();
			checkHus.addAll(this.getLinkedHouseholds().keySet());
			Iterator<NetworkedUrbanAgent> recallIter = checkHus.iterator();

			while (recallIter.hasNext()) {
				Person p = recallIter.next().getPerson();
				p.setSubsistenceAcaiUnit(p.getAge());
				subsistenceAcaiRequirement += p.getSubsistenceAcaiUnit();
			}
		}
		return subsistenceAcaiRequirement;
	}

	public double getSubsistenceManiocRequirement() {
		double subsistenceManiocRequirement = 0;
		for (int i = 0; i < this.familyMembers.size(); i++) {
			Person p = this.familyMembers.get(i);
			p.setSubsistenceManiocUnit(p.getAge());
			subsistenceManiocRequirement += p.getSubsistenceManiocUnit();
		}

		if (this.getLinkedHouseholds().size() > 0) {
			List<NetworkedUrbanAgent> checkHus = new LinkedList<NetworkedUrbanAgent>();
			checkHus.addAll(this.getLinkedHouseholds().keySet());
			Iterator<NetworkedUrbanAgent> recallIter = checkHus.iterator();

			while (recallIter.hasNext()) {
				Person p = recallIter.next().getPerson();
				p.setSubsistenceManiocUnit(p.getAge());
				subsistenceManiocRequirement += p.getSubsistenceManiocUnit();
			}
		}
		// System.out.println("L761 Manioc Requirement "+subManiocRequirement);
		return subsistenceManiocRequirement;

	}

	/*
	 * public void setCashTran( ) { double cashTran=0; int
	 * n=this.familyMembers.size(); // System.out.println("n="+n); for (int
	 * i=0;i<n;i++){ cashTran+=this.familyMembers.get(i).getPension(); }
	 * 
	 * this.cashTran=cashTran; // System.out.println("hhdCash="+this.cashTran);
	 * //get the household cash transfer by counting all pension that eligible
	 * persons have. }
	 */

	public double getAveFemaleEdu() {
		//
		aveFemaleEdu = 0;
		int memberEdu = 0;
		int j = 0;
		for (int i = 0; i < this.familyMembers.size(); i++) {
			Person p = this.familyMembers.get(i);
			if (p.getGenderAge() > 18) // only get the average female adult
										// education
			{
				j = j + 1;
				memberEdu = p.getEducation() + memberEdu;
			}
		}

		if (this.getLinkedHouseholds().size() > 0) {
			List<NetworkedUrbanAgent> checkHus = new LinkedList<NetworkedUrbanAgent>();
			checkHus.addAll(this.getLinkedHouseholds().keySet());
			Iterator<NetworkedUrbanAgent> recallIter = checkHus.iterator();

			while (recallIter.hasNext()) {
				Person p = recallIter.next().getPerson();
				if (p.getGenderAge() > 18) {
					j = j + 1;
					memberEdu = p.getEducation() + memberEdu;
				}
			}
		}
		if (j == 0) {
			aveFemaleEdu = 0;
		}
		// just in case if there's no female member;
		else {
			aveFemaleEdu = (double) memberEdu / j;
		}
		// System.out.println("totalEdu="+totalEdu);
		this.aveFemaleEdu = aveFemaleEdu;
		return aveFemaleEdu;
		// calculate average female education level; Yue, Oct 27, 2014.
	}

	public FastTable<Person> getFamilyMembers() {
		return familyMembers;
	}

	public Map<GridDimensions, MyLandCell> getTenure() {
		return tenure;
	}

	protected final void takePossession(GridValueLayer tenureField, LandCell c) {
		tenure.put(c.getDimensions(), new MyLandCell(c, this));

		if (tenureField != null)
			tenureField.set(getID(), c.getIntArray());

		c.setLandHolder(this);
	}

	private LandCell canTakePossession(GridValueLayer tenureField,
			Grid<LandscapeCell> landscapeGrid, int x, int y) {
		try {
			LandscapeCell cell = landscapeGrid.getObjectAt(x, y);
			if (cell instanceof LandCell) {
				LandCell landCell = (LandCell) cell;
				if (landCell.getLandHolder() == null) {
					return landCell;
				}
			}
		} catch (SpatialException e) {
		}
		return null;
	}

	private LandCell canTakePossession(GridValueLayer tenureField,
			Grid<LandscapeCell> landscapeGrid, Point p) {
		return canTakePossession(tenureField, landscapeGrid, p.x, p.y);
	}

	private boolean hasNeighbour(Grid<LandscapeCell> landscapeGrid, int x, int y) {
		// check if it's adjacent to one of my cells
		boolean isAdjacent = false;
		XYGenerator generator = new XYGenerator(1);
		while (generator.hasNext() && !isAdjacent) {
			Point neighbourPt = generator.next().plus(x, y);
			try {
				LandCell neighbourCell = (LandCell) landscapeGrid.getObjectAt(
						neighbourPt.x, neighbourPt.y);
				isAdjacent = neighbourCell.getLandHolder() == this;
			} catch (SpatialException e) {
			} catch (ClassCastException e) {
			} catch (NullPointerException e) {
			}

			if (isAdjacent)
				return true;
		}

		return isAdjacent;
	}

	protected void takePossession(GridValueLayer tenureField,
			Grid<LandscapeCell> landscapeGrid, int cells, int... coordinates) {
		Queue<Point> nonneighbourRejects = new LinkedList<Point>();

		XYGenerator generator = new XYGenerator();
		Point origin = new Point(coordinates[0], coordinates[1]);
		int attempts = 0;
		int numCells = 0;
		while (numCells < cells && attempts < cells * 2) {
			if (attempts % 10 == 9) {
				// test rejects
				Iterator<Point> iter = nonneighbourRejects.iterator();
				while (iter.hasNext() && numCells < cells) {
					Point p = iter.next();
					LandCell landCell = canTakePossession(tenureField,
							landscapeGrid, p);
					if (p != null) {
						if (numCells == 0
								|| hasNeighbour(landscapeGrid, p.x, p.y)) {
							takePossession(tenureField, landCell);
							numCells++;
							iter.remove();
						}
					}
				}

				if (numCells >= cells)
					return;
			}

			Point p = generator.next().plus(origin);
			LandCell landCell = canTakePossession(tenureField, landscapeGrid, p);
			if (landCell != null) {
				if (numCells == 0 || hasNeighbour(landscapeGrid, p.x, p.y)) {
					takePossession(tenureField, landCell);
					numCells++;
				} else {
					// queue point for future consideration
					nonneighbourRejects.add(p);
				}
			}
			attempts++;
		}
	}

	protected void takePossession(GridValueLayer tenureField,
			Grid<LandscapeCell> landscapeGrid, double hectares,
			double cellsize, int... coordinates) {
		takePossession(tenureField, landscapeGrid, (int) Math.round(hectares
				* 10000d / (cellsize * cellsize)), coordinates);
				
		int maniocnum=0;
		for (MyLandCell c: tenure.values()){
			c.setLandUse(LandUse.MANIOCGARDEN);
				maniocnum++;
				if(maniocnum>1) break;
				}
		int acainum=0;
		for (MyLandCell c: tenure.values()){
			if (c.getLandUse()!=LandUse.MANIOCGARDEN) 
				{ c.setLandUse(LandUse.ACAI);
			      acainum++;
			      if (acainum>1) break;}
		
		}
		
			
			
	}

	protected final void takePossession(LandCell c) {
		takePossession(null, c);
	}

	public Color getColor() {
		return color;
	}

	public double getAcaiYield() {
		return acaiYield;
	}

	public double getManiocYield() {
		return maniocYield;
	}

	public double getTimberYield() {
		return timberYield;
	}

	public double getAlpha() {
		return alpha;
	}

	/*
	 * public double getBeta(){ return beta; }
	 */
	protected void processJobOffer(JobOffer o) {
		// add it to knowledge base
		jobOffers.add(o);
	}

	public FastMap<NetworkedUrbanAgent, RepastEdge<SimpleAgent>> getLinkedHouseholds() {
		return linkedHouseholds;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HouseholdAgent other = (HouseholdAgent) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
	public double getMonetarySubsistenceRequirement() {
		double monetarySubsistenceRequirement = 0;
		for (int i = 0; i < this.familyMembers.size(); i++) {
			Person p = this.familyMembers.get(i);
			monetarySubsistenceRequirement += p.getSubsistenceUnit();
		}
		return monetarySubsistenceRequirement;
	}

	public void setTotalSubsistenceRequirement(double subsistence){
		this.subsistenceRequirement=subsistence;
	}
	public double getTotalSubsistenceRequirement(){
		return this.subsistenceRequirement;
	}
	
    public double getAnnualIncome() {
		return annualIncome;
	}

	public void setAnnualIncome(double annualIncome) {
		this.annualIncome = annualIncome;
		//the definition of annualIncome is raw monetary income,
		//for optimal profit and leisure hhd, it has not deduct subsistence
		//for subsistence hhd, it has deduct crop subsistence 
		//so to get net income, you should use
		// annualIncome - subReq
	}
	
	public void setUpland (double distanceToWater) {
		if (distanceToWater > 400){
			this.upLand=true;
		//	System.out.println("this is upland");
		}
	}
	
    public boolean isUpLand() {
		return upLand;
	}
    
    public void recall (NetworkedUrbanAgent a, String stage){
    	RepastEdge<SimpleAgent> edge = linkedHouseholds.remove(a);
    	Database.getInstance().logRecalledUrbanAgent(conn, a, stage);
    	familyMembers.add(a.getPerson());
    	linkedHouseholds.remove(a);
    	a.getEmployer().remove(a);
    	a.setWage(0);
    	this.setWage(0);
		a.setEmployer(null);
		a.setLocation(null);
    	a.removeNetworkedHousehold(this);
    	
    }
}
 