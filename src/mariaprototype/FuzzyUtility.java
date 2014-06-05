package mariaprototype;

/**
 * Utility class for fuzzy values, which are otherwise backed by <code>double</code>.
 * 
 * @author Raymond Cabrera
 *
 */
public class FuzzyUtility {
	/**
	 * Constrain a variable to fuzzy bounds (0,1). Values outside the domain will be rounded to  
	 * 0 or 1.
	 * 
	 * @param fuzzy Input variable, typically an intended fuzzy variable with unchecked bounds.
	 * @return Variable in fuzzy domain (0,1).
	 */
	public static final double constrain(double fuzzy) {
		if (fuzzy > 1) fuzzy = 1;
		if (fuzzy < 0) fuzzy = 0;
		return fuzzy;
	}
	
	/**
	 * Return a crisp value based on some threshold. Values above the threshold will be set to 1, 0 otherwise.
	 * 
	 * @param input The input value.
	 * @param threshold Crisp threshold.
	 * @return Crisp value 0 or 1.
	 */
	public static final double crispify(double input, double threshold) {
		if (input > threshold) return 1;
		return 0;
	}
	
	/**
	 * Fuzzify an input variable based on lower and upper bounds.
	 * 
	 * @param input The input variable.
	 * @param lower The lower bound. Input values at or below this value will result in a fuzzy value of 0.
	 * @param upper The upper bound. Input values at or above this value will result in a fuzzy value of 1.
	 * @return Fuzzified variable.
	 */
	public static final double fuzzify(double input, double lower, double upper) {
		return constrain((input - lower) / (upper - lower));
	}
}
