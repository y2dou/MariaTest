package mariaprototype.human;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.environment.RunState;

import lpsolve.LpSolve;
import lpsolve.LpSolveException;
import mariaprototype.FuzzyUtility;
import mariaprototype.environmental.LandCell;
import mariaprototype.environmental.LandUse;
import mariaprototype.human.messaging.MessageEnvelope;

/**
 * A <code>HouseholdAgent</code> based on heuristics provided by Miguel and
 * Eduardo.
 * 
 * @author Raymond Cabrera
 * 
 */
public class LinearOptimizingHouseholdAgent extends SimpleHouseholdAgent {
	// plan
	protected FeasibleAllocations feasibleAllocations = new FeasibleAllocations();
	
	/*
	// yield estimates for optimization algorithm
	private double expectedAcaiYield = 15000d;
	private double expectedIntenseAcaiYield = 15000;
	private double expectedManiocYield = 15000;
	private double expectedTimberYield = 15000;
	
	private double acaiYieldCounter = 0;
	private double intenseAcaiYieldCounter = 0;
	*/
	
	public LinearOptimizingHouseholdAgent() {
		super();
	}

	public LinearOptimizingHouseholdAgent(int id) {
		super(id);
	}

	@Override
	public void init(HumanContext humanContext,	int... coordinates) {
		super.init(humanContext, coordinates);
		
		// init labour and capital
		resetLabour();
	}

	@Override
	public void messagePassing1() {
	}

	@Override
	public void messagePassing2() {
	}

	@Override
	public void messagePassing3() {
	}

	@Override
	public void messagePassing4() {
	}

	@Override
	public void plan() {
		processMessages();

		resetActions();

		// consider job offers, move people to town if desired
		ArrayList<Person> eligibleMembers = new ArrayList<Person>();
		int males = 0; // adults
		int females = 0;
		if (!jobOffers.isEmpty()) {
			ListIterator<Person> members = familyMembers.listIterator();
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
		}
		
		while (!jobOffers.isEmpty()) {
			JobOffer o = jobOffers.remove(0);
			
			// rank members by age, younger eligible members leave first
			Collections.sort(eligibleMembers, new Comparator<Person>() {
				@Override
				public int compare(Person o1, Person o2) {
					return new Integer(o1.getAge()).compareTo(o2.getAge());
				}
				
				@Override
				public boolean equals(Object obj) {
					return super.equals(obj);
				}
			});
			
			// find a suitable candidate
			while (!eligibleMembers.isEmpty()) {
				Person p = eligibleMembers.remove(0);
				if (p.isFemale()) {
					if (females > 1) {
						feasibleAllocations.getEmployables().put(p, o);
						break;
					}
				} else {
					if (males > 1) {
						feasibleAllocations.getEmployables().put(p, o);
						break;
					}
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
					feasibleAllocations.getToHarvestAcai().add(c);
					System.out.println("Let's try here");
				} else {
					feasibleAllocations.getToMaintainAcai().add(c);
					feasibleAllocations.getToHarvestIntenseAcai().add(c);
				}
			} else if (c.getLandUse() == LandUse.MANIOCGARDEN) {
				if (cell.getManiocGardenAge() > 3) {
					feasibleAllocations.getToForestFallow().add(c);
				} else {
					feasibleAllocations.getToHarvestHousegarden().add(c);

					if (cell.getAcaiDensity() > 0)
						feasibleAllocations.getToHarvestIntenseAcai().add(c);

					feasibleAllocations.getToMaintainManiocGarden().add(c);
				}
			} else if (c.getLandUse() == LandUse.FOREST) {
				if (cell.getForestAge() > 3) {
					feasibleAllocations.getToHarvestTimber().add(c);
					feasibleAllocations.getToHarvestAcai().add(c);
				}

				if (c.getYearsSinceLast() > 5) {
					feasibleAllocations.getToPossiblyDevelop().add(c);
				}
			} else if (c.getLandUse() == LandUse.FALLOW) {
				final LandUse lastLandUse = c.getLastLandUse();
				switch (lastLandUse) {
				case ACAI:
					feasibleAllocations.getToHarvestAcai().add(c);
					if (c.getYearsSinceLast() > 5)
						feasibleAllocations.getToPossiblyDevelop().add(c);
					break;

				case MANIOCGARDEN:
					if (c.getYearsSinceLast() > 5)
						feasibleAllocations.getToPossiblyDevelop().add(c);
					break;

				case FALLOW:
				case FOREST:
				//	feasibleAllocations.getToPossiblyDevelop().add(c);
				//	break;
                // I made this change June 06, 2014
				{  feasibleAllocations.getToPossiblyDevelop().add(c);
				   System.out.println("Linear output: Forest");
				   break;
				}
					
				case FIELDS:
					if (c.getYearsSinceLast() > 10)
						feasibleAllocations.getToPossiblyDevelop().add(c);
					break;
				}
			}
		}

		// calculate metrics for land use allocation algorithm
		countNeighbours();

		// order land uses by some goodness heuristic? (Chebyshev ordering done elsewhere)
		for (MyLandCell c : feasibleAllocations.getToPossiblyDevelop()) {
			c.setToDevelop(true);
			feasibleAllocations.getToIntensifyAcai().add(c);
			feasibleAllocations.getToManiocGarden().add(c);
		}
		
		// TODO: take over unmanaged property, if projected labour and capital allow
	}
	
	protected void resetActions() {
		feasibleAllocations.reset();
	}
	
	protected double getExpectedPrice(LandUse crop) {
		return marketPrices.get(crop);
	}
	
	protected double getActualPrice(LandUse crop) {
		return marketPrices.get(crop);
	}

	protected HarvestSolution findHarvestSolution() {
		/*
		 * maximize: sum of [ (prices - immediate cost) * yield *
		 * 	futureness_penalty] + wage * employment_labour 
		 * 
		 * s.t.
		 * 
		 * sum (cost * plots) <= capital sum (labour * plots) <= labour
		 * available land non-negativity constraints
		 * 
		 * standard form
		 * 
		 * variables: harvested plots of: acai, maniocgarden, timber
		 */
		
		resetActions();
		
		// iterate through portfolio: identify actions
		for (MyLandCell c : tenure.values()) {
			LandCell cell = c.getCell();
			if (c.getLandUse() == LandUse.ACAI) {
				feasibleAllocations.getToHarvestIntenseAcai().add(c);
			} else if (c.getLandUse() == LandUse.MANIOCGARDEN) {
				if (cell.getManiocGardenAge() > 3) {
				} else {
					feasibleAllocations.getToHarvestHousegarden().add(c);

					if (cell.getAcaiDensity() > 0)
						feasibleAllocations.getToHarvestIntenseAcai().add(c);
				}
			} else if (c.getLandUse() == LandUse.FOREST) {
				if (cell.getForestAge() > 3) {
					feasibleAllocations.getToHarvestTimber().add(c);
					feasibleAllocations.getToHarvestAcai().add(c);
				}
			} else if (c.getLandUse() == LandUse.FALLOW) {
				final LandUse lastLandUse = c.getLastLandUse();
				switch (lastLandUse) {
				case ACAI:
					feasibleAllocations.getToHarvestAcai().add(c);
					break;
				default:
					break;
				}
			}
		}

		// http://lpsolve.sourceforge.net/5.5/formulate.htm#Java
		int nResourceCols = 4;
		int ncols = nResourceCols + linkedHouseholds.size();
		int retVal = 0;
		int j;
		int[] colno = new int[ncols];
		double[] row = new double[ncols];
		
		// get list of agents available for recall back to the household
		// need to move keyset to ordered list for consistency
		List<NetworkedUrbanAgent> recallSolutions = new LinkedList<NetworkedUrbanAgent>();
		recallSolutions.addAll(linkedHouseholds.keySet());

		try {
			LpSolve lp = LpSolve.makeLp(0, ncols);
			if (lp.getLp() == 0) {
				retVal = 1;
			}
	
			// name the LP, set columns
			if (retVal == 0) {
				System.out.println("it is running!");
				lp.setLpName("harvest" + getID());
				
				int i = 1;
				lp.setColName(i++, "acai");
				lp.setColName(i++, "intenseacai");
				lp.setColName(i++, "manioc");
				lp.setColName(i++, "timber");
				
				// add person-job offer columns
				int k = i;
				Iterator<NetworkedUrbanAgent> recallIter = recallSolutions.iterator();
				while (recallIter.hasNext()) {
					// easier to implement this as a NOT recall
					lp.setColName(k, "NOTrecall" + String.valueOf(recallIter.next().getID()));
					lp.setInt(k++, true);
				}
				
				lp.setAddRowmode(true);
			}
			
			if (retVal == 0) {
				// labour requirements
				System.out.println("it is running!");
				j = 0;
	
				colno[j] = 1;
				row[j++] = harvestAcaiLabour * labourMultiplier;
	
				colno[j] = 2;
				row[j++] = harvestAcaiLabour * labourMultiplier;
				
				colno[j] = 3;
				row[j++] = harvestManiocLabour * labourMultiplier;
				
				colno[j] = 4;
				row[j++] = harvestTimberLabour * labourMultiplier;
				
				Iterator<NetworkedUrbanAgent> linkedIter = recallSolutions.iterator();
				while (linkedIter.hasNext()) {
					Person p = linkedIter.next().getPerson();
					colno[j] = j + 1;
					row[j++] = p.getLabour();
				}
	
				/* add the row to lpsolve */
				lp.addConstraintex(j, row, colno, LpSolve.LE, labour);
			}
	
			if (retVal == 0) {
				// set up upper bounds on optimizing variables
				lp.setBounds(1, 0, feasibleAllocations.getToHarvestAcai().size());
				lp.setBounds(2, 0, feasibleAllocations.getToHarvestIntenseAcai().size());
				lp.setBounds(3, 0, feasibleAllocations.getToHarvestHousegarden().size());
				lp.setBounds(4, 0, feasibleAllocations.getToHarvestTimber().size());
				
				// set upper bounds on binary variables
				for (int i = nResourceCols + 1; i <= ncols; i++) {
					lp.setBounds(i, 0, 1);
				}
			}
			
			// add objective function
			if (retVal == 0) {
				lp.setAddRowmode(false);
	
				/* set the objective function (143 x + 60 y) */
				j = 0;
	
				double acaiYield = 0;
				if (!feasibleAllocations.getToHarvestAcai().isEmpty()) {
					for (MyLandCell c : feasibleAllocations.getToHarvestAcai()) {
						acaiYield += c.getCell().getAcaiYield();
					}
					acaiYield /= (double) feasibleAllocations.getToHarvestAcai().size();	
				}
				
				double intenseacaiYield = 0;
				if (!feasibleAllocations.getToHarvestIntenseAcai().isEmpty()) {
					for (MyLandCell c : feasibleAllocations.getToHarvestIntenseAcai()) {
						intenseacaiYield += c.getCell().getIntenseAcaiYield();
					}
					intenseacaiYield /= (double) feasibleAllocations.getToHarvestIntenseAcai().size();
				}
				
				double gardenYield = 0;
				if (!feasibleAllocations.getToHarvestHousegarden().isEmpty()) {
					for (MyLandCell c : feasibleAllocations.getToHarvestHousegarden()) {
						gardenYield += c.getCell().getGardenYield();
					}
					gardenYield /= (double) feasibleAllocations.getToHarvestHousegarden().size();
				}
				
				double timberYield = 0;
				if (!feasibleAllocations.getToHarvestTimber().isEmpty()) {
					for (MyLandCell c : feasibleAllocations.getToHarvestTimber()) {
						timberYield += c.getCell().getTimberYield();
					}
					timberYield /= (double) feasibleAllocations.getToHarvestTimber().size();
				}
				
				colno[j] = 1;
				row[j++] = acaiYield * getExpectedPrice(LandUse.ACAI);
				
				colno[j] = 2;
				row[j++] = intenseacaiYield * getExpectedPrice(LandUse.ACAI);
	
				colno[j] = 3;
				row[j++] = gardenYield * getExpectedPrice(LandUse.MANIOCGARDEN);
				
				colno[j] = 4;
				row[j++] = timberYield * getExpectedPrice(LandUse.FOREST);
				
				Iterator<NetworkedUrbanAgent> recallIter = recallSolutions.iterator();
				while (recallIter.hasNext()) {
					NetworkedUrbanAgent e = recallIter.next();
					colno[j] = j + 1;
					
					row[j++] = e.getWage();
				}
	
				/* set the objective in lpsolve */
				lp.setObjFnex(j, row, colno);
				System.out.println("it is running!");
			}
			
			if (retVal == 0) {
				lp.setMaxim();
	
				// lp.writeLp("harvest" + getID() + ".lp");
				
				lp.setVerbose(LpSolve.IMPORTANT);
	
				retVal = lp.solve();
			}
		
			if (retVal == 0) {
				lp.getVariables(row);
				/* a solution is calculated, now lets get some results */

				/*
				// objective value
				System.out.println("Optimal harvest solution found for household " + getID() + ": " + lp.getObjective());

				// variable values
				for (j = 0; j < ncols; j++)
					System.out.println(lp.getColName(j + 1) + ": " + row[j]);
				*/
				
				int i = 0;
				HarvestSolution recommendation = new HarvestSolution();
				recommendation.setAcai(row[i++]);
				recommendation.setIntenseAcai(row[i++]);
				recommendation.setGardens(row[i++]);
				recommendation.setTimber(row[i++]);
				
				List<NetworkedUrbanAgent> recall = new LinkedList<NetworkedUrbanAgent>();
				Iterator<NetworkedUrbanAgent> recallIter = recallSolutions.iterator();
				while (recallIter.hasNext()) {
					NetworkedUrbanAgent a = recallIter.next();
					if (row[i++] == 0) { // 1 indicates DO NOT recall
						recall.add(a);
					}
				}
				recommendation.setRecall(recall);

				return recommendation;
			}

			/* clean up such that all used memory by lpsolve is freed */
			if (lp.getLp() != 0) {
				lp.deleteLp();
			}
		} catch (LpSolveException e) {
			e.printStackTrace();
		}

		return null;
	}

	protected DevelopmentSolution findDevelopmentSolution() {
		/*
		 * maximize: sum of [ (prices - immediate cost) * yield *
		 * 	futureness_penalty] + wage * employment_labour 
		 * 
		 * s.t.
		 * 
		 * sum (cost * plots) <= capital sum (labour * plots) <= labour
		 * available land non-negativity constraints
		 * 
		 * standard form
		 * 
		 * variables: NEW plots of: acai, maniocgarden
		 */

		// http://lpsolve.sourceforge.net/5.5/formulate.htm#Java
		int nResourceCols = 4;
		int ncols = nResourceCols + feasibleAllocations.getEmployables().size() + linkedHouseholds.size();
		int retVal = 0;
		int j;
		int[] colno = new int[ncols];
		double[] row = new double[ncols];
		
		// need to move sets to ordered lists for consistency
		List<Entry<Person, JobOffer>> employableSolutions = new LinkedList<Entry<Person, JobOffer>>(); 
		employableSolutions.addAll(feasibleAllocations.getEmployables().entrySet());
		
		List<NetworkedUrbanAgent> recallSolutions = new LinkedList<NetworkedUrbanAgent>();
		recallSolutions.addAll(linkedHouseholds.keySet());

		try {
			LpSolve lp = LpSolve.makeLp(0, ncols);
			if (lp.getLp() == 0) {
				retVal = 1;
			}
	
			// name the LP, set column names
			if (retVal == 0) {
				lp.setLpName("development" + getID());
				
				int i = 1;
				lp.setColName(i++, "acai");
				lp.setColName(i++, "manioc");
				lp.setColName(i++, "maintainacai");
				lp.setColName(i++, "maintainmanioc");
				
				// add person-job offer columns
				int k = i;
				Iterator<Entry<Person, JobOffer>> iter = employableSolutions.iterator();
				while (iter.hasNext()) {
					lp.setColName(k, "j" + String.valueOf(iter.next().getKey().getID()));
					lp.setInt(k++, true);
				}
				
				i = k;
				Iterator<NetworkedUrbanAgent> recallIter = recallSolutions.iterator();
				while (recallIter.hasNext()) {
					// easier to implement this as a NOT recall
					lp.setColName(k, "NOTrecall" + String.valueOf(recallIter.next().getID()));
					lp.setInt(k++, true);
				}
				
				lp.setAddRowmode(true);
			}
			
			// set up constraint rows
			// first constraint
			if (retVal == 0) {
				// construct first row (capital)
				j = 0;
	
				 /* first column */
				colno[j] = 1;
				row[j++] = acaiCost * capitalMultiplier;
	
				colno[j] = 2; /* second column */
				row[j++] = maniocCost * capitalMultiplier;
				
				colno[j] = 3;
				row[j++] = maintainAcaiCost * capitalMultiplier;
	
				colno[j] = 4; /* second column */
				row[j++] = maintainManiocCost * capitalMultiplier;
				
				Iterator<Entry<Person, JobOffer>> iter = employableSolutions.iterator();
				while (iter.hasNext()) {
					iter.next();
					colno[j] = j + 1;
					row[j++] = 0; // transportation cost = 0 for now
				}
				
				Iterator<NetworkedUrbanAgent> recallIter = recallSolutions.iterator();
				while (recallIter.hasNext()) {
					recallIter.next();
					colno[j] = j + 1;
					row[j++] = 0;
				}
				
				/* add the row to lpsolve */
				lp.addConstraintex(j, row, colno, LpSolve.LE, capital);
			}
	
			if (retVal == 0) {
				// labour requirements
				j = 0;
	
				colno[j] = 1;
				row[j++] = acaiLabour * labourMultiplier;
	
				colno[j] = 2;
				row[j++] = maniocLabour * labourMultiplier;
				
				colno[j] = 3;
				row[j++] = maintainAcaiLabour * labourMultiplier;
				
				colno[j] = 4;
				row[j++] = maintainManiocLabour * labourMultiplier;
				
				Iterator<Entry<Person, JobOffer>> iter = employableSolutions.iterator();
				while (iter.hasNext()) {
					Entry<Person, JobOffer> e = iter.next();
					colno[j] = j + 1;
					row[j++] = e.getKey().getLabour();
				}
				
				Iterator<NetworkedUrbanAgent> linkedIter = recallSolutions.iterator();
				while (linkedIter.hasNext()) {
					Person p = linkedIter.next().getPerson();
					colno[j] = j + 1;
					row[j++] = p.getLabour();
				}
	
				/* add the row to lpsolve */
				lp.addConstraintex(j, row, colno, LpSolve.LE, labour);
			}
	
			if (retVal == 0) {
				// land requirements
				j = 0;
	
				colno[j] = 1;
				row[j++] = 1;
	
				colno[j] = 2;
				row[j++] = 1;
				
				colno[j] = 3;
				row[j++] = 0;
				
				colno[j] = 4;
				row[j++] = 0;
				
				Iterator<Entry<Person, JobOffer>> iter = employableSolutions.iterator();
				while (iter.hasNext()) {
					iter.next();
					colno[j] = j + 1;
					row[j++] = 0;
				}
				
				Iterator<NetworkedUrbanAgent> recallIter = recallSolutions.iterator();
				while (recallIter.hasNext()) {
					recallIter.next();
					colno[j] = j + 1;
					row[j++] = 0;
				}
	
				/* add the row to lpsolve */
				lp.addConstraintex(j, row, colno, LpSolve.LE, feasibleAllocations.getToPossiblyDevelop().size());
			}
	
			if (retVal == 0) {
				// set up upper bounds on optimizing variables
				lp.setBounds(1, 0, feasibleAllocations.getToIntensifyAcai().size());
				lp.setBounds(2, 0, feasibleAllocations.getToManiocGarden().size());
				lp.setBounds(3, 0, feasibleAllocations.getToMaintainAcai().size());
				lp.setBounds(4, 0, feasibleAllocations.getToMaintainManiocGarden().size());
				
				// set upper bounds on binary variables
				for (int i = nResourceCols + 1; i <= ncols; i++) {
					lp.setBounds(i, 0, 1);
				}
			}
			
			// add objective function
			if (retVal == 0) {
				lp.setAddRowmode(false); /*
										 * rowmode should be turned off again when
										 * done building the model
										 */
	
				/* set the objective function (143 x + 60 y) */
				j = 0;
	
				// new plots
				colno[j] = 1; /* first column */
				row[j++] = 15000d * getExpectedPrice(LandUse.ACAI) - acaiCost;
	
				colno[j] = 2; /* second column */
				row[j++] = 5000d * getExpectedPrice(LandUse.MANIOCGARDEN) - maniocCost;
				
				// maintenance
				colno[j] = 3; /* third column */
				row[j++] = 15000d * getExpectedPrice(LandUse.ACAI) - maintainAcaiCost;
				
				colno[j] = 4; /* fourth column */
				row[j++] = 5000d * getExpectedPrice(LandUse.MANIOCGARDEN) - maintainManiocCost;
				
				Iterator<Entry<Person, JobOffer>> iter = employableSolutions.iterator();
				while (iter.hasNext()) {
					Entry<Person, JobOffer> e = iter.next();
					colno[j] = j + 1;
					
					row[j++] = e.getValue().getWage();
				}
				
				Iterator<NetworkedUrbanAgent> recallIter = recallSolutions.iterator();
				while (recallIter.hasNext()) {
					NetworkedUrbanAgent e = recallIter.next();
					colno[j] = j + 1;
					
					row[j++] = e.getWage();
				}
	
				/* set the objective in lpsolve */
				lp.setObjFnex(j, row, colno);
			}
			
			if (retVal == 0) {
				lp.setMaxim();
	
				// lp.writeLp(RunState.getInstance().getFromRegistry("path") + "_" + "development_nonpredictive_" + getID() + "_step" + Double.toString(RunEnvironment.getInstance().getCurrentSchedule().getTickCount()) + ".lp.txt");
				
				lp.setVerbose(LpSolve.IMPORTANT);
	
				retVal = lp.solve();
			}
			
			if (retVal == 0) {
				lp.getVariables(row);
				/* a solution is calculated, now lets get some results */

				/*
				// objective value
				System.out.println("Optimal development solution found for household " + getID() + ": " + lp.getObjective());

				// variable values
				for (j = 0; j < ncols; j++)
					System.out.println(lp.getColName(j + 1) + ": " + row[j]);
				*/
				
				
				int i = 0;
				DevelopmentSolution recommendation = new DevelopmentSolution();
				recommendation.setAcai(row[i++]);
				recommendation.setGarden(row[i++]);
				recommendation.setMaintainAcai(row[i++]);
				recommendation.setMaintainGarden(row[i++]);
				
				Map<Person, JobOffer> employable = new HashMap<Person, JobOffer>();
				Iterator<Entry<Person, JobOffer>> iter = employableSolutions.iterator();
				while (iter.hasNext()) {
					Entry<Person, JobOffer> e = iter.next();
					if (row[i++] > 0) {
						employable.put(e.getKey(), e.getValue());
					}
				}
				recommendation.setEmploy(employable);
				
				List<NetworkedUrbanAgent> recall = new LinkedList<NetworkedUrbanAgent>();
				Iterator<NetworkedUrbanAgent> recallIter = recallSolutions.iterator();
				while (recallIter.hasNext()) {
					NetworkedUrbanAgent a = recallIter.next();
					if (row[i++] == 0) { // 1 indicates DO NOT recall
						recall.add(a);
					}
				}
				recommendation.setRecall(recall);

				return recommendation;
			}

			/* clean up such that all used memory by lpsolve is freed */
			if (lp.getLp() != 0) {
				lp.deleteLp();
			}
		} catch (LpSolveException e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public void intermediate() {
	}

	@Override
	public void act() {
		// make land use changes, maintain old crops
		// assumption of primary income: acai and annual crops
		// secondary income: none (would be pensions and in-town labour for
		// networked households)
		
		// forest fallow: de-intensify acai (no actual labour, cost)
		for (MyLandCell c : feasibleAllocations.getToForestFallow()) {
			if (!forestFallow(c))
				break;
		}

		resetLabour();

		// slash and burn (again, no labour)
		for (MyLandCell c : feasibleAllocations.getToFallow()) {
			if (!fallow(c))
				break;
		}

		DevelopmentSolution solution = findDevelopmentSolution();
		if (solution != null) {
			// FIXME: do non-integer solutions (partial plots)
			
			// send off people
			Iterator<Entry<Person, JobOffer>> offeredIter = solution.getEmploy().entrySet().iterator();
			if (offeredIter.hasNext()) {
				Entry<Person, JobOffer> e = offeredIter.next();
				send(e.getKey(), e.getValue());
			}
			
			// get people back
			Iterator<NetworkedUrbanAgent> recallIter = solution.getRecall().iterator();
			if (recallIter.hasNext()) {
				recall(recallIter.next(), "action");
			}
			
			resetLabour();
			
			double acai = solution.getAcai();
			double manioc = solution.getGarden();
			double maintainAcai = solution.getMaintainAcai();
			double maintainGardens = solution.getMaintainGarden();
	
			// order shouldn't matter; it's optimal!
			// then again, spatial land allocation isn't
			while (acai >= 1) {
				double acaiIntensification = intensifyAcai();
				if (acaiIntensification <= 0)
					break;
				
				acai -= acaiIntensification;
			}
	
			while (manioc >= 1) {
				if (expandHousegarden()) manioc -= 1;
				
				if (feasibleAllocations.getToManiocGarden().isEmpty())
					break;
			}
			
			ListIterator<MyLandCell> iter = feasibleAllocations.getToMaintainAcai().listIterator();
			while (maintainAcai >= 1) {
				if (!iter.hasNext()) {
					System.err.println("Node " + getID() + " acai maintenance failure: " + solution.getMaintainAcai() + " required, " + feasibleAllocations.getToMaintainAcai().size() + " available.");
					break;
				}
				
				//if (maintain(iter.next()))
				maintain(iter.next());
				maintainAcai -= 1;
			}
			
			iter = feasibleAllocations.getToMaintainManiocGarden().listIterator();
			while (maintainGardens >= 1) {
				if (!iter.hasNext()) {
					System.err.println("Node " + getID() + " garden maintenance failure: " + solution.getMaintainGarden() + " required, " + feasibleAllocations.getToMaintainManiocGarden().size() + " available.");
					break;
				}
				
				//if (maintain(iter.next()))
				maintain(iter.next());
				maintainGardens -= 1;
			}
		}
	}
	
	/**
	 * Intensify acai by a single, unspecified cell. The acai cell will be taken
	 * from the <code>toIntensifyAcai</code> list.
	 * 
	 * @return <code>true</code> if successful.
	 */
	protected double intensifyAcai() {
		MyLandCell c = null;
		try {
			while (!feasibleAllocations.getToIntensifyAcai().isEmpty()) {
				c = feasibleAllocations.getToIntensifyAcai().remove();
				if (c.isToDevelop())
					break;
			}
			
			if (c == null || !c.isToDevelop())
				return 0;
			
			// return intensifyAcai(c);
			return intensifyAcai(c) ? 1 : 0;
		} catch (NoSuchElementException e) {
			return 0;
		}
	}

	/**
	 * Expand housegarden by a single cell, the first cell in the
	 * <code>toHousegarden</code> list.
	 * 
	 * @return <code>true</code> if successful.
	 */
	protected boolean expandHousegarden() {
		MyLandCell c = null;
		try {
			while (!feasibleAllocations.getToManiocGarden().isEmpty()) {
				c = feasibleAllocations.getToManiocGarden().remove();
				if (c.isToDevelop())
					break;
			}
			// return c != null && c.isToDevelop() && expandManiocGarden(c);
			return expandManiocGarden(c);
		} catch (NoSuchElementException e) {
			return false;
		}
	}

	@Override
	public void harvest() {
		resetLabour();
		
		HarvestSolution solution = findHarvestSolution();
		
		if (solution != null) {
			// get labour back
			Iterator<NetworkedUrbanAgent> recallIter = solution.getRecall().iterator();
			if (recallIter.hasNext()) {
				recall(recallIter.next(), "harvest");
			}
			
			resetLabour();
			resetHarvestCount();

			double acai = solution.getAcai();
			double intenseAcai = solution.getIntenseAcai();
			double gardens = solution.getGardens();
			double timber = solution.getTimber();
			
			while (acai >= 1) {
				MyLandCell acaiCell = feasibleAllocations.getToHarvestAcai().remove(0);
				LandCell cell = acaiCell.getCell();

				double yield = cell.getAcaiYield();
				labour -= harvestAcaiLabour	* labourMultiplier;
				capital += yield * getActualPrice(LandUse.ACAI);
				
				acaiYield += yield;
				
				acai -= 1;
			}
			
			while (intenseAcai >= 1) {
				MyLandCell acaiCell = feasibleAllocations.getToHarvestIntenseAcai().remove(0);
				LandCell cell = acaiCell.getCell();

				double yield = cell.getIntenseAcaiYield();
				labour -= harvestAcaiLabour	* labourMultiplier;
				capital += yield * getActualPrice(LandUse.ACAI);
				
				acaiYield += yield;
				
				intenseAcai -= 1;
			}
			
			while (gardens >= 1) {
				MyLandCell myCell = feasibleAllocations.getToHarvestHousegarden().remove(0);
				LandCell cell = myCell.getCell();

				double yield = cell.getGardenYield();
				
				labour -= harvestManiocLabour * labourMultiplier;
				capital += yield * getActualPrice(LandUse.MANIOCGARDEN);
				
				maniocYield += yield;
				
				gardens -= 1;
			}
			
			while (timber >= 1) {
				System.out.println("timber available");
				MyLandCell timberCell = feasibleAllocations.getToHarvestTimber().remove(0);
				LandCell cell = timberCell.getCell();

				double yield = cell.getTimberYield();
				
				// clear land
				cell.setForestDensity(0);
				cell.setCapoeiraDensity(1);
				cell.setAcaiDensity(0);
				cell.setManiocGardenDensity(0);
				cell.setFieldsDensity(0);
				cell.setSecondarySuccessionDensity(0);
				timberCell.setLandUse(LandUse.FALLOW); // is this correct?

				labour -= harvestTimberLabour * labourMultiplier; // labour to clear land
				capital += yield * getActualPrice(LandUse.FOREST);
				
				timberYield += yield;
				
				timber -= 1;
			}
			
		} else {
			System.out.println("No solution found");
		}
	}

	@Override
	public void retrospect() {
		
	}

	@Override
	public void store(MessageEnvelope message) {
		mailbox.put(message.getSource(), message.getContents());
	}

	
}
