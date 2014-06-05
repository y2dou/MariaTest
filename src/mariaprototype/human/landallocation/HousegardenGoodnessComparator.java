package mariaprototype.human.landallocation;

import java.util.Comparator;

import mariaprototype.environmental.LandUse;
import mariaprototype.human.MyLandCell;

public class HousegardenGoodnessComparator implements Comparator<MyLandCell> {
	public HousegardenGoodnessComparator() {

	}
	
	@Override
	public int compare(MyLandCell o1, MyLandCell o2) {
		// close to house, close to water, close to other acai (in that order?)
		double w1 = -getGoodness(o1);
		double w2 = -getGoodness(o2);
		
		return Double.compare(w1, w2);
	}
	
	private double getGoodness(MyLandCell c) {
		return -c.getDistanceFromHouse() + c.getNeighbourLandUseCounts(LandUse.MANIOCGARDEN) * 10000000000d;
	}
}
