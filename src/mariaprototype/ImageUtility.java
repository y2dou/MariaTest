package mariaprototype;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;

import mariaprototype.environmental.LandscapeCell;
import mariaprototype.human.HouseholdAgent;
import mariaprototype.human.MyLandCell;
import repast.simphony.space.Dimensions;
import repast.simphony.valueLayer.GridValueLayer;

public class ImageUtility {
	private static int nodata = -9999;
	
	public static void createPNG(GridValueLayer grid, Range<Double> range, File file) {
		Dimensions d = grid.getDimensions();
		int width = (int) d.getWidth();
		int height = (int) d.getHeight();
		
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = img.createGraphics();
		
		g.setColor(Color.white);
		g.fillRect(0, 0, width, height);
		
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if ((int) grid.get(i, j) != nodata) {
					int val = (int) (255d * (grid.get(i, j) - range.getLower()) / (range.getUpper() - range.getLower()));
					g.setColor(new Color(val, val, val));
					g.drawRect(i, height - j - 1, 1, 1);
				}
			}
		}
		try {
			ImageIO.write(img, "png", file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		g.dispose();
	}
	
	public static void createLandUsePNG(Iterable<HouseholdAgent> agents, int width, int height, File file) {
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = img.createGraphics();
		
		g.setColor(Color.white);
		g.fillRect(0, 0, width, height);
		
		Iterator<HouseholdAgent> iter = agents.iterator();
		while (iter.hasNext()) {
			HouseholdAgent h = iter.next();
			
			Iterator<MyLandCell> cellIter = h.getTenure().values().iterator();
			while (cellIter.hasNext()) {
				MyLandCell c = cellIter.next();
				g.setColor(c.getLandUse().getColor());
				g.drawRect(c.getCell().getX(), height - c.getCell().getY() - 1, 1, 1);
			}
			
		}
		
		try {
			ImageIO.write(img, "png", file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		g.dispose();
	}
	
	public static void createLandCoverPNGs(Iterable<LandscapeCell> cells, int width, int height, File file) {
		
	}
}
