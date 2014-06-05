package mariaprototype.human;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Stroke;


import repast.simphony.visualization.visualization2D.style.DefaultStyle2D;

public class HouseholdAgentStyle2D extends DefaultStyle2D {
	public static Color getColor(Double id) {
		// TODO hash the land holder ID to produce a hue between 0 and 1
		return Color.getHSBColor(id.floatValue() / 100f, 1, 1);
	}
	
	public static Color getColor(Integer id) {
		// TODO hash the land holder ID to produce a hue between 0 and 1
		return Color.getHSBColor(id.floatValue() / 100f, 1, 1);
	}

	@Override
	public Paint getPaint(Object object) {
		if (object instanceof HouseholdAgent) {
			return getColor(((HouseholdAgent) object).getID());
		}
		return null;
	}

	@Override
	public Stroke getStroke(Object object) {
		return null;
	}
	
	
}
