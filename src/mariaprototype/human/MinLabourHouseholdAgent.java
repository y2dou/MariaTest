/**
 * 
 */
package mariaprototype.human;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.ListIterator;

import mariaprototype.FuzzyUtility;
import mariaprototype.environmental.LandCell;
import mariaprototype.environmental.LandUse;



import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.environment.RunState;

import lpsolve.LpSolve;
import lpsolve.LpSolveException;

import mariaprototype.human.messaging.MessageEnvelope;

/**same as LinearOptimizing, but change the object as min labour, 
 * add revenue>=subsistencerequrest as constrain
 * @author DOU Yue
 *
 */
public class MinLabourHouseholdAgent extends SimpleHouseholdAgent {
	protected FeasibleAllocations feasibleAllocations = new FeasibleAllocations();
	/**
	 * 
	 */
	public MinLabourHouseholdAgent() {
		// TODO Auto-generated constructor stub
		super();
	}

	
	public MinLabourHouseholdAgent(int id) {
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
		// TODO Auto-generated method stub
		processMessages();

		resetActions();

		// consider job offers, move people to town if desired
		ArrayList<Person> eligibleMembers = new ArrayList<Person>();
		int males = 0; // adults
		int females = 0;
		if (!jobOffers.isEmpty()) {
		//	System.out.println("size of JobOffer="+jobOffers.size());
			ListIterator<Person> members = familyMembers.listIterator();
			while (members.hasNext()) {
				Person p = members.next();
				if (p.getAge() >= 60) {
					if (p.isFemale())
						females++;
					else
						males++;
				} else if (p.getAge() > 18) {
					//when it's older than 18, then there's no school attendance to check.
					eligibleMembers.add(p);
		//			System.out.println("eligible member add one=="+p.getAge());
					if (p.isFemale())
						females++;
					else
						males++;
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
			//	p.setWage(o.getWage());
				
			//	this.setWage(o.getWage());
		//		System.out.println(this.getWage());
				if (p.isFemale()) {
					if (females > 1) { //why check if females>1?
						feasibleAllocations.getEmployables().put(p, o);
					//	System.out.println("female");
						break;
					}
				} else {
					if (males > 1) {
						feasibleAllocations.getEmployables().put(p, o);
					//	System.out.println("male got the job");
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
					feasibleAllocations.getToPossiblyDevelop().add(c);
					break;

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
		//	feasibleAllocations.getToIntensifyAcai().add(c);
		//	feasibleAllocations.getToManiocGarden().add(c);
			if (c.getDistanceToWater() <= 40) {
				feasibleAllocations.getToIntensifyAcai().add(c);}
				if (c.getDistanceToWater() >= 30 ){
				feasibleAllocations.getToManiocGarden().add(c);}
		}
	
		// TODO: take over unmanaged property, if projected labour and capital allow
	}
	
	private void resetActions() {
		// TODO Auto-generated method stub
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
		 * 
		 * standard form
		 * 
		 * variables: harvested plots of: acai, maniocgarden, timber
		 */
		
		resetActions();//why reset here. then off-farming work is null.
	//	System.out.println("L273 after resetAction"+feasibleAllocations.getEmployables().size());
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
			//		System.out.println("NOT Recall");
				}
				
				lp.setAddRowmode(true);
			}
			
	

			
			if (retVal == 0) {
				// set up upper bounds on optimizing variables
				lp.setBounds(1, 0, feasibleAllocations.getToHarvestAcai().size());
				lp.setBounds(2, 0, feasibleAllocations.getToHarvestIntenseAcai().size());
				lp.setBounds(3, 0, feasibleAllocations.getToHarvestHousegarden().size());
				lp.setBounds(4, 0, feasibleAllocations.getToHarvestTimber().size());
				
		//	System.out.println("L443 household garden="+feasibleAllocations.getToHarvestHousegarden().size());
				// set upper bounds on binary variables
				for (int i = nResourceCols + 1; i <= ncols; i++) {
					lp.setBounds(i, 0, 1);
				}
			}
			
			// add monety function
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
		   //         System.out.println("Line406 "+acaiYield);
				}
				
				double intenseacaiYield = 0;
				if (!feasibleAllocations.getToHarvestIntenseAcai().isEmpty()) {
					for (MyLandCell c : feasibleAllocations.getToHarvestIntenseAcai()) {
						intenseacaiYield += c.getCell().getIntenseAcaiYield();
					}
					intenseacaiYield /= (double) feasibleAllocations.getToHarvestIntenseAcai().size();
				//	System.out.println("Line415 "+acaiYield);
				}
				
				double gardenYield = 0;
				if (!feasibleAllocations.getToHarvestHousegarden().isEmpty()) {
					for (MyLandCell c : feasibleAllocations.getToHarvestHousegarden()) {
						gardenYield += c.getCell().getGardenYield();
					}
					gardenYield /= (double) feasibleAllocations.getToHarvestHousegarden().size();
				//    System.out.println("L424 "+gardenYield);
				}
				
				double timberYield = 0;
				if (!feasibleAllocations.getToHarvestTimber().isEmpty()) {
					for (MyLandCell c : feasibleAllocations.getToHarvestTimber()) {
						timberYield += c.getCell().getTimberYield();
					}
					timberYield /= (double) feasibleAllocations.getToHarvestTimber().size();
				//	System.out.println("L433 "+timberYield);
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
			//		System.out.println("Linear L447, "+e.getWage());
				}
				lp.setObjFnex(j, row, colno);
				//set as a constrain.
			//	lp.addConstraintex(j, row, colno, LpSolve.GE, this.getSubsistenceRequirements()-this.pension);
			}
			
			if (retVal == 0) {
				// labour requirements
			//	lp.setAddRowmode(false);
				
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
				//set as objective
			//	lp.setObjFnex(j, row, colno);
		//		System.out.println("L377="+labour);
			}
			
			if (retVal == 0) {
				lp.setMaxim();
	        //    lp.setMinim();
				// lp.writeLp("harvest" + getID() + ".lp");
				
				lp.setVerbose(LpSolve.IMPORTANT);
	
				retVal = lp.solve();
			}
		
			if (retVal == 0) {
				lp.getVariables(row);
				/* a solution is calculated, now lets get some results */
				 double objValue=lp.getObjective();
		      //      System.out.println("Objective value: " + objValue);
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
		//		System.out.println("garden="+row[2]);
				
		//		for (i=0;i<=5;i++){
			//		System.out.println(lp.getColName(i + 1) + ": " + row[i]);
				//}
				
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
		
		double subsistenceRequirement = this.getSubsistenceAcaiRequirement() * marketPrices.get(LandUse.ACAI) + 
		                         this.getSubsistenceManiocRequirement() * marketPrices.get(LandUse.MANIOCGARDEN);
		this.setSubsistenceRequirement(subsistenceRequirement);
		// need to move sets to ordered lists for consistency
		List<Entry<Person, JobOffer>> employableSolutions = new LinkedList<Entry<Person, JobOffer>>(); 
		employableSolutions.addAll(feasibleAllocations.getEmployables().entrySet());
	//	System.out.println("l570 employments in development="+employableSolutions.size());
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
			//	lp.addConstraintex(j, row, colno, LpSolve.LE, capital-this.getSubsistenceRequirements());
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
				/*		lp.setAddRowmode(false); 
										 * rowmode should be turned off again when
										 * done building the model
										 */
	
				/* set the objective function (143 x + 60 y) */
				j = 0;
	
				// new plots
				colno[j] = 1; /* first column */
				row[j++] = 10000d * getExpectedPrice(LandUse.ACAI) - acaiCost;
	
				colno[j] = 2; /* second column */
			//	row[j++] = 5000d * getExpectedPrice(LandUse.MANIOCGARDEN) - maniocCost;
				row[j++] = 3000d * getExpectedPrice(LandUse.MANIOCGARDEN) - maniocCost;
				
				// maintenance
				colno[j] = 3; /* third column */
				row[j++] = 15000d * getExpectedPrice(LandUse.ACAI) - maintainAcaiCost;
				
				colno[j] = 4; /* fourth column */
				//row[j++] = 5000d * getExpectedPrice(LandUse.MANIOCGARDEN) - maintainManiocCost;
				row[j++] = 3000d * getExpectedPrice(LandUse.MANIOCGARDEN) - maniocCost;
				
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
			
				
				//set the objective
			//	lp.setObjFnex(j, row, colno);
				// add the row to lpsolve 
				lp.addConstraintex(j, row, colno, LpSolve.GE, subsistenceRequirement-this.pension);
				
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
			
			//Yue March 9, 2015, produce the basic subsistence requirement--acai;
			 
			// add objective function
		
			if (retVal == 0) {
				// labour requirements
				
				lp.setAddRowmode(false); 
				
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
				//lp.setObjFnex(j, row, colno);
		//		lp.addConstraintex(j, row, colno, LpSolve.LE, labour);	
				lp.setObjFnex(j, row, colno);
			}
			
			
			if (retVal == 0) {
			//	lp.setMaxim();
				lp.setMinim();
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
		//		System.out.println("L917 garden="+row[1]+" maintainGarden="+row[3]);
				Map<Person, JobOffer> employable = new HashMap<Person, JobOffer>();
				Iterator<Entry<Person, JobOffer>> iter = employableSolutions.iterator();
				while (iter.hasNext()) {
					Entry<Person, JobOffer> e = iter.next();
					if (row[i++] > 0) {
						employable.put(e.getKey(), e.getValue());
	//					System.out.println("L808 "+employable.size()+" salary="+e.getValue().getWage());
					} else {
					//	employable.put(null, null);
					//	System.out.println("no job");
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
	public void act() {
		// TODO Auto-generated method stub
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
			
			while (offeredIter.hasNext()) {
				Entry<Person, JobOffer> e = offeredIter.next();
				send(e.getKey(), e.getValue());
		//		System.out.println("L839 act offer="+offeredIter.next().getValue().getWage());
			}
			
			// get people back
			Iterator<NetworkedUrbanAgent> recallIter = solution.getRecall().iterator();
			while (recallIter.hasNext()) {
				recall(recallIter.next(), "action");
	//			System.out.println("L884 recall");
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
		// TODO Auto-generated method stub
		resetLabour();

		HarvestSolution solution = findHarvestSolution();
	//	System.out.println("L978=");
		if (solution != null) {
			// get labour back
			Iterator<NetworkedUrbanAgent> recallIter = solution.getRecall().iterator();
			while (recallIter.hasNext()) {
				recall(recallIter.next(), "harvest");
		//		System.out.println("Liner Optimizinghhd L995 recall");
			}
			
			resetLabour();
			resetHarvestCount();

			double acai = solution.getAcai();
			double intenseAcai = solution.getIntenseAcai();
			double gardens = solution.getGardens();
			double timber = solution.getTimber();
		//	System.out.println("L1120="+gardens);
			
			while (acai >= 1) {
				MyLandCell acaiCell = feasibleAllocations.getToHarvestAcai().remove(0);
				LandCell cell = acaiCell.getCell();

				double yield = cell.getAcaiYield();
				labour -= harvestAcaiLabour	* labourMultiplier;
				capital += yield * getActualPrice(LandUse.ACAI);
	//			System.out.println("LOhHD L939,capital="+capital+"=acaiprice="+getActualPrice(LandUse.ACAI));
				acaiYield += yield;
				
				acai -= 1;
			}
			
			while (intenseAcai >= 1) {
				MyLandCell acaiCell = feasibleAllocations.getToHarvestIntenseAcai().remove(0);
				LandCell cell = acaiCell.getCell();

				double yield = cell.getIntenseAcaiYield();
				labour -= harvestAcaiLabour	* labourMultiplier;
				capital += yield * getActualPrice(LandUse.ACAI);
			//	System.out.println("LOhHD L952,capital="+capital+"=acaiprice="+getActualPrice(LandUse.ACAI));
				acaiYield += yield;
				
				intenseAcai -= 1;
			}
			
			while (gardens >= 1) {
				
				MyLandCell myCell = feasibleAllocations.getToHarvestHousegarden().remove(0);
				LandCell cell = myCell.getCell();

				double yield = cell.getGardenYield();
				
				labour -= harvestManiocLabour * labourMultiplier;
				capital += yield * getActualPrice(LandUse.MANIOCGARDEN);
			//	System.out.println("LOhHD L966,capital="+capital+"=maniocprice="+getActualPrice(LandUse.MANIOCGARDEN));
				maniocYield += yield;
				
				gardens -= 1;
			}
			
			while (timber >= 1) {
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
		}
		
	    double annualIncome;
	    annualIncome = acaiYield * getActualPrice(LandUse.ACAI) 
	                   + maniocYield*getActualPrice(LandUse.MANIOCGARDEN)
	                   + timberYield * getActualPrice(LandUse.FOREST)
	                   + this.getWage();
		this.setAnnualIncome(annualIncome);

		capital -= subsistenceRequirement; //to deduct subsistence cost;
	}


	@Override
	public void intermediate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void retrospect() {
		// TODO Auto-generated method stub
		
	}
	public void store(MessageEnvelope message) {
		mailbox.put(message.getSource(), message.getContents());
	}
}
