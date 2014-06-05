package mariaprototype.human.landallocation;

import java.util.Comparator;

import mariaprototype.environmental.LandUse;
import mariaprototype.human.MyLandCell;

public class AcaiGoodnessComparator implements Comparator<MyLandCell> {
	public AcaiGoodnessComparator() {
	}
	
	@Override
	public int compare(MyLandCell o1, MyLandCell o2) {
		// close to house, close to water, close to other acai (in that order?)
		double w1 = -getWeight(o1);
		double w2 = -getWeight(o2);
		
		return Double.compare(w1, w2);
	}
	
	private double getWeight(MyLandCell c) {
		return -c.getDistanceFromHouse() + c.getNeighbourLandUseCounts(LandUse.ACAI) * 100000000000d;
		// return c.getNeighbourLandUseCounts(LandUse.ACAI) * 100000;
	}
}
