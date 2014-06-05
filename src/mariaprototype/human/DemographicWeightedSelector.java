package mariaprototype.human;

import mariaprototype.Range;
import mariaprototype.WeightedSelector;

public class DemographicWeightedSelector extends WeightedSelector<Range<Integer>> {
	private boolean isFemale;
	
	public DemographicWeightedSelector(String name, boolean isFemale) {
		super(name);
		this.isFemale = isFemale;
	}
	
	public boolean isFemale() {
		return isFemale;
	}
}
