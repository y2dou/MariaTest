package mariaprototype.environmental;

import mariaprototype.SimpleAgent;
import repast.simphony.space.grid.GridDimensions;

public abstract class SpatialAgent extends SimpleAgent {
	// fixed variables
	protected GridDimensions dimensions;
	
	// permanently store the int array: it's calculated/used often, so avoid reallocating heap space
	// since SpatialAgent can't move, 
	protected int[] intArray;
	
	protected SpatialAgent(int... coordinates) {
		this(new GridDimensions(coordinates));
	}
	
	protected SpatialAgent(GridDimensions dimensions) {
		this.dimensions = dimensions;
		intArray = dimensions.toIntArray(null);
	}
	
	public int getX() {
		return dimensions.getWidth();
	}
	
	public int getY() {
		return dimensions.getHeight();
	}
	
	public int[] getIntArray() {
		return intArray;
	}
	
	public GridDimensions getDimensions() {
		return dimensions;
	}
}
