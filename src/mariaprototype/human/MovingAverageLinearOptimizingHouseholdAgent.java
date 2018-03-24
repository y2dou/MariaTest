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
 * @author Yue Dou
 * 
 */
public class MovingAverageLinearOptimizingHouseholdAgent extends LinearOptimizingHouseholdAgent {
	private int priceMemoryLimit = 3; // memory limit of historic prices in years
    private int yieldMemoryLimit = 3;
	
	private List<Double> acaiPrices = new LinkedList<Double>();
	private List<Double> maniocPrices = new LinkedList<Double>();
	private List<Double> timberPrices = new LinkedList<Double>();
	
	private List<Double> acaiYields = new LinkedList<Double>();
	private List<Double> intenseacaiYields = new LinkedList<Double>();
	private List<Double> maniocYields = new LinkedList<Double>();
	private List<Double> timberYields = new LinkedList<Double>();
	
	public MovingAverageLinearOptimizingHouseholdAgent() {
		super();
	}
	
	public MovingAverageLinearOptimizingHouseholdAgent(int id) {
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
		
		double acaiYield = getLastYearCropYield(LandUse.ACAI);
		acaiYields.add(acaiYield);
		if (acaiYields.size() > yieldMemoryLimit) {
			acaiYields.remove(0);
		}
		
		double intenseacaiYield = getLastYearCropYield(LandUse.INTENSEACAI);
		intenseacaiYields.add(intenseacaiYield);
		if (intenseacaiYields.size() > yieldMemoryLimit){
			intenseacaiYields.remove(0);
		}
		
		double maniocYield = getLastYearCropYield(LandUse.MANIOCGARDEN);
		maniocYields.add(maniocYield);
		if (maniocYields.size() > yieldMemoryLimit) {
			maniocYields.remove(0);
		}
		
		double timberYield = getLastYearCropYield(LandUse.FOREST);
		timberYields.add(timberYield);
		if (timberYields.size()>yieldMemoryLimit) {
			timberYields.remove(0);
		}
	}
	

	
	@Override
	protected double getExpectedPrice(LandUse crop){
		double price = 0;
		double sum = 0;
		if (crop.equals(LandUse.ACAI)) {
			if (acaiPrices.size() < priceMemoryLimit) {
				return getActualPrice(crop);
			}
			else {
				for (int i=priceMemoryLimit-1;i>=0;i--){
					sum +=acaiPrices.get(i).doubleValue();
				}
					price = sum/acaiPrices.size();
			//		System.out.println(acaiPrices.toString());
			//		System.out.println(price);
				return price;
			}
		}else if (crop.equals(LandUse.MANIOCGARDEN)) {		
			if (maniocPrices.size() < priceMemoryLimit) {
				return getActualPrice(crop);
			}
			else {
				for (int i=priceMemoryLimit-1;i >= 0; i--){
					sum +=maniocPrices.get(i).doubleValue();
				}
				price = sum/maniocPrices.size();
				return price;
			}
		} else if (crop.equals(LandUse.FOREST)) {	
			if (timberPrices.size() < priceMemoryLimit) {
				return getActualPrice(crop);
			}
			else {
				for (int i=priceMemoryLimit-1;i>=0;i--){
					sum += timberPrices.get(i).doubleValue();
				}
				price = sum/timberPrices.size();
				return price;
			}
		} else {
			throw new UnsupportedOperationException("Land use " + crop.toString() + " unsupported.");
		}
		
	}
	
	@Override
	protected double getExpectedCropYield(LandUse crop) {
	    double sum = 0;
	    double expectedCropYield = 0;
	    
		if (crop.equals(LandUse.ACAI)) {
			if (acaiYields.size() < yieldMemoryLimit) {
				return getLastYearCropYield(crop);
			}
			else {
				for (int i = yieldMemoryLimit-1; i>=0;i--){
					sum += acaiYields.get(i).doubleValue();
				}
				expectedCropYield = sum/acaiYields.size();
				return expectedCropYield;
			}
		}else if (crop.equals(LandUse.INTENSEACAI)) {
			if (intenseacaiYields.size() < yieldMemoryLimit) {
				return getLastYearCropYield(crop);
			}
			else {
				for (int i = yieldMemoryLimit-1; i>=0;i--){
					sum += intenseacaiYields.get(i).doubleValue();
				}
				expectedCropYield = sum/intenseacaiYields.size();
//				System.out.println("expectedCropYield: "+intenseacaiYields.toString());
			    return expectedCropYield;
			}
		} else if (crop.equals(LandUse.MANIOCGARDEN)) {
			if (maniocYields.size() < yieldMemoryLimit) {
				return getLastYearCropYield(crop);
			}
			else {
				for (int i = yieldMemoryLimit-1; i>=0;i--){
					sum += maniocYields.get(i).doubleValue();
				}
				expectedCropYield = sum/maniocYields.size();
				return expectedCropYield;
			}
		} else if (crop.equals(LandUse.FOREST)) {
			if (timberYields.size() < yieldMemoryLimit) {
				return getLastYearCropYield(crop);
			}
			else {
				for (int i = yieldMemoryLimit-1; i>=0;i--){
					sum += timberYields.get(i).doubleValue();
				}
				expectedCropYield = sum/timberYields.size();
				return expectedCropYield;
			}
		}else {
			throw new UnsupportedOperationException("Land use " + crop.toString() + " unsupported.");
		}

	}
	
	
}
