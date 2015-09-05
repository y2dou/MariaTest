package mariaprototype.environmental;

import java.util.Random;

import mariaprototype.FuzzyUtility;
import mariaprototype.MariaPriorities;
import mariaprototype.human.HouseholdAgent;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunState;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.grid.Grid;

public class LandCell extends LandscapeCell {
	/*
	 * In general:
	 * 
	 * Density and intensity are set by the land manager.
	 * Health and age are set by the transition() step.
	 * 
	 */
	// I'm going to add a distance decay function for growing acai and manioc in upland and floodplain
	//to show the difference.
	
	// upland land uses
	//		capoeira (upland), fallowed land
	private double ssDensity = 0;
	private int ssAge = 0;
	
	// misc land uses
	//		mata
	private double forestDensity = 0;			// decreases w/ extraction, clearing
	private int forestAge = 0;				// proxy for age
	
	// 		rocas
	private double fieldsDensity = 0;
	private double fieldsHealth = 0;
	private int fieldsAge = 0;
	
	// floodplain land uses
	
	// quintais
	private double maniocgardenDensity = 0;
	private double maniocgardenHealth = 0;
	private int maniocgardenAge = 0;
	
	private double acaiDensity = 0;
	private double acaiHealth = 0;
	private int acaiAge = 0;
	
	private double capoeiraDensity = 0;
	
	private double acaiYield = 0;
	private double intenseAcaiYield = 0;
	private double gardenYield = 0;
	private double timberYield = 0;
	
	private LandUse lastLandUse;				// truth
	// current land use and age of land use is knowledge or belief, not necessarily truth
	
	private double fertility = 1;
	private HouseholdAgent landHolder;
	
	private boolean isMaintained = false;
	private double yearDeforested = -9999;
	
	private boolean upland = false;
	private double disToWater = 0;
	//Yue, sept 5, 2015
	public LandCell(Context<SpatialAgent> context, Grid<SpatialAgent> grid, int x, int y) {
		this(context, grid, x, y, context.getValueLayer("Elevation Field").get(x, y), context.getValueLayer("Distance to Water").get(x, y));
	}
	
	public LandCell(Context<SpatialAgent> context, Grid<SpatialAgent> grid, int x, int y, double elevation, double distanceToWater) {
		super(context, grid, x, y, elevation, distanceToWater);
		
		((EnvironmentalContext) context).landCells.add(this);
		
		// calculate initial land uses
		forestDensity = 1;
		forestAge = 100;
		
		lastLandUse = LandUse.FOREST;
		disToWater = this.getDistanceToWater();
	}

	@ScheduledMethod(start = 1, interval = 1, priority = MariaPriorities.CLIMATOLOGY)
	public void clearStatus() {
		isMaintained = false;
	}
	
	@ScheduledMethod(start = 1, interval = 1, priority = MariaPriorities.BIOPHYSICAL)
	public void transition() {
		// model land transitions separate from agent knowledge and decision-making
		
		age();
		
		if (forestDensity < 1 && yearDeforested < 0) {
			yearDeforested = RunState.getInstance().getScheduleRegistry().getModelSchedule().getTickCount();
		}
		
		// transition fallow to SS, SS to forest based on age
		if (ssAge >= 10) {
			forestDensity += ssDensity;
			ssDensity = 0;
			forestAge = ssAge;
			ssAge = 0;
		}
		
		// landscape transition assuming swidden cultivation
		//	note: agents must set density and intensity manually
		
		// lower fertility for land uses which are too old
		// or, lower fertility by some amount
		fertility -= fieldsDensity * RandomHelper.getDistribution("fieldFertilityDiscount").nextDouble();		// assuming no cover
		// fertility -= acaiDensity * RandomHelper.nextDoubleFromTo(0.01, 0.02);
		// FIXME: reduce acai fertility only if it's not weeded
		// fertility -= acaiIntensity * acaiDensity * RandomHelper.nextDoubleFromTo(0.01, 0.02);
		
		// fertility -= maniocgardenDensity * RandomHelper.nextDoubleFromTo(0.4, 0.5);
		fertility -= maniocgardenDensity * RandomHelper.getDistribution("maniocFertilityDiscount").nextDouble();
		
		// capoeira increases fertility 
		//why? Yue, Sept 4, 2015. if capoeira is upland, then it can only increase manioc's fertility not acai
		// regenerate fertility: fallow after cattle half as quickly as for other land uses
		if (lastLandUse.equals(LandUse.FIELDS)) {
			fertility += (ssDensity + forestDensity) / 10;
			fertility += capoeiraDensity / 10;
		} else {
			fertility += (ssDensity + forestDensity) / 5;
			fertility += capoeiraDensity / 5;
		}
		
		fertility = FuzzyUtility.constrain(fertility);
		
		// set health of land cover based on fertility
		if (fertility <= 0.05) {
			fieldsHealth -= 0.5;
			//acaiHealth -= 0.5; // 
			maniocgardenHealth -= 0.5;
		} else {
			acaiHealth = 1;// 0.75 + fertility / 4d;
			maniocgardenHealth = 1;// 0.75 + fertility / 4d;
		}
		
		// unless... they didn't maintain the plot
		//oh my goodness, acaiDensity has always been zero!!How can there be any acai yield!!
		//Yue Nov 11, 2014
		if (!isMaintained) {
		//	acaiDensity=fieldsDensity;
			if (acaiDensity > 0.8) {
				acaiDensity -= 0.2;
				capoeiraDensity += 0.2;
				FuzzyUtility.constrain(capoeiraDensity);
			} else {
				
			}
			
			if (maniocgardenDensity > 0.5) {
			//	maniocgardenDensity -= 0.5; Yue, Sept 3, 2015, I want to increase manioc's yield
				maniocgardenDensity -= 0.4;
				capoeiraDensity += 0.5;
				lastLandUse = LandUse.MANIOCGARDEN;
			} else {
				capoeiraDensity += maniocgardenDensity;
				maniocgardenDensity = 0;
			}
		}
		
		fieldsHealth = FuzzyUtility.constrain(fieldsHealth);
		acaiHealth = FuzzyUtility.constrain(acaiHealth);
		maniocgardenHealth = FuzzyUtility.constrain(maniocgardenHealth);
		
		acaiYield = acaiDensity * acaiHealth * RandomHelper.getDistribution("acaiYield").nextDouble();
		//acaiYield has been always 0, and there's no place it has been set not zero, so let's try to test this.
		//acaiYield =  acaiHealth * RandomHelper.getDistribution("acaiYield").nextDouble();

	//	if (acaiYield>0) System.out.println("L167 "+"acai yield "+acaiYield+" health "+acaiHealth);
		if (acaiDensity>0) { //System.out.println( " L168  acaiYield="+acaiYield);
			
		}
		
		// intenseAcaiYield = acaiDensity * acaiHealth * 15000d; // * some constant
		
	//	intenseAcaiYield = acaiDensity * acaiHealth * 14000d;
		intenseAcaiYield = acaiDensity * acaiHealth * 10000d;
		//try this Feb 05, 2015 Yue
	//	if(intenseAcaiYield>0) System.out.println("Line 179 "+intenseAcaiYield);
	//	gardenYield = maniocgardenDensity * maniocgardenHealth * RandomHelper.getDistribution("maniocYield").nextDouble();
		
		gardenYield = maniocgardenDensity * maniocgardenHealth * RandomHelper.getDistribution("maniocYield").nextDouble();
	//		if (gardenYield>0) System.out.println("L174 "+"this.getDistanceToWater() "+this.getDistanceToWater());
		
		timberYield = forestDensity * 5000d;
		
		//below is the distance decay
		//for acai: f(d)= e^(-(d/500)^2)
		if (acaiYield > 0)
		{
    //    System.out.println("Sept 5 AcaiYield="+acaiYield);
		acaiYield = acaiYield * 1.2 * Math.exp(-Math.pow(disToWater/50, 2));
	//	System.out.println("Sept 5 AcaiCell Distance="+disToWater);
		} 
		
		if (gardenYield > 0) {
	//		System.out.println("ManiocYield="+gardenYield);
			gardenYield = gardenYield * Math.pow(disToWater/20, 0.5);
	//		System.out.println("Sept 5 ManiocYield Distance="+disToWater);
		}
	}
	
	private void age() {
		if (ssDensity > 0)
			ssAge++;
		else
			ssAge = 0;
		
		if (forestDensity > 0)
			forestAge++;
		else
			forestAge = 0;
		
		if (fieldsDensity > 0) 
			fieldsAge++;
		else
			fieldsAge = 0;
		
		if (maniocgardenDensity > 0)
			maniocgardenAge++;
		else
			maniocgardenAge = 0;
		
		if (acaiDensity > 0) 
			acaiAge++;
		else
			acaiAge = 0;
	}
	
	public void finalReport() {
		
	}
	
	public void maintain() {
		isMaintained = true;
	}
	
	public double getIntenseAcaiYield() {
		// calculate kgPerHa (or kgPerCell)
		
		return intenseAcaiYield;
	}
	
	public double getAcaiYield() {
		return acaiYield;
	}
	
	public double getGardenYield() {
		return gardenYield;
	}
	
	public double getTimberYield() {
		return timberYield;
	}

	public double getSecondarySuccessionDensity() {
		return ssDensity;
	}

	public double getSecondarySuccessionAge() {
		return ssAge;
	}

	public double getForestDensity() {
		return forestDensity;
	}

	public int getForestAge() {
		return forestAge;
	}

	public double getAcaiDensity() {
		return acaiDensity;
	}

	public double getAcaiHealth() {
		return acaiHealth;
	}
	
	public int getAcaiAge() {
		return acaiAge;
	}
	
	public double getCapoeiraDensity() {
		return capoeiraDensity;
	}
	
	public double getFertility() {
		return fertility;
	}

	public double getFieldsDensity() {
		return fieldsDensity;
	}

	public double getFieldsHealth() {
		return fieldsHealth;
	}
	
	public int getFieldsAge() {
		return fieldsAge;
	}
	
	public HouseholdAgent getLandHolder() {
		return landHolder;
	}

	public double getManiocGardenDensity() {
		return maniocgardenDensity;
	}

	public double getManiocGardenHealth() {
		return maniocgardenHealth;
	}
	
	public int getManiocGardenAge() {
		return maniocgardenAge;
	}

	public void setSecondarySuccessionDensity(double ssDensity) {
		this.ssDensity = ssDensity;
	}

	public void setForestDensity(double forestDensity) {
		this.forestDensity = forestDensity;
	}

	public void setAcaiDensity(double acaiDensity) {
		this.acaiDensity = acaiDensity;
	}
	
	public void setCapoeiraDensity(double capoeiraDensity) {
		this.capoeiraDensity = capoeiraDensity;
	}

	public void setFieldsDensity(double fieldsDensity) {
		this.fieldsDensity = fieldsDensity;
	}

	public void setManiocGardenDensity(double maniocGardenDensity) {
		this.maniocgardenDensity = maniocGardenDensity;
	}
	
	public void setLandHolder(HouseholdAgent landHolder) {
		this.landHolder = landHolder;
	}
	
	public void setLastLandUse(LandUse lastLandUse) {
		this.lastLandUse = lastLandUse;
	}
	
	public LandUse getLastLandUse() {
		return lastLandUse;
	}
	
	public double getYearDeforested() {
		return yearDeforested;
	}
	
/*	public boolean isUpland() {
		return upland;
	}

	public void setUpland( ) {
		if (disToWater>=200)
		this.upland = true;
		else 
			this.upland = false;
	}*/
}
