package mariaprototype.human;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import mariaprototype.R;
import mariaprototype.environmental.LandUse;

import org.rosuda.JRI.REXP;
import org.rosuda.JRI.RVector;
import org.rosuda.JRI.Rengine;

/**
 * A <code>HouseholdAgent</code> based on linear programming. This household
 * calculates an effective price based on discount rate.
 * 
 * @author Raymond Cabrera
 * 
 */
public class ForwardThinkingLinearOptimizingHouseholdAgent extends LinearOptimizingHouseholdAgent {
	private int priceMemoryLimit = 10; // memory limit of historic prices in years
	private int yearsForecasted = 3;
	private double discountRate = 0.8;
	
	private List<Double> acaiPrices = new LinkedList<Double>();
	private List<Double> maniocPrices = new LinkedList<Double>();
	private List<Double> timberPrices = new LinkedList<Double>();
	
	private double[] acaiLMCoefficients;
	private double[] maniocLMCoefficients;
	private double[] timberLMCoefficients;
	
	public ForwardThinkingLinearOptimizingHouseholdAgent() {
		super();
	}
	
	public ForwardThinkingLinearOptimizingHouseholdAgent(int id) {
		super(id);
	}
	
	@Override
	public void plan() {
		super.plan();
		
		// get past costs
		double acaiPrice = getActualPrice(LandUse.ACAI);
		acaiPrices.add(acaiPrice);
		if (acaiPrices.size() > priceMemoryLimit) {
			acaiPrices.remove(0); // remove least recent price
		}
		
		double maniocPrice = getActualPrice(LandUse.MANIOCGARDEN);
		maniocPrices.add(maniocPrice);
		if (maniocPrices.size() > priceMemoryLimit) {
			maniocPrices.remove(0); // remove least recent price
		}
		
		double timberPrice = getActualPrice(LandUse.FOREST);
		timberPrices.add(timberPrice);
		if (timberPrices.size() > priceMemoryLimit) {
			timberPrices.remove(0); // remove least recent price
		}
		
		// run linear regression
		acaiLMCoefficients = getLMCoefficients(acaiPrices);
		maniocLMCoefficients = getLMCoefficients(maniocPrices);
		timberLMCoefficients = getLMCoefficients(timberPrices);
	}
	
	private double[] getLMCoefficients(List<Double> prices) {
		if (prices.size() < 1) {
			return null;
		}
		
		// run LM regression
		Rengine re = R.getInstance().getREngine();
		
		REXP res;
		double[] xArray = new double[priceMemoryLimit];
		double[] yArray = new double[priceMemoryLimit];
		
		for (int i = 0; i < prices.size(); i++) {
			xArray[i] = i + 1;
			yArray[i] = prices.get(i);
		}
		
		re.assign("x", xArray);
		re.assign("y", yArray);
		
		res = re.eval("glm(y~x)");
		RVector fit = res.asVector();
		double[] coefficients = fit.at(0).asDoubleArray();
		
		return coefficients;
	}
	
	@Override
	protected double getExpectedPrice(LandUse crop) {
		//System.out.print("Actual price: ");
		//System.out.println(getActualPrice(crop));
		
		// extrapolate 
		double[] coefficients = {0.0, 0.0};
		if (crop.equals(LandUse.ACAI)) {
			coefficients = acaiLMCoefficients;
			
			if (acaiPrices.size() < priceMemoryLimit)
				return getActualPrice(crop);
			
		} else if (crop.equals(LandUse.MANIOCGARDEN)) {
			coefficients = maniocLMCoefficients;
			
			if (maniocPrices.size() < priceMemoryLimit)
				return getActualPrice(crop);
		} else if (crop.equals(LandUse.FOREST)) {
			coefficients = timberLMCoefficients;
			
			if (timberPrices.size() < priceMemoryLimit)
				return getActualPrice(crop);
		} else {
			throw new UnsupportedOperationException("Land use " + crop.toString() + " unsupported.");
		}
		
		double price = 0;
		double discount = 1;
		double discountSum = 0;
		for (int year = 1; year <= yearsForecasted; year++) {
			int effectiveYear = year + priceMemoryLimit;
			price += discount * (coefficients[0] + coefficients[1] * (priceMemoryLimit + effectiveYear));
			discountSum += discount;
			discount *= discountRate;
		}
		
		// normalize to appear as a single, annualized price
		price = price / discountSum;
		
		//System.out.print("Expected price: ");
		//System.out.println(price);
		
		return price;
	}
	
	public static void main(String[] args) {
		Queue<Double> prices = new LinkedList<Double>();
		prices.add(1d);
		prices.add(2d);
		prices.add(4d);
		int priceMemoryLimit = prices.size();
		
		R.getInstance().init();
		Rengine re = R.getInstance().getREngine();
		
		REXP res;
		double[] xArray = new double[priceMemoryLimit];
		double[] yArray = new double[priceMemoryLimit];
		
		for (int i = 0; i < priceMemoryLimit; i++) {
			xArray[i] = i + 1;
			yArray[i] = prices.poll();
		}
		
		re.assign("x", xArray);
		re.assign("y", yArray);
		
		res = re.eval("glm(y~x)");
		RVector fit = res.asVector();
		double[] coefficients = fit.at(0).asDoubleArray();
		
		// print coefficients
		System.out.println(coefficients[1]);
		
		R.getInstance().end();
	}
}
