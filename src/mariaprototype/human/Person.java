package mariaprototype.human;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import mariaprototype.MariaPriorities;
import mariaprototype.SimpleAgent;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;

public class Person extends SimpleAgent {
	private double labour;
	private int age;
	private boolean isFemale;
	private boolean isHusband;
	private double pension;
	private int education;
	private int educationLevel;
//	private double ageProb;
	private double jobProb;
	private int ageRange;
	private int ageEdu;
 //   HashMap<Integer,Double> ageP = new HashMap<Integer,Double>();
    HashMap<Integer,Double> jobP = new HashMap<Integer,Double>();
	
	
	/*public enum probAge {
		AGE1 (1,0.222),
		AGE2 (2,0.000),
		AGE3 (3,0.000),
		AGE4 (4,0.750),
		AGE5 (5,1.000),
		AGE6 (6,0.824),
		AGE7 (7,0.640),
		AGE8 (8,0.854),
		AGE9 (9,0.642),
		AGE10 (10,0.831),
		AGE11(11,0.697),
		AGE12(12,0.709),
		AGE13(13,0.557);
		private final int ageRange;
		private final double ageProb;
		probAge (int ageRange,double ageProb) {
			this.ageRange=ageRange;
			this.ageProb=ageProb;
		}
	}*/

	public int getAgeRange() {
		return ageRange;
	}
	public int getEduLevel() {
		return educationLevel;
	}

	public void setAgeRange(int age) {
		if (age>=80) {this.ageRange=1;}
		if (age<80&&age>=75) {this.ageRange=2;}
		if (age<75&&age>=70) {this.ageRange=3;}
		if (age<70&&age>=65) {this.ageRange=4;}
		if (age<65&&age>=60) {this.ageRange=5;}
		if (age<60&&age>=55) {this.ageRange=6;}
		if (age<55&&age>=50) {this.ageRange=7;}
		if (age<50&&age>=45) {this.ageRange=8;}
		if (age<45&&age>=40) {this.ageRange=9;}
		if (age<40&&age>=35) {this.ageRange=10;}
		if (age<35&&age>=30) {this.ageRange=11;}
		if (age<30&&age>=25) {this.ageRange=12;}
		if (age<25&&age>=20) {this.ageRange=13;}
		
	}
	public void setEduLevel(int education){
		if (education==0) {this.educationLevel=0;}
		if (education>0&&education<=6) {this.educationLevel=1;}
		if (education>6&&education<=9) {this.educationLevel=2;}
		if (education>9&&education<=12) {this.educationLevel=3;}
		if (education>12) {this.educationLevel=4;}
	}
	
	public void setAgeEduLevel(int ageRange, int educationLevel){
		this.ageEdu=getAgeRange()*10+getEduLevel();
	}

	public Person() {
		this(RandomHelper.getDistribution("isFemale").nextInt() == 1, RandomHelper.getDistribution("age").nextInt());
	}
	
	public Person(boolean isFemale, int age) {
		this.isFemale = isFemale;
		this.age = age;
		calculateLabour();
		calculatePension();
	}
	
	public boolean isHusband() {
		return isHusband;
	}
	public void setHusband(boolean isHusband) {
		this.isHusband = isHusband;
	}
	
	//@ScheduledMethod(start = 1, interval = 1, priority = MariaPriorities.DATA_PREPARATION)
	public void age() {
		
		age++;
		calculateLabour();
		calculatePension();
	}
	
	private void calculatePension () {
	// to get people's pension, person who is older than 60, can get pension 100; 
		
		if (age>=60) 
			pension=Policy.cashTransferVolume;
				
		else
			pension=0;
		
		this.pension=pension;
	//	System.out.println("pension="+pension);
	}
	public double getPension() {
		return pension;
		
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
	
	public int getEducation() {
		return education;
	}

	public void setEducation(int education) {
		
		this.education = education;
	}	
	
/*	public double getAgeProbability() {
	
		//Oct 11, 2014
		setAgeRange(this.age);
		ageP.put(1, 0.222);
		ageP.put(2, 0.000);
		ageP.put(3, 0.000);
		ageP.put(4, 0.750);
		ageP.put(5, 1.000);
		ageP.put(6, 0.824);
		ageP.put(7, 0.640);
		ageP.put(8, 0.854);
		ageP.put(9, 0.642);
		ageP.put(10, 0.831);
		ageP.put(11, 0.697);
		ageP.put(12, 0.709);
		ageP.put(13, 0.557);
        this.ageProb=ageP.get(ageRange).doubleValue();
//so I used a hashmap to mapping the age with the probability;
  //      System.out.println("age="+age+"=ageRange="+ageRange+"=ageProb="+ageProb);
		return ageProb;
	} */
	
/*	public double getJobProbability(){
		
		setAgeRange(this.age);
		setEduLevel(this.education);
		setAgeEduLevel(ageRange,educationLevel);
		
		jobP.put(10, 0.028);
		jobP.put(11, 0.222);
		jobP.put(20, 0.000);
		jobP.put(30, 0.000);
		jobP.put(31, 0.000);
		jobP.put(40, 0.500);
		jobP.put(41, 0.750);
		jobP.put(50, 1.000);
		jobP.put(51, 1.000);
		jobP.put(60, 0.412);
		jobP.put(61, 0.760);
		jobP.put(62, 0.412);
		jobP.put(70, 0.427);
		jobP.put(71, 0.414);
		jobP.put(72, 0.000);
		jobP.put(73, 0.640);
		jobP.put(80, 0.569);
		jobP.put(81, 0.732);
		jobP.put(82, 0.711);
		jobP.put(83, 0.768);
		jobP.put(84, 0.854);
		jobP.put(90, 0.000);
		jobP.put(91, 0.441);
		jobP.put(92, 0.183);
		jobP.put(93, 0.449);
		jobP.put(94, 0.642);
		jobP.put(100,0.831);
		jobP.put(101, 0.710);
		jobP.put(102, 0.554);
		jobP.put(103, 0.693);
		jobP.put(104, 0.831);
		jobP.put(110, 0.697);
		jobP.put(111, 0.363);
		jobP.put(112, 0.581);
		jobP.put(113, 0.536);
		jobP.put(114,0.581);
		jobP.put(120, 0.709);
		jobP.put(121, 0.526);
		jobP.put(122, 0.376);
		jobP.put(123, 0.532);
		jobP.put(124, 0.355);
		jobP.put(130, 0.557);
		jobP.put(131, 0.393);
		jobP.put(132, 0.391);
		jobP.put(133, 0.235);
		jobP.put(134, 0.325);
		//the key = agerange*10+educationLevel; probability is in Appendix_age_edu_probability.xlsx sheet 2
		
		
		this.jobProb=jobP.get(ageEdu).doubleValue();
	//	System.out.println("ageRange="+ageRange+"=eduRange="+educationLevel+"=jobProb"+jobProb);
		return jobProb;
	}*/
}
