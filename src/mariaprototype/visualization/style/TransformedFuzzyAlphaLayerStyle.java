package mariaprototype.visualization.style;

import java.awt.Color;

import mariaprototype.FuzzyUtility;


/**
 * Similar to <code>FuzzyAlphaLayerStyle</code>, except that input values are transformed 
 * on the fly.
 * 
 * @author Raymond Cabrera
 * @see mariaprototype.visualization.style.TransformedFuzzyLayerStyle
 */
public class TransformedFuzzyAlphaLayerStyle extends FuzzyAlphaLayerStyle {
	protected double lower;
	protected double upper;

	public TransformedFuzzyAlphaLayerStyle(Color baseColor, double lower, double upper) {
		super(baseColor);
		this.lower = lower;
		this.upper = upper;
	}
	
	@Override
	protected int getRGBA(double v) {
		v = FuzzyUtility.fuzzify(v, lower, upper);
		byte alpha = ((Double) (v * 255d)).byteValue();

		// impose transparency based on the value of the layer
		int rgba = alpha << 24 | (baseColor.getRGB() & 0xFFF);
		return rgba;
	}
	
}
