package mariaprototype.visualization.style;

import java.awt.Color;
import java.awt.Paint;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.valueLayer.ValueLayer;
import repast.simphony.visualization.visualization2D.style.ValueLayerStyle;

/**
 * <code>ValueLayerStyle</code> which displays a single colour with transparency 
 * based on the input value. 
 * 
 * For Maria, this class is intended to style the intensity of single land uses 
 * such that multiple land uses can be merged into a single display, though other
 * applications are possible.
 * 
 * @author Raymond Cabrera
 *
 */
public class FuzzyAlphaLayerStyle implements ValueLayerStyle {
	protected ValueLayer fuzzyLayer;
	protected Color baseColor;
	
	// lookup table
	public FuzzyAlphaLayerStyle(Color baseColor) {
		this.baseColor = baseColor;
	}

	public void addValueLayer(ValueLayer layer) {
		this.fuzzyLayer = layer;
	}

	public int getRed(double... coordinates) {
		return baseColor.getRed();
	}
	
	public int getGreen(double... coordinates) {
		return baseColor.getGreen();
	}
	
	public int getBlue(double... coordinates) {
		return baseColor.getBlue();
	}

	public float getCellSize() {
		Parameters p = RunEnvironment.getInstance().getParameters();
		return (Float) p.getValue("cellsize");
	}
	
	protected int getRGBA(double v) {
		byte alpha = ((Double) (v * 255d)).byteValue();

		// impose transparency based on the value of the layer
		int rgba = (alpha << 24) | (baseColor.getRGB() & 0xFFF);
		return rgba;
	}

	public Paint getPaint(double... coordinates) {
		Double v = fuzzyLayer.get(coordinates);
		return new Color(getRGBA(v), true);
	}

	

}
