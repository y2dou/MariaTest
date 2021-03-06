package mariaprototype.environmental;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.util.HashMap;
import java.util.Map;

import mariaprototype.ImageUtility;
import mariaprototype.MariaPriorities;
import mariaprototype.Range;
import mariaprototype.human.StaticMarketAgent;
import repast.simphony.context.Context;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.valueLayer.GridValueLayer;
import cern.jet.random.AbstractDistribution;

public class EnvironmentalContextBuilder implements ContextBuilder<SpatialAgent> {
	private int width;
	private int height;
	private double originx;
	private double originy;
	private double cellsize;
    
	
	private int nodata;
	
	
	@Override
	public Context<SpatialAgent> build(Context<SpatialAgent> c) {
		EnvironmentalContext context;
		if (c == null) {
			context = new EnvironmentalContext("EnvironmentalContext");
		} else {
			try {
				context = (EnvironmentalContext) c;
			} catch (ClassCastException e) {
				context = new EnvironmentalContext("EnvironmentalContext");
			}
		}
		
		// load default parameters (width and height may be overridden)
		Parameters p = RunEnvironment.getInstance().getParameters();
		width = (Integer) p.getValue("width");
		height = (Integer) p.getValue("height");
		cellsize = (Double) p.getValue("cellsize");
		
	//	System.out.println (cellsize);
/*		double climateMultiplier = (Double) p.getValue("climateMultiplier");
		double acaiClimateMultiplier = (Double) p.getValue("acaiClimateMultiplier");
		double maniocClimateMultiplier = (Double) p.getValue("maniocClimateMultiplier");
		//add this climate multiplier as a coefficient for crop yield for climatic scenarios.
		//the idea is for climatic hazards, it will be 0.8(for instance) for the acai yield and manioc yield. 
		
		// load data: prices
		Map<LandUse, InputStream> climateLists = new HashMap<LandUse, InputStream>();
		StaticMarketAgent staticMarketAgent = new StaticMarketAgent();*/
		
	/*	
		try {
			double staticAcaiPrice = (Double) p.getValue("acaiPrice");
			
			
			if (staticAcaiPrice >= 0) {
				staticMarketAgent.setPrice(LandUse.ACAI, staticAcaiPrice);
				System.out.println ("acaiPrice =" + staticAcaiPrice);
			} else {
				priceLists.put(LandUse.ACAI, new FileInputStream("auxdata/prices/acai.prices.test.txt"));
				
				double multiplier = (Double) p.getValue("acaiMultiplier");
				if (multiplier <= 0)
					multiplier = globalMultiplier;
				
				priceMultipliers.put(LandUse.ACAI, multiplier);
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
*/
		setUpRandomDistributions();
		
		// create elevation layer
		GridValueLayer elevationField;
		BufferedInputStream stream = null;
		Range<Double> elevationRange = new Range<Double>(0d, 0d);
		try {
			stream = new BufferedInputStream(new FileInputStream("gisdata/paricatuba/elevation.asc"));
			elevationField = loadFieldFromStream(context, stream, "Elevation Field", elevationRange);
		} catch (IOException e) {
			e.printStackTrace();
			elevationField = createElevationFieldFromRandom(context);
		} finally {
			try {
				if (stream != null) 
					stream.close();
			} catch (IOException e) {}
		}
		p.setValue("width", width);
		p.setValue("height", height);
		p.setValue("originx", originx);
		p.setValue("originy", originy);
		p.setValue("cellsize", cellsize);
		
		context.width = width;
		context.height = height;
		context.originx = originx;
		context.originy = originy;
		context.cellsize = cellsize;
		context.elevationRange = elevationRange;
		
		Range<Double> distanceToWaterRange = new Range<Double>(0d, 0d);
		GridValueLayer isLandField, distanceToWaterField;
		try {
			stream = new BufferedInputStream(new FileInputStream("gisdata/paricatuba/land.asc"));
		//	isLandField = loadFieldFromStream(context, stream, "is Land");
			isLandField = loadFieldFromStream(context, stream, "is Land", distanceToWaterRange);
		} catch (IOException e) {
			e.printStackTrace();
			// if there is no land file, set elevation threshold to 0
			isLandField = createField(context, "is Land");
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					isLandField.set(elevationField.get(i, j) > 0 ? 1 : 0, i, j);
				}
			}
		} finally {
			try {
				if (stream != null) 
					stream.close();
			} catch (IOException e) {}
		}
		
		try {
			stream = new BufferedInputStream(new FileInputStream("gisdata/paricatuba/disttowater.asc"));
			distanceToWaterField = loadFieldFromStream(context, stream, "Distance to Water", distanceToWaterRange);
		} catch (IOException e) {
			e.printStackTrace();
			// if there is no distance to water, use a blank field
			distanceToWaterField = createField(context, "Distance to Water", false);
		} finally {
			try {
				if (stream != null) 
					stream.close();
			} catch (IOException e) {}
		}
		
		// create landscape
		Grid<SpatialAgent> landscapeGrid = GridFactoryFinder.createGridFactory(null).createGrid("Landscape Grid",
						context,
						new GridBuilderParameters<SpatialAgent>(
								new repast.simphony.space.grid.StrictBorders(),
								new SimpleGridAdder<SpatialAgent>(), false, width, height));
		context.addProjection(landscapeGrid);
       
		/*
		GridValueLayer slopeField = createField(context, "Slope Field");
		for (int i = 1; i < width - 1; i++) {
			for (int j = 1; j < height - 1; j++) {
				double slope;
				
				double vGrad = (slopeField.get(i-1,j-1) + 2d * slopeField.get(i,j-1) + slopeField.get(i+1,j-1) 
					- (slopeField.get(i-1,j+1) + 2d * slopeField.get(i,j+1) + slopeField.get(i+1,j+1))) / (8 * cellsize);
				double hGrad = (slopeField.get(i-1,j-1) + 2d * slopeField.get(i-1,j) + slopeField.get(i-1,j+1) 
					- (slopeField.get(i+1,j-1) + 2d * slopeField.get(i+1,j) + slopeField.get(i+1,j+1))) / (8 * cellsize);
				
				slope = Math.atan(Math.sqrt(vGrad * vGrad + hGrad * hGrad)) * 57.29582;
				
				slopeField.set(slope, i, j);
			}
		}
		*/
		
		// populate land use cells
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				double elevation = elevationField.get(i, j);
				double distanceToWater = distanceToWaterField.get(i, j);
				
				if (Double.isNaN(elevation))
					continue;
				
				boolean isLand = isLandField.get(i, j) > 0;
				
				if (isLand) {
					new LandCell(context, landscapeGrid, i, j, elevation, distanceToWater);
				} else {
					new WaterCell(context, landscapeGrid, i, j, elevation);
				}
				
			}
		}
		
		

		ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
		ScheduleParameters params;
		//params = ScheduleParameters.createRepeating(1, 1, MariaPriorities.CLIMATOLOGY);
		//schedule.schedule(params, this, "climatology");
		
		params = ScheduleParameters.createAtEnd(MariaPriorities.FINAL_REPORT);
		schedule.schedule(params, context, "finalReport");
		
		params = ScheduleParameters.createAtEnd(MariaPriorities.CLEANUP);
		schedule.schedule(params, context, "cleanup");
		
		return context;
	}
	
	private void setUpRandomDistributions() {
		RandomHelper.registerDistribution("elevationField", RandomHelper.createUniform(-1, 10));
		RandomHelper.registerDistribution("fieldFertilityDiscount", RandomHelper.createUniform(0.1, 0.2));    // 5-10 years of use
		RandomHelper.registerDistribution("maniocFertilityDiscount", RandomHelper.createUniform(0.45, 0.55)); // 2 years of use
		
	/*	RandomHelper.registerDistribution("acaiYield", RandomHelper.createUniform(180d, 360d));
		RandomHelper.registerDistribution("maniocYield", RandomHelper.createUniform(280d, 560d));
		RandomHelper.registerDistribution("intenseAcaiYield", RandomHelper.createUniform(360d, 720d)); */
		RandomHelper.registerDistribution("acaiYield", RandomHelper.createUniform(30d, 85d));
	//	RandomHelper.registerDistribution("maniocYield", RandomHelper.createUniform(47d, 68d));
		RandomHelper.registerDistribution("maniocYield", RandomHelper.createUniform(68d, 100d));
		RandomHelper.registerDistribution("intenseAcaiYield", RandomHelper.createUniform(144d, 275d)); 
		//hectares * 10000d / (cellsize * cellsize), the yield here is for per cell, which is 0.0225;
		//from the article:agriculture intensification, economic identity, and shared invisibility in Amazonian 
		// peasantry: Caboclos and colonists in comparative perspective, 
		//acai yield range: 
		//unmanaged: 1390 kg/ha/yr  --> 31.275 
		//intermediately managed: 2600--3780 kg/ha/yr  --> 58.5 - 85.05
		//intensively managed: 6400-12200 kg/ha/yr  --> 144 - 274.5
		//from the article: the diversity of bitter manioc cultivation in a whitewater amazonian landscape
		//manioc yield range: (table 4)
		//oxisol: 2,250 kg/ha, ultisol: 2100 kg/ha -->47.25
		//floodplain: 3000 kg/ha --> 67.5
		
		//Yue, Sept 3, 2015, i want to increase manioc yield;
	}
	
	private GridValueLayer createField(EnvironmentalContext context, String fieldName) {
		return createField(context, fieldName, true);
	}
	
	private GridValueLayer createField(EnvironmentalContext context, String fieldName, boolean dense) {
		GridValueLayer field = new GridValueLayer(fieldName, dense,
				new repast.simphony.space.grid.StrictBorders(), width, height);
		context.addValueLayer(field);
		return field;
	}
	
	private GridValueLayer createElevationFieldFromRandom(EnvironmentalContext context) {
		GridValueLayer elevationField = createField(context, "Elevation Field");
		AbstractDistribution elevationDist = RandomHelper.getDistribution("elevationField");
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				elevationField.set(elevationDist.nextDouble(), i, j);
			}
		}
		return elevationField;
	}
	
	private GridValueLayer loadFieldFromStream(EnvironmentalContext context, InputStream stream, String fieldName) throws IOException {
		return loadFieldFromStream(context, stream, fieldName, null);
	}
	
	private GridValueLayer loadFieldFromStream(EnvironmentalContext context, InputStream stream, String fieldName, Range<Double> range) throws IOException {
		int type;
		BufferedReader r = new BufferedReader(new InputStreamReader(stream));
		StreamTokenizer st = new StreamTokenizer(r);

		st.parseNumbers();
		st.wordChars('_', '_');
		st.eolIsSignificant(false);
		st.lowerCaseMode(true);
		// cols
		type = st.nextToken();
		type = st.nextToken();
		width = (int) st.nval;
		// rows
		type = st.nextToken();
		type = st.nextToken();
		height = (int) st.nval;
		// xllcorner
		type = st.nextToken();
		type = st.nextToken();
		originx = st.nval; 
		// yllcorner
		type = st.nextToken();
		type = st.nextToken();
		originy = st.nval;
		// cellSize
		type = st.nextToken();
		type = st.nextToken();
		cellsize = st.nval;
		
		GridValueLayer field = createField(context, fieldName);
		
		// termx and termy
		// double termx = Math.floor(originx) + cellSize * width;
		// double termy = Math.floor(originy) + cellSize * height;
		// missing
		type = st.nextToken();
		if (type == StreamTokenizer.TT_NUMBER) {
			st.pushBack();
			nodata = -9999;
		} else {
			type = st.nextToken();
			nodata = (int) st.nval;
		}
		st.ordinaryChars('E', 'E');

		double d1;
		
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		for (int i = height - 1; i >= 0; i--) {
			for (int j = 0; j < width; j++) {
				st.nextToken();
				d1 = st.nval;
				
				// handle exponents
				type = st.nextToken();
				if (type != StreamTokenizer.TT_NUMBER
						&& type != StreamTokenizer.TT_EOF) {
					if ((st.sval.charAt(0) == 'e' || st.sval.charAt(0) == 'E') && st.sval.length() > 1) {
						d1 = d1 * Math.pow(10.0, Double.valueOf(st.sval.substring(1)));
					} else {
						type = st.nextToken();
						d1 = d1 * Math.pow(10.0, st.nval);
					}
				} else {
					st.pushBack();
				}

				if ((int) d1 != nodata) {
					field.set(d1, j, i);
					
					min = Math.min(min, d1);
					max = Math.max(max, d1);
				} else
					field.set(Double.NaN, j, i);
			}
		}
		
		if (range != null) {
			range.setLower(min);
			range.setUpper(max);
		}
		
		return field;
	}
}
