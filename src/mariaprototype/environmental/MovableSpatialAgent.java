package mariaprototype.environmental;

import repast.simphony.space.grid.GridDimensions;

public class MovableSpatialAgent extends SpatialAgent {
	@Override
	public int[] getIntArray() {
		// since dimensions may change, recalculate intArray every time
		return dimensions.toIntArray(intArray);
	}
	
	public void setDimensions(GridDimensions d) {
		this.dimensions = d;
	}
}
