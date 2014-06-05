package mariaprototype.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import mariaprototype.Point;

import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.ProjectedCRS;

import repast.simphony.space.gis.Geography;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

public class GeographyUtility<T> {
	// GIS attributes
	private Geography<T> geography;
	private GeometryFactory factory = new GeometryFactory(); 
	private double falseEasting;
	private double falseNorthing;
	
	// raster attributes
	//private double originx;
	//private double originy;
	private double cellsize;
	
	public GeographyUtility(Geography<T> geography, File projectionFile, double cellsize) throws FileNotFoundException, FactoryException, IOException {
		this.geography = geography;
		ProjectedCRS crs = (ProjectedCRS) CRS.parseWKT(new BufferedReader(new FileReader(projectionFile)).readLine());;
		falseEasting = crs.getConversionFromBase().getParameterValues().parameter("false_easting").doubleValue();
		falseNorthing = crs.getConversionFromBase().getParameterValues().parameter("false_northing").doubleValue();
		
		//this.originx = originx;
		//this.originy = originy;
		this.cellsize = cellsize;
	}
	
	public GeographyUtility(Geography<T> geography, CoordinateReferenceSystem projectedCRS, double cellsize) {
		this.geography = geography;
		ProjectedCRS crs = (ProjectedCRS) projectedCRS;
		falseEasting = crs.getConversionFromBase().getParameterValues().parameter("false_easting").doubleValue();
		falseNorthing = crs.getConversionFromBase().getParameterValues().parameter("false_northing").doubleValue();
		
		//this.originx = originx;
		//this.originy = originy;
		this.cellsize = cellsize;
	}
	
	public GeometryFactory getGeometryFactory() {
		return factory;
	}
	
	public Point getRasterXY(T object) {
		Point point;
		com.vividsolutions.jts.geom.Point pt = (com.vividsolutions.jts.geom.Point) geography.getGeometry(object);
		int x = (int) (-0.5 - (falseEasting - pt.getX()) / cellsize);
		int y = (int) ((pt.getY() - falseNorthing) / cellsize - 0.5);
		point = new Point(x, y);
		
		return point;
	}
	
	public Coordinate getCoordinates(int x, int y, double originx, double originy) {
		return getCoordinates(x, y, originx, originy, cellsize);
	}
	
	public Coordinate getCoordinates(int x, int y, double originx, double originy, double cellSize) {
		double easting = (x + 0.5) * cellSize + originx;
		double northing = (y + 0.5) * cellSize + originy;
		return new Coordinate(easting, northing);
	}
	
}
