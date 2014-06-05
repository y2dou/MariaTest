package mariaprototype.environmental.visualization.style;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Stroke;

import mariaprototype.environmental.LandCell;
import mariaprototype.environmental.WaterCell;
import mariaprototype.human.HouseholdAgent;

public class LandTenureStyle2D extends DefaultCellStyle2D {
	@Override
	public Paint getPaint(Object o){

		if (o instanceof WaterCell)
			return Color.BLUE;
		else if (o instanceof LandCell) {
			LandCell c = (LandCell) o;
			
			HouseholdAgent h = c.getLandHolder();
			if (h != null)
				return h.getColor();
			
			return Color.darkGray;
		}

		return null;
	}
	
	

	// Don't paint the outline of the shape.
	@Override
	public Stroke getStroke(Object o){
		return null;
	}
}
