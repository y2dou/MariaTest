package mariaprototype.environmental;

import repast.simphony.space.grid.GridDimensions;

public class House extends SpatialAgent {
	public House(int... coordinates) {
		super(coordinates);
	}
	
	public House(GridDimensions dimensions) {
		super(dimensions);
	}
}
