package mariaprototype.human;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;

import javolution.util.FastTable;
import mariaprototype.FuzzyUtility;
import mariaprototype.WeightedSelector;
import mariaprototype.environmental.LandCell;
import mariaprototype.environmental.LandUse;
import mariaprototype.human.landallocation.AcaiGoodnessComparator;
import mariaprototype.human.landallocation.HousegardenGoodnessComparator;
import mariaprototype.human.messaging.Message;
import mariaprototype.human.messaging.MessageEnvelope;
import mariaprototype.human.messaging.NetworkAgent;
import repast.simphony.random.RandomHelper;


/**
 * A <code>HouseholdAgent</code> based on heuristics provided by Miguel and Eduardo.
 * 
 * @author Raymond Cabrera
 *
 */
public class HeuristicHouseholdAgent extends SimpleHouseholdAgent {	
	private WeightedSelector<LandUse> landUseSelector;
	
	// plan
	private FastTable<MyLandCell> toForestFallow = new FastTable<MyLandCell>();	// de-intensify acai
	private FastTable<MyLandCell> toFallow = new FastTable<MyLandCell>();
	
	// order potential expansion actions by best allocation: k-NN for now
	private PriorityQueue<MyLandCell> toIntensifyAcai = new PriorityQueue<MyLandCell>(11, ACAI_COUNT_COMPARATOR);
	private PriorityQueue<MyLandCell> toManiocGarden = new PriorityQueue<MyLandCell>(11, MANIOCGARDEN_COUNT_COMPARATOR);
	private FastTable<MyLandCell> toFields = new FastTable<MyLandCell>();
	private FastTable<MyLandCell> toHarvestAcai = new FastTable<MyLandCell>();
	private FastTable<MyLandCell> toHarvestIntenseAcai = new FastTable<MyLandCell>();
	private FastTable<MyLandCell> toHarvestHousegarden = new FastTable<MyLandCell>();
	private FastTable<MyLandCell> toHarvestCattle = new FastTable<MyLandCell>();
	private FastTable<MyLandCell> toHarvestTimber = new FastTable<MyLandCell>();
	private FastTable<MyLandCell> toMaintain = new FastTable<MyLandCell>();
	
	private FastTable<MyLandCell> toPossiblyDevelop = new FastTable<MyLandCell>();
	
	// retrospective variables
	private double lastCapital;
	private double profitPerLabour = Double.MAX_VALUE;
	
	public HeuristicHouseholdAgent() {
		super();
	}
	
	public HeuristicHouseholdAgent(int id) {
		super(id);
	}
	
	@Override
	public void init(HumanContext humanContext, int... coordinates) {
		super.init(humanContext, coordinates);
		
		RandomHelper.registerGenerator("householdLandUseSelector" + Integer.toString(getID()), RandomHelper.nextInt());
		landUseSelector = new WeightedSelector<LandUse>("householdLandUseSelector" + Integer.toString(getID()));
		
		lastCapital = capital;
		
		// init labour and capital
		resetLabour();
	}
	
	@Override
	public void messagePassing1() {}
	@Override
	public void messagePassing2() {}
	@Override
	public void messagePassing3() {}
	@Override
	public void messagePassing4() {}

	protected void processHouseholdMessage(Map.Entry<NetworkAgent, Message> e) {}
	
	@Override
	public void plan() {
		processMessages();
		
		// collect stats over managed property
		// create plan
		// TODO: project labour and capital costs (harvest and LUCC are separate)
		
		resetActions();
		
		// consider job offers, move people to town if desired
		if (!jobOffers.isEmpty()) {
			// select person who can work in town
			ArrayList<Person> eligibleMembers = new ArrayList<Person>();
			ListIterator<Person> members = familyMembers.listIterator();
			
			int males = 0; // adults
			int females = 0;
			while (members.hasNext()) {
				Person p = members.next();
				if (p.getAge() >= 70) {
					if (p.isFemale())
						females++;
					else
						males++;
				} else if (p.getAge() >= 16) {
					eligibleMembers.add(p);
					if (p.isFemale())
						females++;
					else
						males++;
				} else {
					
				}
			}
			
			if (!eligibleMembers.isEmpty()) {
				if (males <= 1) {
					ListIterator<Person> iter = eligibleMembers.listIterator();
					if (!iter.next().isFemale()) {
						iter.remove();
					}
				}
			}
			
			if (!eligibleMembers.isEmpty()) {
				if (females <= 1) {
					ListIterator<Person> iter = eligibleMembers.listIterator();
					if (iter.next().isFemale()) {
						iter.remove();
					}
				}
			}
			
			while (!jobOffers.isEmpty() && !eligibleMembers.isEmpty()) {
				JobOffer o = jobOffers.remove(0);
				
				Person p = eligibleMembers.get(RandomHelper.nextIntFromTo(0, eligibleMembers.size() - 1));
				if (o.getWage() / p.getLabour() > profitPerLabour) {
					send(p, o);
					eligibleMembers.remove(p);
				}
			}
		}
		
		// iterate through portfolio: identify actions
		for (MyLandCell c : tenure.values()) {
			c.age();
			
			LandCell cell = c.getCell();
			if (c.getLandUse() == LandUse.ACAI) {
				if (c.getYearsSinceMaintained() >= 3) {
					c.setLandUse(LandUse.FOREST);
					c.setYearsSinceLast(c.getYearsSinceMaintained());
					c.setYearsSinceMaintained(0);
					cell.setForestDensity(FuzzyUtility.constrain(cell.getForestDensity() + cell.getAcaiDensity()));
					cell.setAcaiDensity(0);
					toHarvestAcai.add(c);
				} else {
					toMaintain.add(c);
					toHarvestIntenseAcai.add(c);
				}
			} else if (c.getLandUse() == LandUse.MANIOCGARDEN) {
				if (cell.getManiocGardenAge() > 3) {
					toForestFallow.add(c);
				} else {
					toHarvestHousegarden.add(c);
					
					if (cell.getAcaiDensity() > 0)
						toHarvestIntenseAcai.add(c);
					
					toMaintain.add(c);
				}
			} else if (c.getLandUse() == LandUse.FOREST) {
				if (cell.getForestAge() > 3) {
					toHarvestTimber.add(c);
					toHarvestAcai.add(c);
				}
				
				if (c.getYearsSinceLast() > 5) {
					toPossiblyDevelop.add(c);
				}
			} else if (c.getLandUse() == LandUse.FALLOW) {
				final LandUse lastLandUse = c.getLastLandUse();
				switch(lastLandUse) {
					case ACAI:
						toHarvestAcai.add(c);
						if (c.getYearsSinceLast() > 5)
							toPossiblyDevelop.add(c);
						break;
						
					case MANIOCGARDEN:
						if (c.getYearsSinceLast() > 5)
							toPossiblyDevelop.add(c);
						break;
						
					case FALLOW:
					case FOREST:
						toPossiblyDevelop.add(c);
						break;
						
					case FIELDS:
						if (c.getYearsSinceLast() > 10)
							toPossiblyDevelop.add(c);
						break;
				}
			}
		}
		
		// TODO: order maintenance activities by profit? de-value maintenance activities which result in loss
		
		// calculate metrics for land use allocation algorithm
		countNeighbours();
		
		// order land uses by some goodness heuristic
		for (MyLandCell c : toPossiblyDevelop) {
			c.setToDevelop(true);
			toIntensifyAcai.add(c);
			toManiocGarden.add(c);
		}
	}
	
	private void resetActions() {
		// maintenance
		toMaintain.clear();
		
		// harvest actions
		toHarvestAcai.clear();
		toHarvestIntenseAcai.clear();
		toHarvestTimber.clear();
		toHarvestHousegarden.clear();
		toHarvestCattle.clear();
		
		// possibilities
		toPossiblyDevelop.clear();
		
		// land use change
		toForestFallow.clear();
		toFallow.clear();
		toManiocGarden.clear();
		toFields.clear();
		toIntensifyAcai.clear();
	}
	
	private void setupPriceWeightedLandUseSelector() {
		throw new UnsupportedOperationException("Price-weighted land use selector is unimplemented.");
		// not implemented since revenue per plot is considered, rather than price per kg
	}
	
	private void setupRevenueWeightedLandUseSelector() {
		// heuristic: prioritize remaining land use selections based on revenue
		landUseSelector.reset();
		if (marketPrices != null) {
			// select with weighted probability
			
			for (Map.Entry<LandUse, Double> e : marketPrices.entrySet()) {
				landUseSelector.add(e.getKey(), e.getValue());
			}
			
			double acaiYield = 0;
			if (!toHarvestAcai.isEmpty()) {
				for (MyLandCell c : toHarvestAcai) {
					acaiYield += c.getCell().getAcaiYield();
				}
				acaiYield /= (double) toHarvestAcai.size();	
			}
			
			double intenseacaiYield = 0;
			if (!toHarvestIntenseAcai.isEmpty()) {
				for (MyLandCell c : toHarvestIntenseAcai) {
					intenseacaiYield += c.getCell().getIntenseAcaiYield();
				}
				intenseacaiYield /= (double) toHarvestIntenseAcai.size();
			}
			
			double gardenYield = 0;
			if (!toHarvestHousegarden.isEmpty()) {
				for (MyLandCell c : toHarvestHousegarden) {
					gardenYield += c.getCell().getGardenYield();
				}
				gardenYield /= (double) toHarvestHousegarden.size();
			}
			
			double timberYield = 0;
			if (!toHarvestTimber.isEmpty()) {
				for (MyLandCell c : toHarvestTimber) {
					timberYield += c.getCell().getTimberYield();
				}
				timberYield /= (double) toHarvestTimber.size();
			}
			
			landUseSelector.add(LandUse.ACAI, marketPrices.get(LandUse.ACAI) * intenseacaiYield);
			landUseSelector.add(LandUse.FALLOW, marketPrices.get(LandUse.ACAI) * acaiYield);
			landUseSelector.add(LandUse.MANIOCGARDEN, marketPrices.get(LandUse.MANIOCGARDEN) * gardenYield);
			// landUseSelector.add(LandUse.FOREST, marketPrices.get(LandUse.FOREST) * timberYield);
		} else {
			// select with uniform probability
			landUseSelector.add(LandUse.ACAI, 1);
			landUseSelector.add(LandUse.MANIOCGARDEN, 1);
		}
	}
	
	private void setupPriceAndLandWeightedLandUseSelector() {
		// heuristic: prioritize remaining land use selections based on price
		landUseSelector.reset();
		
		if (marketPrices != null) {
			// select with weighted probability
			
			// count up land uses
			int acai = 0;
			int manioc = 0;
			int fields = 0;
			int forest = 0;
			int fallow = 0;
			for (MyLandCell c : tenure.values()) {
				switch (c.getLandUse()) {
				case ACAI:
					acai++;
					break;
				case MANIOCGARDEN:
					manioc++;
					break;
				case FIELDS:
					fields++;
					break;
				case FOREST:
					forest++;
					break;
				case FALLOW:
					fallow++;
					break;
				}
			}
			
			double totalSize = tenure.size();
			
			Double totalPrices = marketPrices.get(LandUse.ACAI) + marketPrices.get(LandUse.MANIOCGARDEN) + marketPrices.get(LandUse.FOREST);
			landUseSelector.add(LandUse.ACAI, Math.min(marketPrices.get(LandUse.ACAI) / totalPrices * acai / totalSize, 0.01));
			landUseSelector.add(LandUse.MANIOCGARDEN, Math.min(marketPrices.get(LandUse.MANIOCGARDEN) / totalPrices * manioc / totalSize, 0.01));
			//landUseSelector.add(LandUse.FOREST, Math.min(marketPrices.get(LandUse.FOREST) / totalPrices * forest / totalSize, 0.01));
		} else {
			// select with uniform probability
			landUseSelector.add(LandUse.ACAI, 1);
			landUseSelector.add(LandUse.MANIOCGARDEN, 1);
		}
	}
	
	@Override
	public void intermediate() {}
	
	@Override
	public void act() {
		// make land use changes, maintain old crops
		//		assumption of primary income: acai and annual crops
		//		secondary income: none (would be pensions and in-town labour for networked households)
		
		// send/recall people to/from town
		
		List<NetworkedUrbanAgent> toRecall = new LinkedList<NetworkedUrbanAgent>();
		for (NetworkedUrbanAgent a : linkedHouseholds.keySet()) {
			if (a.getWage() / a.getPerson().getLabour() < profitPerLabour) {
				toRecall.add(a);
			}
		}
		
		for (NetworkedUrbanAgent a : toRecall) {
			recall(a, "action");
		}
		
		resetLabour();
		
		// non-transitional maintenance
		ListIterator<MyLandCell> iter = toMaintain.listIterator();
		while (iter.hasNext()) {
			MyLandCell myCell = iter.next();
			
			if (maintain(myCell)) iter.remove();
		}
		
		// forest fallow: de-intensify acai
		for (MyLandCell c : toForestFallow) {
			if (!forestFallow(c)) break;
		}
		
		// slash and burn
		for (MyLandCell c : toFallow) {
			if (!fallow(c)) break;
		}
		
		// there's a lot missing here
		
		setupRevenueWeightedLandUseSelector();
		while (landUseSelector.size() > 0) {
			LandUse landUse = landUseSelector.sample();
			if (landUse == null)
				break;
			
			switch(landUse) {
			case ACAI:
				if (!intensifyAcai()) landUseSelector.remove(landUse);
				break;
			case MANIOCGARDEN:
				if (!expandHousegarden()) landUseSelector.remove(landUse);
				break;
			default:
				landUseSelector.remove(landUse);
				break;
			}
		}
	}
	
	/**
	 * Intensify acai by a single, unspecified cell. The acai cell will be taken from the <code>toIntensifyAcai</code> list.
	 * 
	 * @return <code>true</code> if successful.
	 */
	protected boolean intensifyAcai() {
		MyLandCell c = null;
		try {
			while (!toIntensifyAcai.isEmpty()) {
				c = toIntensifyAcai.remove();
				if (c.isToDevelop())
					break;
			}
			return c != null && c.isToDevelop() && intensifyAcai(c);
		} catch (NoSuchElementException e) {
			return false;
		}
	}
	
	/**
	 * Expand housegarden by a single cell, the first cell in the <code>toHousegarden</code> list.
	 * 
	 * @return <code>true</code> if successful.
	 */
	protected boolean expandHousegarden() {
		MyLandCell c = null;
		try {
			while (!toManiocGarden.isEmpty()) {
				c = toManiocGarden.remove();
				if (c.isToDevelop())
					break;
			}
			return c != null && c.isToDevelop() && expandManiocGarden(c);
		} catch (NoSuchElementException e) {
			return false;
		}
	}
	
	@Override
	public void harvest() {
		// heuristic: harvest in order of price (approximately 2/3 of land, with a variation of 1/3)
		// alternate heuristic: harvest in order of price / cost of harvest, where cost = labour + capital
		// or, where cost = capital + opportunity cost (marginal cost of labour?)
		resetLabour();
		
		// recall agents if necessary
		List<NetworkedUrbanAgent> toRecall = new LinkedList<NetworkedUrbanAgent>();
		for (NetworkedUrbanAgent a : linkedHouseholds.keySet()) {
			if (a.getWage() / a.getPerson().getLabour() < profitPerLabour) {
				toRecall.add(a);
			}
		}
		
		for (NetworkedUrbanAgent a : toRecall) {
			recall(a, "harvest");
		}
		
		resetLabour();
		resetHarvestCount();
		
		int harvestTotalCells = (int) Math.round(RandomHelper.getDistribution("harvestTotalCells").nextDouble() * (toMaintain.size() + toHarvestTimber.size()));
		setupRevenueWeightedLandUseSelector();
		for (int i = 0; i < harvestTotalCells && landUseSelector.size() > 0 && labour > 0 && capital > 0; i++) {
			LandUse selectedLandUse = landUseSelector.sample();
			switch(selectedLandUse) {
				case ACAI:
					// acai
					if (!toHarvestIntenseAcai.isEmpty()) {
						MyLandCell acaiCell = toHarvestIntenseAcai.remove(0);
						LandCell cell = acaiCell.getCell();

						double yield = cell.getIntenseAcaiYield();
						
						if (labour >= harvestAcaiLabour	* labourMultiplier && capital >= yield * marketPrices.get(LandUse.ACAI))
						{
							labour -= harvestAcaiLabour	* labourMultiplier;
							capital += yield * marketPrices.get(LandUse.ACAI);
							
							acaiYield += yield;
						}
					} else {
						landUseSelector.remove(LandUse.ACAI);
						i--;
					}
					
					break;
				case FALLOW:
					// not really fallow, but non-intense acai
					if (!toHarvestAcai.isEmpty()) {
						MyLandCell acaiCell = toHarvestAcai.remove(0);
						LandCell cell = acaiCell.getCell();

						double yield = cell.getAcaiYield();
						
						if (labour >= harvestAcaiLabour	* labourMultiplier && capital >= yield * marketPrices.get(LandUse.ACAI)) {
							labour -= harvestAcaiLabour	* labourMultiplier;
							capital += yield * marketPrices.get(LandUse.ACAI);
							
							acaiYield += yield;
						}
					} else {
						landUseSelector.remove(LandUse.ACAI);
						i--;
					}
					break;
				case FIELDS:
					// cattle
					landUseSelector.remove(LandUse.FIELDS);
					i--;
					break;
				case FOREST:
					// timber
					
			//		System.out.println("Case Forest: ");
					if (!toHarvestTimber.isEmpty()) {
			//			System.out.println("toHarvestTimber list not empty");
						MyLandCell timberCell = toHarvestTimber.remove(0);
						LandCell cell = timberCell.getCell();
						double yield = cell.getTimberYield();
						
						if (labour >= harvestTimberLabour * labourMultiplier && capital >= yield * marketPrices.get(LandUse.FOREST)) {
							labour -= harvestTimberLabour * labourMultiplier; // labour to clear land
							capital += yield * marketPrices.get(LandUse.FOREST);
	
							// clear land
							cell.setForestDensity(0);
							cell.setCapoeiraDensity(1);
							cell.setAcaiDensity(0);
							cell.setManiocGardenDensity(0);
							cell.setFieldsDensity(0);
							cell.setSecondarySuccessionDensity(0);
							timberCell.setLandUse(LandUse.FALLOW); // is this correct?
							timberYield += yield;
						}
						
						
					} else {
						System.out.println("EMPTY!!!!!!");
						landUseSelector.remove(LandUse.FOREST);
						i--;
					}
					
					// TODO: implement non-timber extraction
					break;
				case MANIOCGARDEN:
					// annuals
					if (!toHarvestHousegarden.isEmpty()) {
						MyLandCell myCell = toHarvestHousegarden.remove(0);
						LandCell cell = myCell.getCell();

						double yield = cell.getGardenYield();
						if (labour >= harvestManiocLabour * labourMultiplier && capital >= yield * marketPrices.get(LandUse.MANIOCGARDEN)) {
							labour -= harvestManiocLabour * labourMultiplier;
							capital += yield * marketPrices.get(LandUse.MANIOCGARDEN);
							
							maniocYield += yield;
						}
					} else {
						landUseSelector.remove(LandUse.MANIOCGARDEN);
						i--;
					}
					break;
				default:
					break;
			}
		}
	}
	
	@Override
	public void retrospect() {
		// calculate income per unit labour
		profitPerLabour = (capital - lastCapital) / labour;
		lastCapital = capital;
		
		resetLabour();
	}
	
	@Override
	public void store(MessageEnvelope message) {
		mailbox.put(message.getSource(), message.getContents());
	}
	
	// land use allocation heuristics
	public static Comparator<MyLandCell> ACAI_COUNT_COMPARATOR = new AcaiGoodnessComparator();
	public static Comparator<MyLandCell> MANIOCGARDEN_COUNT_COMPARATOR = new HousegardenGoodnessComparator();
}
