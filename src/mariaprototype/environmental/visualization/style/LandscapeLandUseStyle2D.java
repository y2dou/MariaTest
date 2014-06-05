package mariaprototype.environmental.visualization.style;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Stroke;

import mariaprototype.environmental.LandCell;
import mariaprototype.environmental.LandUse;
import mariaprototype.environmental.WaterCell;
import mariaprototype.visualization.ColorUtility;

public class LandscapeLandUseStyle2D extends DefaultCellStyle2D {
	@Override
	public Paint getPaint(Object o){

		if (o instanceof WaterCell)
			return Color.BLUE;
		else if (o instanceof LandCell) {
			LandCell c = (LandCell) o;
			
			// blend all land use colors
			double cumulative = 0;
			Color color = Color.white;
			
			color = ColorUtility.blend(color, LandUse.FOREST.getColor(), c.getForestDensity() / (cumulative + c.getForestDensity()));
			cumulative += c.getForestDensity();
			
			color = ColorUtility.blend(color, LandUse.FALLOW.getColor(), c.getSecondarySuccessionDensity() / (cumulative + c.getSecondarySuccessionDensity()));
			cumulative += c.getSecondarySuccessionDensity();
			
			color = ColorUtility.blend(color, LandUse.ACAI.getColor(), c.getAcaiDensity() / (cumulative + c.getAcaiDensity()));
			cumulative += c.getAcaiDensity();
			
			color = ColorUtility.blend(color, LandUse.MANIOCGARDEN.getColor(), c.getManiocGardenDensity() / (cumulative + c.getManiocGardenDensity()));
			cumulative += c.getManiocGardenDensity();
			
			return color;
		}
/*
		else if (o instanceof LandUseCell) {
			LandUseCell c = (LandUseCell) o;
			Color col = Color.WHITE;
			
			if (c instanceof AcaiCell) {
				col = ColorUtility.blend(TAN, Color.GREEN, ((AcaiCell) c).getIntensity());
			} else if (c instanceof UplandFallowCell) {
				col = TAN;
			}
			
			return col;
		}
*/
		return null;
	}

	// Don't paint the outline of the shape.
	@Override
	public Stroke getStroke(Object o){
		return null;
	}
}
