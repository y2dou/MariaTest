package mariaprototype.environmental.visualization.style;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Stroke;

import mariaprototype.environmental.LandCell;
import mariaprototype.environmental.WaterCell;
import mariaprototype.visualization.ColorUtility;

public class LandscapeStyle2D extends DefaultCellStyle2D {
	private static final Color TAN = new Color(205, 133, 63);

	@Override
	public Paint getPaint(Object o){

		if (o instanceof WaterCell)
			return Color.BLUE;
		else if (o instanceof LandCell) {
			LandCell c = (LandCell) o;
			return ColorUtility.blend(TAN, Color.GREEN, c.getFertility());
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
