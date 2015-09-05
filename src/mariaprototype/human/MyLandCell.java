package mariaprototype.human;

import java.util.HashMap;

import mariaprototype.SimpleAgent;
import mariaprototype.environmental.LandCell;
import mariaprototype.environmental.LandUse;

/**
 * <p>
 * A wrapper around LandCell containing beliefs and knowledge of land cover and underlying soil of a LandCell and 
 * its neighbours.
 * 
 * <p>
 * The use of this class' components is optional, depending on the decision-making method of the agent.
 * 
 * @author Raymond Cabrera
 *
 */
public class MyLandCell extends SimpleAgent {
	private LandCell cell;
	
	/**
	 * Current land use as intended by the cell's owner.
	 */
	private LandUse landUse;
	
	/**
	 * Immediately previous land use.
	 */
	private LandUse lastLandUse;	// belief (contrasted with truth in LandCell)
	private int yearsSinceLast;
	private int yearsSinceMaintained;
	
	/**
	 * A map storing counts of neighbouring land uses.
	 */
	private HashMap<LandUse, Double> neighbourLandUses = new HashMap<LandUse, Double>();
	
	/**
	 * Flag marking land cells ready for development.
	 */
	private boolean toDevelop = false;
	
	private double distanceFromHouse;
	private double distanceToWater;
	
	public MyLandCell(LandCell c, HouseholdAgent h) {
		this.cell = c;
		
		// derive land use from current coverage
		yearsSinceLast = 100;
		lastLandUse = c.getLastLandUse();
		if (cell.getAcaiAge() > 0) {
			landUse = LandUse.ACAI;	// intense acai
		} else if (cell.getFieldsAge() > 0) {
			landUse = LandUse.FIELDS;
		} else if (cell.getForestAge() > 0) {
			landUse = LandUse.FOREST;
		} else if (cell.getManiocGardenAge() > 0) {
			//landUse = LandUse.FIELDS;
			//shouldn't this be maniocGarden? July 16, 2014
			landUse=LandUse.MANIOCGARDEN;
		} else if (cell.getSecondarySuccessionAge() > 0) {
			landUse = LandUse.FIELDS;
		}
		
		distanceFromHouse = h.getLocation().getDistanceTo(c.getX(), c.getY());
		distanceToWater = c.getDistanceToWater();
	}
	
	public void maintain() {
		cell.maintain();
		yearsSinceMaintained = 0;
	}
	
	public void age() {
		yearsSinceLast++;
		yearsSinceMaintained++;
	}
	
	public double getDistanceFromHouse() {
		return distanceFromHouse;
	}
	
	public double getDistanceToWater () {
		return distanceToWater;
	}
	
	public LandUse getLandUse() {
		return landUse;
	}
	
	public void setLandUse(LandUse landUse) {
		lastLandUse = this.landUse;
		cell.setLastLandUse(landUse);
		
		this.landUse = landUse;
		yearsSinceLast = 0;
	}
	
	public LandUse getLastLandUse() {
		return lastLandUse;
	}
	
	public int getYearsSinceMaintained() {
		return yearsSinceMaintained;
	}
	
	public void setYearsSinceMaintained(int yearsSinceMaintained) {
		this.yearsSinceMaintained = yearsSinceMaintained;
	}

	public int getYearsSinceLast() {
		return yearsSinceLast;
	}

	public void setYearsSinceLast(int yearsSinceLast) {
		this.yearsSinceLast = yearsSinceLast;
	}

	public LandCell getCell() {
		return cell;
	}
	
	public boolean isToDevelop() {
		return toDevelop;
	}
	
	public void setToDevelop(boolean toDevelop) {
		this.toDevelop = toDevelop;
	}
	
	public void addNeighbourLandUse(LandUse neighbourLandUse, double weight) {
		try {
			neighbourLandUses.put(neighbourLandUse, neighbourLandUses.get(neighbourLandUse) + weight);
		} catch (NullPointerException e) {
			neighbourLandUses.put(neighbourLandUse, weight);
		}
	}
	
	public Double getNeighbourLandUseCounts(LandUse nLandUse) {
		Double count = neighbourLandUses.get(nLandUse);
		return count == null ? 0 : count; 
	}
	
	public void clearNeighbourLandUses() {
		neighbourLandUses.clear();
	}
}
