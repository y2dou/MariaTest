package mariaprototype.visualization.style;

import mariaprototype.FuzzyUtility;

/**
 * Similar to FuzzyLayerStyle, except that the input values from the ValueLayer are converted
 * to fuzzy values on-the-fly.
 * 
 * @author Raymond Cabrera
 * @see FuzzyLayerStyle
 *
 */
public abstract class TransformedFuzzyLayerStyle extends FuzzyLayerStyle {
	protected double lower = 0;
	protected double upper = 1;
	
	protected void setRange(double lower, double upper) {
		this.lower = lower;
		this.upper = upper;
	}
	
	@Override
	protected Double getFuzzyValue(double... coordinates) {
		return FuzzyUtility.fuzzify(fuzzyLayer.get(coordinates), lower, upper);
	}

}
