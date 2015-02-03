

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
 * @author DOU Yue
 * this is the chayanov module, in which households consider both income and leisure time, as their optimal goal.
 * utility=income^alpha*leisure^(1-alpha)
 *
 */
public class ChayanovHouseholdAgent extends SimpleHouseholdAgent {

		// plan
		protected FeasibleAllocations feasibleAllocations = new FeasibleAllocations();
		
	
		
		public ChayanovHouseholdAgent() {
			super();
		}

		public ChayanovHouseholdAgent(int id) {
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
		//	String s = null;
			//s.toString();

			resetActions();
	        
			// consider job offers, move people to town if desired
			ArrayList<Person> eligibleMembers = new ArrayList<Person>();
		//	System.out.println("Linear don't make plan1");
			int males = 0; // adults
			int females = 0;
		//	System.out.println("Linear don't make plan2");
			if (!jobOffers.isEmpty()) {
				
				ListIterator<Person> members = familyMembers.listIterator();
			//	System.out.println(familyMembers.listIterator());
			//	System.out.println("Linears don't make plan3");
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
			else {this.setWage(0);}
			
			while (!jobOffers.isEmpty()&&!eligibleMembers.isEmpty()) {
				// rank members by age, younger eligible members leave first
				Collections.sort(eligibleMembers, new Comparator<Person>() {
					@Override
					public int compare(Person o1, Person o2) {
					//	return new Integer(o1.getAge()/o1.getEducation()).compareTo(o2.getAge()/o2.getEducation());
						return new Integer(o1.getEducation()).compareTo(o2.getEducation());
					}
					
				});
				JobOffer o = jobOffers.remove(0);
				// find a suitable candidate
			    int j=eligibleMembers.size();
					Person p = eligibleMembers.remove(j-1);
					feasibleAllocations.getEmployables().put(p, o);
					this.setWage(o.getWage());
					//let the most educated person get the job;
			//	    System.out.println("555");
			/*		if (p.isFemale()) {
						if (females > 1) {
							feasibleAllocations.getEmployables().put(p, o);
							System.out.println("666");
							break;
						}
					} else {
						if (males > 1) {
							feasibleAllocations.getEmployables().put(p, o);
							System.out.println("777");
							break;
						}
					}*/
				
				
			}
			
			// iterate through portfolio: identify actions
			for (MyLandCell c : tenure.values()) {
				c.age();

				LandCell cell = c.getCell();
			//	System.out.println("Land Cell+"+cell.getForestAge());
				if (c.getLandUse() == LandUse.ACAI) {
					if (c.getYearsSinceMaintained() >= 3) {
						c.setLandUse(LandUse.FOREST);
						c.setYearsSinceLast(c.getYearsSinceMaintained());
						c.setYearsSinceMaintained(0);
						cell.setForestDensity(FuzzyUtility.constrain(cell.getForestDensity() + cell.getAcaiDensity()));
						cell.setAcaiDensity(0);
						feasibleAllocations.getToHarvestAcai().add(c);
				//		System.out.println("Let's try here: "+feasibleAllocations.getToHarvestAcai().size());
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
				//	System.out.println("This is Forest+"+c.getLandUse());
					if (cell.getForestAge() > 3) {
						feasibleAllocations.getToHarvestTimber().add(c);
						feasibleAllocations.getToHarvestAcai().add(c);
			//			System.out.println("This is Primary Forest");
			//			System.out.println("size: " + feasibleAllocations.getToHarvestTimber().size());
					}

					if (c.getYearsSinceLast() > 5) {
						feasibleAllocations.getToPossiblyDevelop().add(c);
					}
				} else if (c.getLandUse() == LandUse.FALLOW) {
					final LandUse lastLandUse = c.getLastLandUse();
				//	System.out.println("Land Use"+c.getLandUse());
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
					{  feasibleAllocations.getToPossiblyDevelop().add(c);
				//	   System.out.println("Linear output: Forest");
					   break;
					}
//					feasibleAllocations.getToPossiblyDevelop().add(c);
					//	break;
	                // Yue made this change June 06, 2014
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
			
//			System.out.println("Line 246 HarvestAcai size = "+feasibleAllocations.getToHarvestAcai().size());
			// TODO: take over unmanaged property, if projected labour and capital allow
		}
		
		protected void resetActions() {
			feasibleAllocations.reset();
		}
		
		protected double getExpectedPrice(LandUse crop) {
			return marketPrices.get(crop);
			
		//	System.out.println(marketPrices.);
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
			//			System.out.println("Harvest for manioc"+feasibleAllocations.getToHarvestHousegarden().size());
						if (cell.getAcaiDensity() > 0)
							feasibleAllocations.getToHarvestIntenseAcai().add(c);
					}
				} else if (c.getLandUse() == LandUse.FOREST) {
					if (cell.getForestAge() > 3) {
						feasibleAllocations.getToHarvestTimber().add(c);
						feasibleAllocations.getToHarvestAcai().add(c);
					//	System.out.println("HarvestSolution for Forest"+feasibleAllocations.getToHarvestTimber().size());
					}
				} else if (c.getLandUse() == LandUse.FALLOW) {
					final LandUse lastLandUse = c.getLastLandUse();
					switch (lastLandUse) {
					case ACAI:
						feasibleAllocations.getToHarvestAcai().add(c);
				//		System.out.println("HarvestAcai="+feasibleAllocations.getToHarvestAcai().size());
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
			//System.out.println("___Try to find the best solution1");
			
			// get list of agents available for recall back to the household
			// need to move keyset to ordered list for consistency
			List<NetworkedUrbanAgent> recallSolutions = new LinkedList<NetworkedUrbanAgent>();
			recallSolutions.addAll(linkedHouseholds.keySet());
			
			
			try {
			//	System.out.println("before lp...");
				LpSolve lp = LpSolve.makeLp(0, ncols);
			//from here 	
			//	System.out.println("size of lp" + lp.getLp());
				if (lp.getLp() == 0) {
					retVal = 1;
			//		System.out.println("it is NOT running!");
				}
		
				// name the LP, set columns
				if (retVal == 0) {
				//	System.out.println("it is running!");
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
				//	System.out.println("it is running!");
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
				//	System.out.println("lp ColName:"+lp.getColName(getID()));
				}
		
				if (retVal == 0) {
					// set up upper bounds on optimizing variables
					lp.setBounds(1, 0, feasibleAllocations.getToHarvestAcai().size());
					lp.setBounds(2, 0, feasibleAllocations.getToHarvestIntenseAcai().size());
					lp.setBounds(3, 0, feasibleAllocations.getToHarvestHousegarden().size());
					lp.setBounds(4, 0, feasibleAllocations.getToHarvestTimber().size());
			//		System.out.println("feasibleAllocations=acai "+feasibleAllocations.getToHarvestAcai().size()+
			//				",intenseAcai"+feasibleAllocations.getToHarvestIntenseAcai().size()+",housegarden "+
			//				feasibleAllocations.getToHarvestHousegarden().size());
			//have checked, this is correct, it can return feasible Allocations. problem is acaiYield;		
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
						//	System.out.println("line417="+c.getCell().getAcaiYield());
							if(c.getCell().getAcaiYield()>0) {
						//	System.out.println("c.getCell().getAcaiYield() "+c.getCell().getAcaiYield());
								}
							acaiYield += c.getCell().getAcaiYield();
						//	System.out.println("line421="+acaiYield); (checked), good
						}
						acaiYield /= (double) feasibleAllocations.getToHarvestAcai().size();	
						
					}
				//	System.out.println("L427 acaiYield"+acaiYield); (checked)
					
					double intenseacaiYield = 0;
					if (!feasibleAllocations.getToHarvestIntenseAcai().isEmpty()) {
						for (MyLandCell c : feasibleAllocations.getToHarvestIntenseAcai()) {
					//		System.out.println("intensifyAcai"+c.getCell().getAcaiYield());
							intenseacaiYield += c.getCell().getIntenseAcaiYield();
						}
						intenseacaiYield /= (double) feasibleAllocations.getToHarvestIntenseAcai().size();
					//	System.out.println("Intensify acai Yield="+intenseacaiYield);
					}
				//	System.out.println("L438 Intense acaiYield"+intenseacaiYield); (checked)
					double gardenYield = 0;
					if (!feasibleAllocations.getToHarvestHousegarden().isEmpty()) {
						for (MyLandCell c : feasibleAllocations.getToHarvestHousegarden()) {
					//		System.out.println("intensify Manioc="+c.getCell().getGardenYield());
							gardenYield += c.getCell().getGardenYield();
						}
						gardenYield /= (double) feasibleAllocations.getToHarvestHousegarden().size();
					//	System.out.println("Garden Yield="+gardenYield); (checked)
					}
					
					double timberYield = 0;
					if (!feasibleAllocations.getToHarvestTimber().isEmpty()) {
						for (MyLandCell c : feasibleAllocations.getToHarvestTimber()) {
							timberYield += c.getCell().getTimberYield();
						}
						timberYield /= (double) feasibleAllocations.getToHarvestTimber().size();
					//	System.out.println("++++++Timber Yield is"+timberYield);
					}
					
					colno[j] = 1;
					row[j++] = acaiYield * getExpectedPrice(LandUse.ACAI);
				//	System.out.println("L460 AcaiYield="+acaiYield);
				//	System.out.println("L461 AcaiPrice="+getExpectedPrice(LandUse.ACAI));
				//	System.out.println("L462 Expected Acai Profit:"+row[0]);
					colno[j] = 2;
					row[j++] = intenseacaiYield * getExpectedPrice(LandUse.ACAI);
			//		System.out.println("L465 intense AcaiYield="+intenseacaiYield);
			//		System.out.println("Expected Intensify Acai Profit:"+row[1]);
					colno[j] = 3;
					row[j++] = gardenYield * getExpectedPrice(LandUse.MANIOCGARDEN);
			//		System.out.println("L468 GardenYield="+gardenYield);
			//		System.out.println("L469 GardenPrice="+getExpectedPrice(LandUse.MANIOCGARDEN));
			//		System.out.println("L470 Expected Garden Profit:"+row[2]);
				//	System.out.println("Expected garden Profit:"+row[j-1]);
					colno[j] = 4;
					row[j++] = timberYield * getExpectedPrice(LandUse.FOREST);
			//		System.out.println("Expected Timber Profit:"+row[j-1]);
					
					Iterator<NetworkedUrbanAgent> recallIter = recallSolutions.iterator();
					while (recallIter.hasNext()) {
						NetworkedUrbanAgent e = recallIter.next();
						colno[j] = j + 1;
						
						row[j++] = e.getWage();
					}
		
					/* set the objective in lpsolve */
					lp.setObjFnex(j, row, colno);
				//	System.out.println("*****it is running!");
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
				//	System.out.println("L519 harvest recommendation for acai "+(row[0]+row[1])); 
					
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
	    //    System.out.println("How about here4");

			// http://lpsolve.sourceforge.net/5.5/formulate.htm#Java
	//***************************************************************************************************//
	// changed now, by Yue Nov 04, 2014.
			/*when making new development, households consider income and leisure, 
			 * so it's a cobb-douglas utility objective function, which is non-linear as well
			 * and there is no way that we can solve it by lpsolve
			 * max(Utility)=max{ [cashT+wage+f_agri(l_a)]^a*[Lmax-lw-l_a]^b}
			 * f_agri(l_a)=(price-immediateCost)*yield*l_a
			 */
	       double cashT=this.getPension();
	       double wage=0;
	       double lw=0;
	       double lmax=this.getLabour();
	       //to get wage income and wage labour;
	       List<Entry<Person, JobOffer>> employableSolutions = new LinkedList<Entry<Person, JobOffer>>(); 
	       employableSolutions.addAll(feasibleAllocations.getEmployables().entrySet());
	       Iterator<Entry<Person, JobOffer>> iter=employableSolutions.iterator();
	       if( !employableSolutions.isEmpty()) {
	    	   while(iter.hasNext()) {
	    		   Entry<Person, JobOffer> e = iter.next();
	    		   wage = wage + e.getValue().getWage();
	    	//	   System.out.println("householdID="+this.getID()+"  wage="+wage);
	    		   //sum of wages from all members that get a job offer;
	    		   lw = lw + e.getKey().getLabour();
	    		   //sum of labour from all eligible members.
	    	                          }
	                                            }
	    	   
	       double utility=0;
	       double agriincome=0;
	       double income=0; 
	       double leisure=0;
	       double lacai=0;
	       double  lmanioc=0;
	       double lmaintainacai=0;
	       double lmaintainmanioc=0;
	       double utilityMax=0;
	       double[] labourArray;
	       labourArray=new double[4];
	       double[] land;
		   land=new double[4];
		   for(int i=0;i<4;i++){
			   labourArray[i]=0;
			   land[i]=0;
		   }

		   double[] landUpperBounds;
		   landUpperBounds=new double[4];
		   landUpperBounds[0]=feasibleAllocations.getToIntensifyAcai().size();
		   landUpperBounds[1]=feasibleAllocations.getToManiocGarden().size();
		   landUpperBounds[2]=feasibleAllocations.getToMaintainAcai().size();
		   landUpperBounds[3]=feasibleAllocations.getToMaintainManiocGarden().size();
		 //  System.out.println(landUpperBounds[0]+" "+landUpperBounds[1]+" "+landUpperBounds[2]+" "+landUpperBounds[3]);
		 //  System.out.println("feasiblePossibleDevelop="+feasibleAllocations.getToPossiblyDevelop().size());
		   /*try to find the best fit of lacai, lmanioc,..
	        * maximize [sum(price-immediate cost)*yield*l]
	        * constrains
	        * sum(cost*plots)<capital
	        * sum(labour)<lmax-lwage
	        * sum(plots)<property 
	        */
	      
	       for (lacai=0;lacai<=lmax-lw;lacai+=acaiLabour){
	    	   for(lmanioc=0;lmanioc<=lmax-lw-lacai;lmanioc+=maniocLabour){
	    		   for(lmaintainmanioc=0;lmaintainmanioc<=lmax-lw-lacai-lmanioc;lmaintainmanioc+=maintainManiocLabour){
	    			  for(lmaintainacai=0;lmaintainacai<=lmax-lw-lacai-lmanioc-lmaintainmanioc;lmaintainacai+=maintainAcaiLabour) 
	    			 {
	    				  land[0]=lacai/acaiLabour;
	   				      land[1]=lmanioc/maniocLabour;
	   				      land[2]=lmaintainacai/maintainAcaiLabour;
	   				      land[3]=lmaintainmanioc/maintainManiocLabour;
	   				   
	    				  double cost = acaiCost*capitalMultiplier*land[0]
	    				                 + maniocCost*capitalMultiplier*land[1]
	    				                 + maintainAcaiCost*capitalMultiplier*land[2]
	    				                 + maintainManiocCost*capitalMultiplier*land[3];
	    				    				   
	    				   agriincome=(15000d*getExpectedPrice(LandUse.ACAI) - acaiCost)*land[0]
	    				              +(5000d * getExpectedPrice(LandUse.MANIOCGARDEN) - maniocCost)*land[1]
	    				              +(15000d * getExpectedPrice(LandUse.ACAI) - maintainAcaiCost)*land[2]
	    				              +(5000d * getExpectedPrice(LandUse.MANIOCGARDEN) - maintainManiocCost)*land[3];
	    				   
	    				  
	    				   if (cost<=capital){
	    					   if(land[0]<=landUpperBounds[0]){
	    						   if(land[1]<=landUpperBounds[1]) {
	    							   if(land[2]<=landUpperBounds[2]){
	    								   if(land[3]<=landUpperBounds[3]){
	    									   income=agriincome+wage+cashT;
	    			    					   leisure=(lmax-lw-lacai-lmanioc-lmaintainacai-lmaintainmanioc);
	    			    					   utility=Math.pow(income, this.getAlpha())*Math.pow(leisure, 1-this.getAlpha());
	    			    				//	   utility=income;
	    								   }
	    							   }
	    						   }
	    						   
	    					   }
	    				   }
	    						  
	    				   //when constrain meets, calculate utility in  this if statement;
	    				   if(utility>utilityMax){
	    						   utilityMax=utility;
	    						   labourArray[0]=lacai;
	    						   labourArray[1]=lmanioc;
	    						   labourArray[2]=lmaintainacai;
	    						   labourArray[3]=lmaintainmanioc;
	    					
	    				//		   System.out.println(lacai+" "+lmanioc+ "  "+lmaintainacai+" "+lmaintainmanioc);
	    				//		   System.out.println("utility="+utility+" utilityMax="+utilityMax);
	    					   } //if(utility>utlityMax)
	    				   
	    			   }//first for
	    		   }// second for
	    		   
	    	   }//third for
	       }//last for
		 //  System.out.println("hhdid="+this.getID());
//		   System.out.println(labourArray[0]+" "+labourArray[1]+ "  "+labourArray[2]+" "+labourArray[3]);
	//System.out.println("labour at wage="+lw);
	//System.out.println(" household utility="+utilityMax);

	        
	       DevelopmentSolution recommendation= new DevelopmentSolution();
	       recommendation.setAcai(labourArray[0]/acaiLabour);
	    //   recommendation.setAcai(10);
	       recommendation.setGarden(labourArray[1]/maniocLabour);
	     //  recommendation.setGarden(2);
	       
			recommendation.setMaintainAcai(labourArray[2]/maintainAcaiLabour);
			recommendation.setMaintainGarden(labourArray[3]/maintainManiocLabour);
		
		//	System.out.println("recommendation "+recommendation.getAcai()+" "+recommendation.getGarden()+" "
		//			+recommendation.getMaintainAcai()+" "+recommendation.getMaintainGarden());
			
			Map<Person, JobOffer> employable = new HashMap<Person, JobOffer>();
			Iterator<Entry<Person, JobOffer>> itera = employableSolutions.iterator();
			while (itera.hasNext()) {
				Entry<Person, JobOffer> e = itera.next();
				employable.put(e.getKey(), e.getValue());
				
			}
			recommendation.setEmploy(employable);
			
			
			List<NetworkedUrbanAgent> recallSolutions = new LinkedList<NetworkedUrbanAgent>();
			recallSolutions.addAll(linkedHouseholds.keySet());
			List<NetworkedUrbanAgent> recall = new LinkedList<NetworkedUrbanAgent>();
			Iterator<NetworkedUrbanAgent> recallIter = recallSolutions.iterator();
			while (recallIter.hasNext()) {
				NetworkedUrbanAgent a = recallIter.next();
				//if (row[i++] == 0) { // 1 indicates DO NOT recall
					recall.add(a);
				
			}

			recommendation.setRecall(recall);

			return recommendation;

			
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
	  //      System.out.println("How about here");
			for (MyLandCell c : feasibleAllocations.getToForestFallow()) {
				if (!forestFallow(c))
					break;
			}

			resetLabour();
	   //     System.out.println("How about here2");

			// slash and burn (again, no labour)
			for (MyLandCell c : feasibleAllocations.getToFallow()) {
				if (!fallow(c))
					break;
			}
	    //    System.out.println("How about here3");

			DevelopmentSolution solution = findDevelopmentSolution();
	   //     System.out.println("How about here4");

			if (solution != null) {
				// FIXME: do non-integer solutions (partial plots)

				// send off people
				Iterator<Entry<Person, JobOffer>> offeredIter = solution.getEmploy().entrySet().iterator();
				if (offeredIter.hasNext()) {
					Entry<Person, JobOffer> e = offeredIter.next();
					send(e.getKey(), e.getValue());
				}
		//        System.out.println("How about here5");

				// get people back
				Iterator<NetworkedUrbanAgent> recallIter = solution.getRecall().iterator();
				if (recallIter.hasNext()) {
					recall(recallIter.next(), "action");
				}
				
				resetLabour();
		//        System.out.println("How about here6");
				
				double acai = solution.getAcai();
				double manioc = solution.getGarden();
				double maintainAcai = solution.getMaintainAcai();
				double maintainGardens = solution.getMaintainGarden();
				
		//       System.out.println("How about here7 "+acai+" "+manioc+" "+maintainAcai+" "+maintainGardens);

				// order shouldn't matter; it's optimal!
				// then again, spatial land allocation isn't
				while (acai >= 1) {
					double acaiIntensification = intensifyAcai();
					if (acaiIntensification <= 0)
						break;
					
					acai -= acaiIntensification;
				}
		  //      System.out.println("How about here8");

				while (manioc >= 1) {
					if (expandHousegarden()) manioc -= 1;
					
					if (feasibleAllocations.getToManiocGarden().isEmpty())
						break;
				}
		  //      System.out.println("How about here9");
				
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

		   //     System.out.println("How about here10");
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
			
			double income=0;
			if (solution != null) {
				// get labour back
				Iterator<NetworkedUrbanAgent> recallIter = solution.getRecall().iterator();
				if (recallIter.hasNext()) {
					recall(recallIter.next(), "harvest");
				}
				
				resetLabour();
				resetHarvestCount();

				double acai = solution.getAcai();
			//	System.out.println("acai="+acai);
				double intenseAcai = solution.getIntenseAcai();
			//	System.out.println("IntenseAcai="+intenseAcai);
				double gardens = solution.getGardens();
			//	System.out.println("Gardens="+gardens);
				double timber = solution.getTimber();
			//	System.out.println("L896 solution.acai="+(acai+intenseAcai));
				while (acai >= 1) {
			//		System.out.println("acai="+acai);
					MyLandCell acaiCell = feasibleAllocations.getToHarvestAcai().remove(0);
					LandCell cell = acaiCell.getCell();

					double yield = cell.getAcaiYield();
					labour -= harvestAcaiLabour	* labourMultiplier;
					capital += yield * getActualPrice(LandUse.ACAI);
					income +=yield * getActualPrice(LandUse.ACAI);
					//Yue, Nov 5,2014
					acaiYield += yield;
				//	System.out.println("acaiYield="+acaiYield);
					acai -= 1;
				}
				
				while (intenseAcai >= 1) {
					MyLandCell acaiCell = feasibleAllocations.getToHarvestIntenseAcai().remove(0);
					LandCell cell = acaiCell.getCell();

					double yield = cell.getIntenseAcaiYield();
					labour -= harvestAcaiLabour	* labourMultiplier;
					capital += yield * getActualPrice(LandUse.ACAI);
					income +=yield * getActualPrice(LandUse.ACAI);
					//Yue, Nov 5, 2014
					acaiYield += yield;
				//	System.out.println("acaiYield="+acaiYield);
					intenseAcai -= 1;
				}
				
				while (gardens >= 1) {
					MyLandCell myCell = feasibleAllocations.getToHarvestHousegarden().remove(0);
					LandCell cell = myCell.getCell();

					double yield = cell.getGardenYield();
					
					labour -= harvestManiocLabour * labourMultiplier;
					capital += yield * getActualPrice(LandUse.MANIOCGARDEN);
					income += yield * getActualPrice(LandUse.MANIOCGARDEN);
					//Yue, Nov 5,2014
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
					income +=yield * getActualPrice(LandUse.FOREST);
					//Yue
					timberYield += yield;
					
					timber -= 1;
				}
				
				
				//capital += this.cashTran;
				
			
				
				
			//	this.jobOffers
			//	System.out.println(this.getID()+"linear capital="+capital+"=cashTran="+cashTran);
				//this.jobOffers.
				
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
