package mariaprototype.human;

import mariaprototype.MariaPriorities;
import mariaprototype.SimpleAgent;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;

public class Person extends SimpleAgent {
	private double labour;
	private int age;
	private boolean isFemale;
	
	public Person() {
		this(RandomHelper.getDistribution("isFemale").nextInt() == 1, RandomHelper.getDistribution("age").nextInt());
	}
	
	public Person(boolean isFemale, int age) {
		this.isFemale = isFemale;
		this.age = age;
		calculateLabour();
	}
	
	@ScheduledMethod(start = 1, interval = 1, priority = MariaPriorities.DATA_PREPARATION)
	public void age() {
		age++;
		calculateLabour();
	}
	
	// FIXME: set contributing labour for acai, other agroforestry
	private void calculateLabour() {
		// labour (person-months)
		if (age >= 18)
			labour = 1;
		else
			labour = age / 18d;
		
		if (isFemale) labour /= 2; // reduce contributing labour: same as LUCITA
	}
	
	public double getLabour() {
		return labour;
	}
	
	public boolean isFemale() {
		return isFemale;
	}
	
	public int getAge() {
		return age;
	}
	
	/**
	 * Return negative ages for males and positive ages for females. Useful for demographic pyramid construction.
	 * 
	 * @return
	 */
	public int getGenderAge() {
		return age * (isFemale ? 1 : -1);
	}
}