package mariaprototype.human;

import java.util.Comparator;

import mariaprototype.environmental.LandUse;

public class NeighbourLandCellComparator implements Comparator<MyLandCell> {
	private final LandUse landUse;
	
	public NeighbourLandCellComparator(LandUse landUse) {
		this.landUse = landUse;
	}
	
	@Override
	public int compare(MyLandCell o1, MyLandCell o2) {
		return o1.getNeighbourLandUseCounts(landUse).compareTo(o2.getNeighbourLandUseCounts(landUse));
	}
}
