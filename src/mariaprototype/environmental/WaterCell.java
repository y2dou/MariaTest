package mariaprototype.environmental;

import java.awt.Color;

import mariaprototype.MariaPriorities;
import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.grid.Grid;
import repast.simphony.valueLayer.GridValueLayer;

public class WaterCell extends LandscapeCell {
	private double shrimp = 0;			// shrimp in the water
	private double shrimpEff = 0;		// % harvest yield of population: can be modified by shrimping farms
	private Color color;
	
	public WaterCell(Context<SpatialAgent> context, Grid<SpatialAgent> grid, int x, int y, double elevation) {
		super(context, grid, x, y);

		this.elevation = elevation;
		shrimp = 1;
		shrimpEff = 0.2;
	}
	
	public WaterCell(Context<SpatialAgent> context, Grid<SpatialAgent> grid, int x, int y) {
		super(context, grid, x, y);

		GridValueLayer vlElevation = (GridValueLayer) context.getValueLayer("Elevation Field");
		elevation = vlElevation.get(x, y);
		
		shrimp = 1;
		shrimpEff = 0.2;
	}

	@ScheduledMethod(start = 1, interval = 1, priority = MariaPriorities.BIOPHYSICAL)
	public void step() {
		// TODO: parameterize shrimp regrowth rate
		/*
		if (shrimp < 1) {
			shrimp *= 1.10; // 10% regrowth rate (net of emigration)
			
			int xD = RandomHelper.nextIntFromTo(-1, 1);
			int yD = RandomHelper.nextIntFromTo(-1, 1);
			
			// 5% immigration rate from random adjacent cell or bonus regrowth rate
			//   prevents permanent harvest of shrimp 
			Context<LandscapeCell> context = ContextUtils.getContext(this);
			Grid grid = (Grid) context.getProjection("Landscape Grid");
			shrimp += 0.05 * ((WaterCell) grid.getObjectAt(getX() + xD, getY() + yD)).getShrimp();
		}
		*/
	}
	
	public double farmShrimp() {
		// TODO: implement shrimp farming
		throw new UnsupportedOperationException("Shrimp farming unsupported.");
	}
	
	/*
	private double getShrimp() {
		return shrimp;
	}
	*/
	
	public double harvestShrimp() {
		double harvest = shrimpEff * shrimp;
		shrimp -= harvest;
		return harvest;
	}
	private void WaterCellColor (Color color) {
		this.color = color;
	}
	
	public Color getColor() {
		return color;
	}
}
