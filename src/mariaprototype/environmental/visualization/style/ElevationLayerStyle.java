package mariaprototype.environmental.visualization.style;

import java.awt.Color;

import mariaprototype.visualization.style.TransformedFuzzyLayerStyle;

public class ElevationLayerStyle extends TransformedFuzzyLayerStyle {
	public ElevationLayerStyle() {
		// set the range of elevations to (-1, 10)
		setRange(-4, 34);
	}
	
	public void setRange(int lower, int upper) {
		super.setRange(lower, upper);
	}
	
	@Override
	protected Color getColor0() {
		return Color.RED;
	}

	@Override
	protected Color getColor1() {
		return Color.GREEN;
	}
}
