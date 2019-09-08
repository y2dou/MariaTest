package mariaprototype.environmental;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


import com.jidesoft.utils.Base64.InputStream;
import com.thoughtworks.xstream.io.path.Path;

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
	protected Range<Double> distanceToWaterRange;
	//Yue, Sep 10, 2015
	private boolean outputImages;
	
	protected int width;
	protected int height;
	protected double originx;
	protected double originy;
	protected double cellsize;
	
	private static final double NO_TICKS = -1.0d;
	Map<Double, Double> climateLists;
	//private double acaiClimateIndicator;
	
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
	
	private void init()  {
		Parameters p = RunEnvironment.getInstance().getParameters();
		outputImages = (Boolean) p.getValue("outputEnabled") && (Boolean) p.getValue("outputImagesAsFiles");
		
		// XXX: if value is less than zero, we need to read a configuration
		//      file.
		climateLists = new HashMap<Double, Double> ();
		   
		if ((Double) p.getValue("climateIndicator") > 0.0d) {		 
			climateLists.put(NO_TICKS, (Double) p.getValue("climateIndicator"));	
	//		System.out.println(p.getValue("climateIndicator"));
		}
		if ((Double) p.getValue("climateIndicator") <= 0.0 ){
			//pay attention, the number written in parameter file has to be -1.01 or below...
			//if it's -1.0 it won't work...god knows why
	//	else {
//			System.out.println("how about here");
			//XXX: take all values in the file and load them into the map
			String fileName = "auxdata/prices/climateIndicatorTest.txt";
			
			String line= null;
			
			try {
				FileReader fileReader = new FileReader(fileName);
				BufferedReader bufferedReader = 
	                new BufferedReader(fileReader);
                int ticks = 1;
	            while((line = bufferedReader.readLine()) != null) {
	            	climateLists.put(new Double(ticks), new Double(line.trim()));
	          
	            	++ticks;
	      //    	System.out.println(ticks+" "+new Double(line.trim()));
	            }   
//				System.out.println(climateLists.toString());
	            bufferedReader.close();
	           
			}
			catch (FileNotFoundException ex){
				System.out.println("Exception");
			}
			
			catch(IOException ex) {
				System.out.println("Exception 2");
			}		
				}
		 System.out.println("  Climate Indicator: "+climateLists.toString());
		
		landCells = new HashSet<LandCell>();
	}

	public void finalReport() {
		if (!(Boolean) RunState.getInstance().getFromRegistry("invalidRun")) {
			if (outputImages)
			//	ImageUtility.createPNG((GridValueLayer) getValueLayer("Elevation Field"), elevationRange, new File((String) RunState.getInstance().getFromRegistry("path") + "/elevation.png"));
			ImageUtility.createPNG((GridValueLayer) getValueLayer("Distance to Water"), distanceToWaterRange, new File((String) RunState.getInstance().getFromRegistry("path") + "/distanceToWater.png"));
	//		System.out.println("Water Layer has been drawn");
			//ImageUtility.createPNG(G, range, file)
			
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
	
	public double getClimateIndicator(double ticks) {
		double result;
		if (climateLists.containsKey(NO_TICKS))	{
			result = ((Double) climateLists.get(NO_TICKS)).doubleValue();
		} else {
			result = ((Double) climateLists.get(ticks)).doubleValue();
		}
		return result;
	}
}
