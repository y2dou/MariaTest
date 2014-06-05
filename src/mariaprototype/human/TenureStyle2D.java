package mariaprototype.human;

import java.awt.Paint;


import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.valueLayer.ValueLayer;
import repast.simphony.visualization.visualization2D.style.ValueLayerStyle;

public class TenureStyle2D implements ValueLayerStyle {

	protected ValueLayer tenureLayer;
	
	public void addValueLayer(ValueLayer layer) {
		this.tenureLayer = layer;
	}

	public int getBlue(double... coordinates) {
		return 0;
	}

	public float getCellSize() {
		Parameters p = RunEnvironment.getInstance().getParameters();
		return (Float) p.getValue("cellsize");
	}

	public int getGreen(double... coordinates) {
		return 0;
	}

	public Paint getPaint(double... coordinates) {
		Double v = tenureLayer.get(coordinates);

		return HouseholdAgentStyle2D.getColor(v);
	}

	public int getRed(double... coordinates) {
		return 0;
	}

}
