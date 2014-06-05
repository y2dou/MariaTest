package mariaprototype.visualization.style;

import java.awt.Color;
import java.awt.Paint;

import mariaprototype.visualization.ColorUtility;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.valueLayer.ValueLayer;
import repast.simphony.visualization.visualization2D.style.ValueLayerStyle;

/**
 * ValueLayerStyle for fuzzy values as a gradient between two colours. Concrete classes
 * must implement <code>getColor0()</code> and <code>getColor1()</code> to define the base
 * colours for fuzzy values 0 and 1, respectively.
 * 
 * @author Raymond Cabrera
 * 
 */
public abstract class FuzzyLayerStyle implements ValueLayerStyle {
	
	protected ValueLayer fuzzyLayer;
	
	/**
	 * Return colour representing 0.
	 * 
	 * @return Base colour for fuzzy value 0.
	 */
	protected abstract Color getColor0();
	
	/**
	 * Return colour representing 1.
	 * 
	 * @return Base colour for fuzzy value 1.
	 */
	protected abstract Color getColor1();

	public void addValueLayer(ValueLayer layer) {
		this.fuzzyLayer = layer;
	}
	
	public float getCellSize() {
		Parameters p = RunEnvironment.getInstance().getParameters();
		return ((Double) p.getValue("cellsize")).floatValue();
	}
	
	protected Double getFuzzyValue(double... coordinates) {
		return fuzzyLayer.get(coordinates);
	}

	public int getRed(double... coordinates) {
		return ColorUtility.blendInt(getColor0().getRed(), getColor1().getRed(), getFuzzyValue(coordinates));
	}
	
	public int getGreen(double... coordinates) {
		return ColorUtility.blendInt(getColor0().getGreen(), getColor1().getGreen(), getFuzzyValue(coordinates));
	}
	
	public int getBlue(double... coordinates) {
		return ColorUtility.blendInt(getColor0().getBlue(), getColor1().getBlue(), getFuzzyValue(coordinates));
	}

	public Paint getPaint(double... coordinates) {
		// mix hue1 and hue2
		return ColorUtility.blend(getColor0(), getColor1(), getFuzzyValue(coordinates));
	}

	

}
