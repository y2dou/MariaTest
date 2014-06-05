package mariaprototype.environmental.visualization.style;

import java.awt.Color;

import mariaprototype.visualization.style.FuzzyLayerStyle;

public class FertilityLayerStyle extends FuzzyLayerStyle {
	private Color hue1 = Color.WHITE;
	private Color hue2 = Color.GREEN;

	@Override
	protected Color getColor0() {
		return hue1;
	}

	@Override
	protected Color getColor1() {
		return hue2;
	}

}
