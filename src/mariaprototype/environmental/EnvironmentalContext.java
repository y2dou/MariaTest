package mariaprototype.environmental;

import java.io.File;
import java.sql.Connection;
import java.util.HashSet;
import java.util.Set;

import mariaprototype.ImageUtility;
import mariaprototype.Range;
import mariaprototype.database.Database;
import repast.simphony.context.DefaultContext;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.environment.RunState;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.grid.Grid;
import repast.simphony.valueLayer.GridValueLayer;

public class EnvironmentalContext extends DefaultContext<SpatialAgent> {
	protected Range<Double> elevationRange;
	
	private boolean outputImages;
	
	protected int width;
	protected int height;
	protected double originx;
	protected double originy;
	protected double cellsize;
	
	protected Set<LandCell> landCells;
	
	public EnvironmentalContext() {
		super();
		init();
	}
	
	public EnvironmentalContext(Object name, Object typeID) {
		super(name, typeID);
		init();
	}

	public EnvironmentalContext(Object name) {
		super(name);
		init();
	}
	
	private void init() {
		Parameters p = RunEnvironment.getInstance().getParameters();
		outputImages = (Boolean) p.getValue("outputEnabled") && (Boolean) p.getValue("outputImagesAsFiles");
		
		landCells = new HashSet<LandCell>();
	}

	public void finalReport() {
		if (!(Boolean) RunState.getInstance().getFromRegistry("invalidRun")) {
			if (outputImages)
				ImageUtility.createPNG((GridValueLayer) getValueLayer("Elevation Field"), elevationRange, new File((String) RunState.getInstance().getFromRegistry("path") + "/elevation.png"));
			
			Connection conn = (Connection) RunState.getInstance().getFromRegistry("connection");
			
			Database.getInstance().logLandCells(conn, landCells);
		}
	}
	
	public void cleanup() {
		// destroy grids
		
		if (RunEnvironment.getInstance().isBatch()) {
			try {
				clear();
			} catch (NullPointerException e) {}
		}
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public double getOriginx() {
		return originx;
	}
	
	public double getOriginy() {
		return originy;
	}
	
	public double getCellsize() {
		return cellsize;
	}

}
