package mariaprototype.human;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javolution.util.FastMap;
import mariaprototype.SimpleAgent;
import mariaprototype.database.Database;
import mariaprototype.environmental.EnvironmentalContext;
import mariaprototype.environmental.LandCell;
import mariaprototype.environmental.LandUse;
import mariaprototype.environmental.LandscapeCell;
import mariaprototype.human.messaging.MarketPrices;
import mariaprototype.human.messaging.Message;
import mariaprototype.human.messaging.NetworkAgent;
import mariaprototype.human.messaging.Message.MessageType;
import repast.simphony.context.Context;
import repast.simphony.essentials.RepastEssentials;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridDimensions;
import repast.simphony.valueLayer.GridValueLayer;

public abstract class SimpleHouseholdAgent extends HouseholdAgent {
	protected FastMap<NetworkAgent, Message> mailbox = new FastMap<NetworkAgent, Message>();

	protected Map<LandUse, Double> marketPrices = new FastMap<LandUse, Double>();
	
	public SimpleHouseholdAgent() {
		super();
	}
	
	public SimpleHouseholdAgent(int id) {
		super(id);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void init(HumanContext humanContext, int... coordinates) {
		super.init(humanContext, coordinates);
		
		// select 10 or so plots to start with
		context = humanContext;
		EnvironmentalContext environmentalContext = ((HumanContext) humanContext).getEnvironmentalContext();
		Grid<LandscapeCell> landscapeGrid = (Grid<LandscapeCell>) environmentalContext.getProjection("Landscape Grid");
		
		GridValueLayer tenureField = (GridValueLayer) context.getValueLayer("Land Holder Field");
		
	//	takePossession(tenureField, landscapeGrid, RandomHelper.getDistribution("hectares").nextDouble(), environmentalContext.getCellsize(), coordinates);
		takePossession(tenureField, landscapeGrid, 0.5d, environmentalContext.getCellsize(), coordinates);
	}
	
	protected void processMessages() {
		for (Map.Entry<NetworkAgent, Message> e : mailbox.entrySet()) {
			AgentType sourceType = e.getKey().getAgentType();
			Message message = e.getValue();
			if (sourceType.equals(AgentType.HOUSEHOLD_AGENT)) {
				if (message.getMessageType().equals(MessageType.HOUSEHOLD_STATE)) {
					// I shouldn't be multi-sited. I don't care.
					processHouseholdMessage(e);
				}
			} else if (sourceType.equals(AgentType.POLICY_AGENT)) {
				if (message.getMessageType().equals(MessageType.POLICY)) {
					// a government policy
					// read through it, see if conditions are met
				}
			} else if (sourceType.equals(AgentType.MIDDLEMAN_AGENT)) {
				if (message.getMessageType().equals(MessageType.MIDDLEMAN_OFFER)) {
					// a middleman offer

					// mull over the commission, considering transport costs
				}
			} else if (sourceType.equals(AgentType.MARKET_AGENT)) {
				if (message.getMessageType().equals(MessageType.MARKET_PRICES)) {
					// update own knowledge of prices
					
					MarketPrices m = (MarketPrices) message;
					Iterator<Entry<LandUse, Double>> prices = m.iterator();
					while (prices.hasNext()) {
						Entry<LandUse, Double> f = prices.next();
						marketPrices.put(f.getKey(), f.getValue());
					}
							
					// adapt
				}
			} else if (sourceType.equals(AgentType.TOWN)) {
				if (message.getMessageType().equals(MessageType.JOB_OFFER)) {
					processJobOffer((JobOffer) e.getValue());
			//		System.out.println(e.getValue());
				}
			}
		}
		mailbox.clear();
	}
	
	protected void processHouseholdMessage(Map.Entry<NetworkAgent, Message> e) {
	}
	
	protected boolean forestFallow(MyLandCell c) {
		final double requiredLabour = forestFallowLabour * labourMultiplier;
		final double requiredCapital = forestFallowCost * capitalMultiplier;
		
		if (labour > requiredLabour && capital > requiredCapital) {
			LandCell cell = c.getCell();
			
			c.setLandUse(LandUse.FALLOW);
			c.maintain();
			
			// leave acai as is
			cell.setCapoeiraDensity(1 - cell.getAcaiDensity()); // forest fallow everything but acai
			cell.setFieldsDensity(0);
			cell.setForestDensity(0);
			cell.setManiocGardenDensity(0);
			cell.setSecondarySuccessionDensity(0);
			
			labour -= requiredLabour;
			capital -= requiredCapital;
			
			c.setToDevelop(false);
			
			return true;
		}
		
		return false;			
	}
	
	protected boolean fallow(MyLandCell c) {
		final double requiredLabour = fallowLabour * labourMultiplier;
		final double requiredCapital = fallowCost * capitalMultiplier;
		
		if (labour > requiredLabour && capital > requiredCapital) {
			LandCell cell = c.getCell();
		
			c.setLandUse(LandUse.FALLOW);
			c.maintain();
			
			cell.setAcaiDensity(0);
			cell.setFieldsDensity(0);
			cell.setForestDensity(0);
			cell.setManiocGardenDensity(0);
			cell.setSecondarySuccessionDensity(1);
			
			labour -= requiredLabour;
			capital -= requiredCapital;
			
			c.setToDevelop(false);
			
			return true;
		}
		
		return false;
	}
	
	protected boolean intensifyAcai(MyLandCell c) {
		final double requiredLabour = acaiLabour * labourMultiplier;
		final double requiredCapital = acaiCost * capitalMultiplier;	// selective pruning, etc.
		
		if (labour > requiredLabour && capital > requiredCapital) {
			LandCell cell = c.getCell();
			
			c.setLandUse(LandUse.ACAI);
			c.maintain();
			
			cell.setAcaiDensity(1);
			// cell.setAcaiIntensity(Math.min(cell.getAcaiIntensity() + 0.4, 1d));
			cell.setFieldsDensity(0);
			cell.setForestDensity(0);
			cell.setManiocGardenDensity(0);
			cell.setSecondarySuccessionDensity(0);
			
			labour -= requiredLabour;
			capital -= requiredCapital;
			
			c.setToDevelop(false);
			
			return true;
		}
		
		return false;
	}
	
	protected boolean expandManiocGarden(MyLandCell c) {
		final double requiredLabour = maniocLabour * labourMultiplier;
		final double requiredCapital = maniocCost * capitalMultiplier;
		
		if (labour > requiredLabour && capital > requiredCapital) {
			LandCell cell = c.getCell();
			
			c.setLandUse(LandUse.MANIOCGARDEN);
			c.maintain();
			
			cell.setAcaiDensity(0);
			cell.setFieldsDensity(0);
			cell.setForestDensity(0);
			cell.setManiocGardenDensity(1);
			cell.setSecondarySuccessionDensity(0);
			
			labour -= requiredLabour;
			capital -= requiredCapital;
			
			c.setToDevelop(false);
			
			return true;
		}
		
		return false;
	}
	
	protected boolean maintain(MyLandCell c) {
		double requiredLabour = 0;	// person-months
		double requiredCapital = 0; // 100 = price of acai in 1992
		
		switch(c.getLandUse()) {
		case ACAI:
			requiredLabour = maintainAcaiLabour * labourMultiplier;
			requiredCapital = maintainAcaiCost * capitalMultiplier;
			break;
		case MANIOCGARDEN:
			requiredLabour = maintainManiocLabour * labourMultiplier;
			requiredCapital = maintainManiocCost * capitalMultiplier;
			break;
		case FALLOW:
		case FOREST:
			break;
		case FIELDS:
			break;
		}
		
		if (labour >= requiredLabour - 0.001 && capital >= requiredCapital) {
			c.maintain();
			labour -= requiredLabour;
			capital -= requiredCapital;
			return true;
		}
		return false;
	}
	
	protected void countNeighbours() {
		countNeighbours(tenure, Neighbourhood.MOORE);
	}
	
	protected void countNeighbours(Map<GridDimensions, MyLandCell> subset, Neighbourhood neighbourhood) {
		int[] dim = new int[2];
		GridDimensions neighbourDim = new GridDimensions(dim, new int[2]);
		
		int[] x = neighbourhood.getX();
		int[] y = neighbourhood.getY();
		double[] weights = neighbourhood.getWeights();
		for (MyLandCell c : subset.values()) {
			c.clearNeighbourLandUses();
			for (int i = 0; i < x.length; i+=1) {
				dim[0] = x[i] + c.getCell().getX();
				dim[1] = y[i] + c.getCell().getY();
				
				try {
					LandUse neighbourLandUse = tenure.get(neighbourDim).getLandUse();
					c.addNeighbourLandUse(neighbourLandUse, weights[i]);
				} catch (NullPointerException e) {
					// unowned plots will result in a null pointer. this is normal.
				}
			}
		}
	}
	

	
	@SuppressWarnings("unchecked")
	protected void send(Person p, JobOffer o) {
		Town t = o.getTown();
	//    System.out.println(t.broadcastJobs());
		NetworkedUrbanAgent a = new NetworkedUrbanAgent(p);
		a.setWage(o.getWage());
		a.setColor(this.getColor());
		familyMembers.remove(p);
	//	System.out.println("family send");
		t.add(a);
		a.setEmployer(t);
		a.addNetworkedHousehold(this);
		
		((Context<SimpleAgent>) context).add(a);
		Geography<SimpleAgent> geography = (Geography<SimpleAgent>) context.getProjection("Person Geography");
		geography.move(a, a.getLocation());
		Network<SimpleAgent> network = (Network<SimpleAgent>) context.getProjection("Multisited Household Network");
		RepastEdge<SimpleAgent> edge = new RepastEdge<SimpleAgent>(this, a, true, 1);
		network.addEdge(edge);
		
		linkedHouseholds.put(a, edge);
		
		Database.getInstance().logNewUrbanAgent(conn, a, this);
	}
	
	@SuppressWarnings("unchecked")
	protected void recall(NetworkedUrbanAgent a, String stage) {
		RepastEdge<SimpleAgent> edge = linkedHouseholds.remove(a);
		
		Database.getInstance().logRecalledUrbanAgent(conn, a, stage);
		
		familyMembers.add(a.getPerson());
		capital += a.getCapital();
	//	System.out.println("simpleHHD="+this.getID()+" recall capital="+a.getCapital());
		setWage(this.getWage()+a.getPerson().getWage()/2);
		a.getEmployer().remove(a);
		a.setEmployer(null);
		a.setLocation(null);
		a.removeNetworkedHousehold(this);
		
		Network<SimpleAgent> network = (Network<SimpleAgent>) context.getProjection("Multisited Household Network");
		network.removeEdge(edge);
		
		/*
		 * breaks the GIS3D visualization
		Geography<SimpleAgent> geography = (Geography<SimpleAgent>) context.getProjection("Person Geography");
		geography.move(a, null);
		*/
		
		/*
		 * doesn't get removed from context properly anyway...
		try {
			((Context<SimpleAgent>) context).remove(a);
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		*/
		
		try {
			RepastEssentials.RemoveAgentFromModel(a);
		} catch(Exception e) {
			/* Repast doesn't let us throw out this node.
			 * All other references are removed, so other 
			 * than a small memory leak that gets cleared 
			 * at the end of the run, that's ok. 
			 */
		}
		
	}
	
	
}
