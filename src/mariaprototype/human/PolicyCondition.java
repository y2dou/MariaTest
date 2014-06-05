package mariaprototype.human;

public interface PolicyCondition {
	public boolean isSatisfiedBy(HouseholdAgent agent);
	// should also report on how to satisfy condition
}
