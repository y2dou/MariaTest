package mariaprototype.human;

import java.awt.Color;
import java.sql.Connection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import javolution.util.FastMap;
import javolution.util.FastTable;
import mariaprototype.MariaPriorities;
import mariaprototype.Point;
import mariaprototype.SimpleAgent;
import mariaprototype.environmental.House;
import mariaprototype.environmental.LandCell;
import mariaprototype.environmental.LandscapeCell;
import mariaprototype.human.messaging.EnvelopePool;
import mariaprototype.human.messaging.MessageEnvelope;
import mariaprototype.human.messaging.NetworkAgent;
import mariaprototype.utility.XYGenerator;
import repast.simphony.annotate.AgentAnnot;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.environment.RunState;
import repast.simphony.engine.schedule.ScheduledMethod;
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
 * 	<li>Deterministic agent
 * 	<ul>
 * 		<li>Rule-based agent</li>
 * 		<li>Fuzzy (JESS) rule-based agent</li>
 * 	</ul>
 * 	</li>
 * 	<li>Adaptive agent
 * 	<ul>
 * 		<li>Learning fuzzy (JESS) rule-based agent</li>
 * 		<li>Simulated annealing (based on age)</li>
 * 		<li>Simulated annealing w/ individual voting (based on age)</li>
 * 	</ul>
 * 	</li>
 * 	<li>Imitative agent
 * 	<ul>
 * 		<li>Nearest neighbour</li>
 * 		<li>...other examples from FEARLUS</li>
 * 	</ul>
 * 	</li>
 * 	<li>Heuristic agent</li>
 * </ul>
 * 
 * @author arcabrer
 *
 */
@AgentAnnot(displayName = "HouseholdAgentAnnot")
public abstract class HouseholdAgent extends SimpleAgent implements NetworkAgent {
	private static final AtomicInteger idGenerator = new AtomicInteger(0);
	private int id;
	private Color color;
	
	protected Context<?> context;
	protected HouseholdAgentState lastState;
	
	protected Point location;
	
	protected double capital;
	protected double labour;
	protected double subsistenceRequirements;
	protected double cashTran ;
	//add cash transfer to hhd agent, June 17, 2014;

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
		labourMultiplier = (Double) RunEnvironment.getInstance().getParameters().getValue("labourMultiplier");
		capitalMultiplier = (Double) RunEnvironment.getInstance().getParameters().getValue("capitalMultiplier");
		forestFallowLabour = (Double) RunEnvironment.getInstance().getParameters().getValue("forestFallowLabour");
		forestFallowCost = (Double) RunEnvironment.getInstance().getParameters().getValue("forestFallowCost");
		fallowLabour = (Double) RunEnvironment.getInstance().getParameters().getValue("fallowLabour");
		fallowCost = (Double) RunEnvironment.getInstance().getParameters().getValue("fallowCost");
		acaiLabour = (Double) RunEnvironment.getInstance().getParameters().getValue("acaiLabour");
		acaiCost = (Double) RunEnvironment.getInstance().getParameters().getValue("acaiCost");
		maniocLabour = (Double) RunEnvironment.getInstance().getParameters().getValue("maniocLabour");
		maniocCost = (Double) RunEnvironment.getInstance().getParameters().getValue("maniocCost");
		maintainAcaiLabour = (Double) RunEnvironment.getInstance().getParameters().getValue("maintainAcaiLabour");
		maintainAcaiCost = (Double) RunEnvironment.getInstance().getParameters().getValue("maintainAcaiCost");
		maintainManiocLabour = (Double) RunEnvironment.getInstance().getParameters().getValue("maintainManiocLabour");
		maintainManiocCost = (Double) RunEnvironment.getInstance().getParameters().getValue("maintainManiocCost");
		harvestAcaiLabour = (Double) RunEnvironment.getInstance().getParameters().getValue("harvestAcaiLabour");
		harvestManiocLabour = (Double) RunEnvironment.getInstance().getParameters().getValue("harvestManiocLabour");
		harvestTimberLabour = (Double) RunEnvironment.getInstance().getParameters().getValue("harvestTimberLabour");
		//cashTran = (Double) RunEnvironment.getInstance().getParameters().getValue("cashTransfer");
			
		color = new Color((id * 1291 + 1297) % 256, (id * 2267 + 337) % 256, (id * 1553 + 3) % 256);
		jobOffers = new LinkedList<JobOffer>();
		
		conn = (Connection) RunState.getInstance().getFromRegistry("connection");
	}
	
	@Override
	public final int getID() {
		return id;
	}
	
	public Point getLocation() {
		return location;
	}
	
	public final void add(Person person) {
		familyMembers.add(person);
	}
	
	protected final void resetLabour() {
		labour = 0;
		for (Person p : familyMembers) {
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
	public abstract void messagePassing1();	// send initial messages: send current state
	
	@ScheduledMethod(start = 1, interval = 1, priority = MariaPriorities.PLANNING)
	public abstract void plan();			// process messages? plan out current+future
	
	@ScheduledMethod(start = 1, interval = 1, priority = MariaPriorities.MESSAGE_PASSING_2)
	public abstract void messagePassing2();	// send messages
	
	@ScheduledMethod(start = 1, interval = 1, priority = MariaPriorities.INTERMEDIATE)
	public abstract void intermediate();	// post-economic adjustments to plan
	
	@ScheduledMethod(start = 1, interval = 1, priority = MariaPriorities.MESSAGE_PASSING_3)
	public abstract void messagePassing3();	// send messages
	
	/**
	 * Implements *land use* change and allocation.
	 */
	@ScheduledMethod(start = 1, interval = 1, priority = MariaPriorities.ACTION)
	public abstract void act();				// implement current

	@ScheduledMethod(start = 1, interval = 1, priority = MariaPriorities.MESSAGE_PASSING_4)
	public abstract void messagePassing4();	// send messages
	
	@ScheduledMethod(start = 1, interval = 1, priority = MariaPriorities.HARVEST)
	public abstract void harvest();	// send messages
	
	@ScheduledMethod(start = 1, interval = 1, priority = MariaPriorities.RETROSPECT)
	public abstract void retrospect();	// send messages
	
	
	@Override
	public void store(MessageEnvelope message) {}
	
	@SuppressWarnings("unchecked")
	protected final void broadcastState() {
		EnvelopePool pool = (EnvelopePool) context.getObjects(EnvelopePool.class).get(0);
		
		lastState.update(this);
		MessageEnvelope messageEnvelope = pool.getBroadcastEnvelope(this, lastState);
		
		// send lastState
		Network<HouseholdAgent> network = (Network<HouseholdAgent>) context.getProjection("Multisited Household Network");
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
	
	public final double getCapital() {
		this.setCashTran();
		capital = capital + this.getCashTran();
		return capital;
		
	}
	
	public void setCapital(double capital) {
		//setCapital only happens at the initialization stage; 
		//it's not called every stage;
		
		this.capital = capital+this.getCashTran();
		
		//i don't want to add a new variable called totalCapital, there will be too many revision; 
		//This way can include cash Transfer into capital;
		//Yue
	}
	
	public double getLabour() {
		return labour;
	}
	
	public double getCashTran() {
		
		return cashTran;
	}

	public void setCashTran( ) {
		cashTran=0;
		int n=this.familyMembers.size();
	//	System.out.println("n="+n);
		for (int i=0;i<n;i++){
			cashTran+=this.familyMembers.get(i).getPension();
		}
		
			this.cashTran=cashTran;
	//		System.out.println("hhdCash="+this.cashTran);
			//get the household cash transfer by counting all pension that eligible persons have.
	}
	
	
	public FastTable<Person> getFamilyMembers() {
		return familyMembers;
	}
	
	public final double getSubsistenceRequirements() {
		return subsistenceRequirements;
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
	
	private LandCell canTakePossession(GridValueLayer tenureField, Grid<LandscapeCell> landscapeGrid, int x, int y) {
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
	
	private LandCell canTakePossession(GridValueLayer tenureField, Grid<LandscapeCell> landscapeGrid, Point p) {
		return canTakePossession(tenureField, landscapeGrid, p.x, p.y);
	}
	
	private boolean hasNeighbour(Grid<LandscapeCell> landscapeGrid, int x, int y) {
		// check if it's adjacent to one of my cells
		boolean isAdjacent = false;
		XYGenerator generator = new XYGenerator(1);
		while (generator.hasNext() && !isAdjacent) {
			Point neighbourPt = generator.next().plus(x, y);
			try {
				LandCell neighbourCell = (LandCell) landscapeGrid.getObjectAt(neighbourPt.x, neighbourPt.y);
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
	
	protected void takePossession(GridValueLayer tenureField, Grid<LandscapeCell> landscapeGrid, int cells, int... coordinates) {
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
					LandCell landCell = canTakePossession(tenureField, landscapeGrid, p);
					if (p != null) {
						if (numCells == 0 || hasNeighbour(landscapeGrid, p.x, p.y)) {
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
	
	protected void takePossession(GridValueLayer tenureField, Grid<LandscapeCell> landscapeGrid, double hectares, double cellsize, int... coordinates) {
		takePossession(tenureField, landscapeGrid, (int) Math.round(hectares * 10000d / (cellsize * cellsize)), coordinates);
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
}
