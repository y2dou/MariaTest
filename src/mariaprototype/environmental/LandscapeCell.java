package mariaprototype.environmental;

import repast.simphony.context.Context;
import repast.simphony.space.grid.Grid;

public abstract class LandscapeCell extends SpatialAgent {
	// fixed variables
	protected double elevation;
	protected double distanceToWater; //Yue Sept 3, 2015
	
	protected LandscapeCell(Context<SpatialAgent> context, Grid<SpatialAgent> grid, int x, int y) {
		this(context, grid, x, y, 0, 0);
	}
	
/*	protected LandscapeCell(Context<SpatialAgent> context, Grid<SpatialAgent> grid, int x, int y, double elevation) {
		super(x, y);
		this.elevation = elevation;
		
		context.add(this);
		
	    // move the cell to its position on the patch grid
		grid.moveTo(this, x, y);
	}*/
	
	protected LandscapeCell(Context<SpatialAgent> context, Grid<SpatialAgent> grid, int x, int y, double elevation, double distanceToWater) {
		super(x, y);
		this.elevation = elevation;
		this.distanceToWater = distanceToWater;
		context.add(this);
		
	    // move the cell to its position on the patch grid
		grid.moveTo(this, x, y);
	}
	public double getElevation() {
		return elevation;
	}
	public double getDistanceToWater(){
		return distanceToWater;
	}
}
