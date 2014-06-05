package mariaprototype.human;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lpsolve.LpSolve;
import lpsolve.LpSolveException;
import mariaprototype.FuzzyUtility;
import mariaprototype.R;
import mariaprototype.environmental.LandCell;
import mariaprototype.environmental.LandUse;

import org.rosuda.JRI.REXP;
import org.rosuda.JRI.RVector;
import org.rosuda.JRI.Rengine;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.environment.RunState;

/**
 * A Linear Programming Household Agent which does full price forecasting over the
 * next n years at the specified discount rate.
 * 
 * @author arcabrer
 *
 */
public class FullForwardLPHouseholdAgent extends LinearOptimizingHouseholdAgent {
	private int priceMemoryLimit = 10; // memory limit of historic prices in years
	private int yearsForecasted = 3;
	private double discountRate = 0.8;
	
	private List<Double> acaiPrices = new LinkedList<Double>();
	private List<Double> maniocPrices = new LinkedList<Double>();
	private List<Double> timberPrices = new LinkedList<Double>();
	
	private double[] acaiLMCoefficients;
	private double[] maniocLMCoefficients;
	private double[] timberLMCoefficients;
	
	private List<FeasibleAllocations> feasibleAllocations = new ArrayList<FeasibleAllocations>();
	
	public FullForwardLPHouseholdAgent() {
		super();
	}
	
	public FullForwardLPHouseholdAgent(int id) {
		super(id);
	}
	
	@Override
	public void init(HumanContext humanContext, int... coordinates) {
		super.init(humanContext, coordinates);
		
		feasibleAllocations.add(super.feasibleAllocations);
		for (int i = 1; i <= yearsForecasted; i++) {
			feasibleAllocations.add(new FeasibleAllocations());
		}
	}
	
	@Override
	protected void resetActions() {
		super.resetActions();
		
		for (FeasibleAllocations f : feasibleAllocations) {
			f.reset();
		}
	}
	
	@Override
	public void plan() {
		super.plan();
		
		// list actions for next 2 years
		for (int i = 1; i <= yearsForecasted; i++) {
			FeasibleAllocations alloc = feasibleAllocations.get(i);
			
			// forward calculate future plots (best guess for now)
			for (MyLandCell c : tenure.values()) {
				// DO NOT RUN c.age();
				
				LandCell cell = c.getCell();
				if (c.getLandUse() == LandUse.ACAI) {
					if (c.getYearsSinceMaintained() >= 3 - i) {
						alloc.getToHarvestAcai().add(c);
					} else {
						alloc.getToMaintainAcai().add(c);
						alloc.getToHarvestIntenseAcai().add(c);
					}
				} else if (c.getLandUse() == LandUse.MANIOCGARDEN) {
					if (cell.getManiocGardenAge() > 3 - i) {
						alloc.getToForestFallow().add(c);
					} else {
						alloc.getToHarvestHousegarden().add(c);

						if (cell.getAcaiDensity() > 0)
							alloc.getToHarvestIntenseAcai().add(c);

						alloc.getToMaintainManiocGarden().add(c);
					}
				} else if (c.getLandUse() == LandUse.FOREST) {
					if (cell.getForestAge() > 3 - i) {
						alloc.getToHarvestTimber().add(c);
						alloc.getToHarvestAcai().add(c);
					}

					if (c.getYearsSinceLast() > 5 - i) {
						alloc.getToPossiblyDevelop().add(c);
					}
				} else if (c.getLandUse() == LandUse.FALLOW) {
					final LandUse lastLandUse = c.getLastLandUse();
					switch (lastLandUse) {
					case ACAI:
						alloc.getToHarvestAcai().add(c);
						if (c.getYearsSinceLast() > 5 - i)
							alloc.getToPossiblyDevelop().add(c);
						break;

					case MANIOCGARDEN:
						if (c.getYearsSinceLast() > 5 - i)
							alloc.getToPossiblyDevelop().add(c);
						break;

					case FALLOW:
					case FOREST:
						alloc.getToPossiblyDevelop().add(c);
						break;

					case FIELDS:
						if (c.getYearsSinceLast() > 10 - i)
							alloc.getToPossiblyDevelop().add(c);
						break;
					}
				}
			}
			
			for (MyLandCell c : alloc.getToPossiblyDevelop()) {
				alloc.getToIntensifyAcai().add(c);
				alloc.getToManiocGarden().add(c);
			}
		}
		
		// get past costs
		double acaiPrice = getActualPrice(LandUse.ACAI);
		acaiPrices.add(acaiPrice);
		if (acaiPrices.size() > priceMemoryLimit) {
			acaiPrices.remove(0); // remove least recent price
		}
		
		double maniocPrice = getActualPrice(LandUse.MANIOCGARDEN);
		maniocPrices.add(maniocPrice);
		if (maniocPrices.size() > priceMemoryLimit) {
			maniocPrices.remove(0); // remove least recent price
		}
		
		double timberPrice = getActualPrice(LandUse.FOREST);
		timberPrices.add(timberPrice);
		if (timberPrices.size() > priceMemoryLimit) {
			timberPrices.remove(0); // remove least recent price
		}
		
		// run linear regression
		acaiLMCoefficients = getLMCoefficients(acaiPrices);
		maniocLMCoefficients = getLMCoefficients(maniocPrices);
		timberLMCoefficients = getLMCoefficients(timberPrices);
	}
	
	private double[] getLMCoefficients(List<Double> prices) {
		if (prices.size() < 1) {
			return null;
		}
		
		// run LM regression
		Rengine re = R.getInstance().getREngine();
		
		REXP res;
		double[] xArray = new double[priceMemoryLimit];
		double[] yArray = new double[priceMemoryLimit];
		
		for (int i = 0; i < prices.size(); i++) {
			xArray[i] = i + 1;
			yArray[i] = prices.get(i);
		}
		
		re.assign("x", xArray);
		re.assign("y", yArray);
		
		res = re.eval("glm(y~x)");
		RVector fit = res.asVector();
		double[] coefficients = fit.at(0).asDoubleArray();
		
		return coefficients;
	}
	
	protected double getExpectedPrice(LandUse crop, int year) {
		//System.out.print("Actual price: ");
		//System.out.println(getActualPrice(crop));
		
		// extrapolate 
		double[] coefficients = {0.0, 0.0};
		if (crop.equals(LandUse.ACAI)) {
			coefficients = acaiLMCoefficients;
			
			if (year == 0 || acaiPrices.size() < priceMemoryLimit)
				return getActualPrice(crop);
			
		} else if (crop.equals(LandUse.MANIOCGARDEN)) {
			coefficients = maniocLMCoefficients;
			
			if (year == 0 || maniocPrices.size() < priceMemoryLimit)
				return getActualPrice(crop);
		} else if (crop.equals(LandUse.FOREST)) {
			coefficients = timberLMCoefficients;
			
			if (year == 0 || timberPrices.size() < priceMemoryLimit)
				return getActualPrice(crop);
		} else {
			throw new UnsupportedOperationException("Land use " + crop.toString() + " unsupported.");
		}
		
		int effectiveYear = year + priceMemoryLimit;
		double price = coefficients[0] + coefficients[1] * (priceMemoryLimit + effectiveYear);
		
		return price;
	}
	
	@Override
	protected DevelopmentSolution findDevelopmentSolution() {
		int nResourceCols = 4;
		int ncols = (yearsForecasted + 1) * nResourceCols + feasibleAllocations.get(0).getEmployables().size() + linkedHouseholds.size();
		int retVal = 0;
		int j;
		int[] colno = new int[ncols];
		double[] row = new double[ncols];
		
		List<Entry<Person, JobOffer>> employableSolutions = new LinkedList<Entry<Person, JobOffer>>(); 
		employableSolutions.addAll(feasibleAllocations.get(0).getEmployables().entrySet());
		
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
				
				for (int year = 0; year <= yearsForecasted; year++) {
					String yearStr = Integer.toString(year);
					lp.setColName(i++, "acai" + yearStr);
					lp.setColName(i++, "manioc" + yearStr);
					lp.setColName(i++, "maintainacai" + yearStr);
					lp.setColName(i++, "maintainmanioc" + yearStr);
				}
				
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
			
			double discount = 1;
			
			// set up constraint rows
			// first constraint: capital constraints, first year
			if (retVal == 0) {
				// construct first row (capital)
				j = 0;
				
				/* first column */
				colno[j] = 1;
				row[j++] = acaiCost * capitalMultiplier * discount;
	
				colno[j] = 2; /* second column */
				row[j++] = maniocCost * capitalMultiplier * discount;
				
				colno[j] = 3;
				row[j++] = maintainAcaiCost * capitalMultiplier * discount;
	
				colno[j] = 4; /* second column */
				row[j++] = maintainManiocCost * capitalMultiplier * discount;
				
				for (int year = 1; year <= yearsForecasted; year++) {
					for (int r = 0; r < nResourceCols; r++) {
						colno[j] = j + 1;
						row[j++] = 0;
					}
				}
				
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
			
			// capital constraints, n year (after current year)
			if (retVal == 0) {
				for (int year = 1; year <= yearsForecasted; year++) {
					j = 0;
					
					for (int y = 0; y < year; y++) {
						double thisDiscount = Math.pow(discountRate, y);
						
						// previous years costs and income
						colno[j] = j + 1;
						row[j++] = thisDiscount * (acaiCost * capitalMultiplier - 15000d * getExpectedPrice(LandUse.ACAI, y));
			
						colno[j] = j + 1; /* second column */
						row[j++] = thisDiscount * (maniocCost * capitalMultiplier - 5000d * getExpectedPrice(LandUse.MANIOCGARDEN, y));
						
						colno[j] = j + 1;
						row[j++] = thisDiscount * (maintainAcaiCost * capitalMultiplier - 15000d * getExpectedPrice(LandUse.ACAI, y));
			
						colno[j] = j + 1; /* second column */
						row[j++] = thisDiscount * (maintainManiocCost * capitalMultiplier - 5000d * getExpectedPrice(LandUse.MANIOCGARDEN, y));
					}
					
					discount = Math.pow(discountRate, year);
					
					colno[j] = j + 1;
					row[j++] = acaiCost * capitalMultiplier * discount;
		
					colno[j] = j + 1; /* second column */
					row[j++] = maniocCost * capitalMultiplier * discount;
					
					colno[j] = j + 1;
					row[j++] = maintainAcaiCost * capitalMultiplier * discount;
		
					colno[j] = j + 1; /* second column */
					row[j++] = maintainManiocCost * capitalMultiplier * discount;
					
					for (int y = year + 1; y <= yearsForecasted; y++) {
						for (int r = 0; r < nResourceCols; r++) {
							colno[j] = j + 1;
							row[j++] = 0;
						}
					}
					
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
			}
			
			// labour constraints (each year is the same)
			if (retVal == 0) {
				for (int year = 0; year <= yearsForecasted; year++) {
					// labour requirements
					j = 0;
		
					for (int y = 0; y <= yearsForecasted; y++) {
						if (y == year) {
							// add labour requirements for this year
							colno[j] = j + 1;
							row[j++] = acaiLabour * labourMultiplier;
				
							colno[j] = j + 1;
							row[j++] = maniocLabour * labourMultiplier;
							
							colno[j] = j + 1;
							row[j++] = maintainAcaiLabour * labourMultiplier;
							
							colno[j] = j + 1;
							row[j++] = maintainManiocLabour * labourMultiplier;
						} else {
							// set other years to 0 labour
							for (int r = 0; r < nResourceCols; r++) {
								colno[j] = j + 1;
								row[j++] = 0;
							}
						}
					}
					
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
			}
			
			// land constraints
			if (retVal == 0) {
				for (int year = 0; year <= yearsForecasted; year++) {
					FeasibleAllocations alloc = feasibleAllocations.get(year);
					
					j = 0;
					
					for (int y = 0; y <= yearsForecasted; y++) {
						if (y == year) {
							colno[j] = j + 1;
							row[j++] = 1;
				
							colno[j] = j + 1;
							row[j++] = 1;
							
							colno[j] = j + 1;
							row[j++] = 0;
							
							colno[j] = j + 1;
							row[j++] = 0;
						} else {
							for (int r = 0; r < nResourceCols; r++) {
								colno[j] = j + 1;
								row[j++] = 0;
							}
						}
					}
					
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
					lp.addConstraintex(j, row, colno, LpSolve.LE, alloc.getToPossiblyDevelop().size());
				}
			}
			
			// maintenance bounds check
			if (retVal == 0) {
				// for manioc gardens
				int r = 1;
				
				// clear row
				for (int c = 0; c < ncols; c++) {
					row[c] = 0;
				}
				
				j = r;
				colno[j] = j + 1;
				row[j] = 1;
				
				j += 2;
				colno[j] = j + 1;
				row[j] = 1;
				
				j += 4;
				colno[j] = j + 1;
				row[j] = -1;
				
				lp.addConstraintex(ncols, row, colno, LpSolve.GE, 0);
				
				for (int year = 2; year <= yearsForecasted; year++) {
					
					// clear row
					for (int c = 0; c < ncols; c++) {
						row[c] = 0;
					}
					
					j = year * nResourceCols + r - 8;
					colno[j] = j + 1;
					row[j] = 1;
					
					j += 4;
					colno[j] = j + 1;
					row[j] = 1;
					
					j += 6;
					colno[j] = j + 1;
					row[j] = -1;
					
					lp.addConstraintex(ncols, row, colno, LpSolve.GE, 0);
				}
				
				// for acai
				r = 0;
				
				for (int year = 1; year <= yearsForecasted; year++) {
					// clear row
					for (int c = 0; c < ncols; c++) {
						row[c] = 0;
					}
					
					j = year * nResourceCols + r - 4;
					colno[j] = j + 1;
					row[j] = 1;
					
					j += 2;
					colno[j] = j + 1;
					row[j] = 1;
					
					j += 4;
					colno[j] = j + 1;
					row[j] = -1;
					
					lp.addConstraintex(ncols, row, colno, LpSolve.GE, 0);
				}
			}
			
			// bounds check
			if (retVal == 0) {
				j = 1;
				
				// set up upper bounds on optimizing variables
				lp.setBounds(j++, 0, super.feasibleAllocations.getToIntensifyAcai().size());
				lp.setBounds(j++, 0, super.feasibleAllocations.getToManiocGarden().size());
				lp.setBounds(j++, 0, super.feasibleAllocations.getToMaintainAcai().size());
				lp.setBounds(j++, 0, super.feasibleAllocations.getToMaintainManiocGarden().size());
				
				int maxAcai = super.feasibleAllocations.getToMaintainAcai().size() + super.feasibleAllocations.getToIntensifyAcai().size();
				int maxManioc = super.feasibleAllocations.getToMaintainManiocGarden().size() + super.feasibleAllocations.getToManiocGarden().size();
				for (int year = 1; year <= yearsForecasted; year++) {
					lp.setBounds(j++, 0, feasibleAllocations.get(year).getToIntensifyAcai().size());
					lp.setBounds(j++, 0, feasibleAllocations.get(year).getToManiocGarden().size());
					lp.setBounds(j++, 0, maxAcai);
					lp.setBounds(j++, 0, maxManioc);
					
					maxAcai += feasibleAllocations.get(year).getToIntensifyAcai().size();
					maxManioc += feasibleAllocations.get(year).getToManiocGarden().size();
				}
				
				// set upper bounds on binary variables
				for (int i = j; i <= ncols; i++) {
					lp.setBounds(i, 0, 1);
				}
			}
			
			// objective function
			if (retVal == 0) {
				lp.setAddRowmode(false); /*
										 * rowmode should be turned off again when
										 * done building the model
										 */
	
				/* set the objective function (143 x + 60 y) */
				j = 0;
	
				for (int year = 0; year <= yearsForecasted; year++) {
					discount = Math.pow(discountRate, year);
					
					// new plots
					colno[j] = j + 1; /* first column */
					row[j++] = discount * (15000d * getExpectedPrice(LandUse.ACAI, year) - acaiCost);
		
					colno[j] = j + 1; /* second column */
					row[j++] = discount * (5000d * getExpectedPrice(LandUse.MANIOCGARDEN, year) - maniocCost);
					
					// maintenance
					colno[j] = j + 1; /* third column */
					row[j++] = discount * (15000d * getExpectedPrice(LandUse.ACAI, year) - maintainAcaiCost);
					
					colno[j] = j + 1; /* fourth column */
					row[j++] = discount * (5000d * getExpectedPrice(LandUse.MANIOCGARDEN, year) - maintainManiocCost);
				}
				
				double wageWeight = 1;
				for (int y = 1; y <= yearsForecasted; y++) {
					wageWeight += Math.pow(discountRate, y);
				}
				
				Iterator<Entry<Person, JobOffer>> iter = employableSolutions.iterator();
				while (iter.hasNext()) {
					Entry<Person, JobOffer> e = iter.next();
					colno[j] = j + 1;
					
					row[j++] = wageWeight * e.getValue().getWage(); // need to scale this up
				}
				
				Iterator<NetworkedUrbanAgent> recallIter = recallSolutions.iterator();
				while (recallIter.hasNext()) {
					NetworkedUrbanAgent e = recallIter.next();
					colno[j] = j + 1;
					
					row[j++] = wageWeight * e.getWage();
				}
	
				/* set the objective in lpsolve */
				lp.setObjFnex(j, row, colno);
			}
			
			if (retVal == 0) {
				lp.setMaxim();
				
				// lp.writeLp(RunState.getInstance().getFromRegistry("path") + "_" + "development_FULL_" + getID() + "_step" + Double.toString(RunEnvironment.getInstance().getCurrentSchedule().getTickCount()) + ".lp.txt");
				
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
				
				for (int year = 1; year <= yearsForecasted; year++) {
					for (int r = 0; r < nResourceCols; r++) 
						i++;
				}
				
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
	protected HarvestSolution findHarvestSolution() {
		return super.findHarvestSolution();
	}
}
