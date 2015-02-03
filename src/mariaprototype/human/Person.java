package mariaprototype.human;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
	private double bf;
	private int education;
	private int educationLevel;
//	private double ageProb;
	private double jobProb;
	private int ageRange;
	private int ageEdu;
	private double reproduceProbility;
	private boolean reproduceThisTime;
 //   HashMap<Integer,Double> ageP = new HashMap<Integer,Double>();
    HashMap<Integer,Double> jobP = new HashMap<Integer,Double>();
	
	

	public int getAgeRange() {
		return ageRange;
	}
	public int getEduLevel() {
		return educationLevel;
	}

	public void setAgeRange(int age) {
		//if (age>=80) {this.ageRange=1;}
		if (age<=15)  {this.ageRange=0;}
		if (age<=17&&age>15) {this.ageRange=1;}
		if (age==18||age==19) {this.ageRange=2;}
		if (age<25&&age>=20) {this.ageRange=3;}
		if (age<30&&age>=25) {this.ageRange=4;}
		if (age<35&&age>=30) {this.ageRange=5;}
		if (age<40&&age>=35) {this.ageRange=6;}
		if (age<45&&age>=40) {this.ageRange=7;}
		if (age<50&&age>=45) {this.ageRange=8;}
		if (age<60&&age>=50) {this.ageRange=9;}
		if (age<70&&age>=60) {this.ageRange=10;}
		if (age<80&&age>=70) {this.ageRange=11;}
		if (age>=80) {this.ageRange=12;}
	//	System.out.println("ageRange="+this.ageRange);
		
	}
	
	public void setEduLevel(int education){
		//if (education==0) {this.educationLevel=0;}
		if (education<6) {this.educationLevel=1;} //no education or fundamental incompleto
		if (education>=6&&education<9) {this.educationLevel=2;} //fundamental completo e m'edio incompleto
		if (education>=9&&education<12) {this.educationLevel=3;} //medio completo e superior incompleto
		if (education>=12) {this.educationLevel=4;} //superior completo
	}
	
    public void setAgeEdu(int age, int eduLevel){
    	this.ageEdu=getAgeRange()+getEduLevel()*100;
    }

	public Person() {
		this(RandomHelper.getDistribution("isFemale").nextInt() == 1, RandomHelper.getDistribution("age").nextInt());
	}
	
	public Person(boolean isFemale, int age) {
		this.isFemale = isFemale;
		this.age = age;
	//	calculateLabour();
	//	calculatePension();
	//	calculateBf();
	}
	
	//@ScheduledMethod(start = 1, interval = 1, priority = MariaPriorities.ACTION)	
	//person reproduce, add new familyMember and labour , edited by Yue Dou, Jan 31,2015
	//. 
	public boolean getReproduce (){
		//check if this is the eligible person
		// use boolean to deliver the checking results to householdAgent;
		boolean reproduceCheck=false;
		if (this.getGenderAge()>15&&this.getGenderAge()<50) {
		/*	switch (this.ageRange) {
			case 1: this.reproduceProbility=0.045*1000;
			case 2: this.reproduceProbility=0.082*1000;
			case 3: this.reproduceProbility=0.195*1000;
			case 4: this.reproduceProbility=0.113*1000;
			case 5: this.reproduceProbility=0.066*1000;
			case 6: this.reproduceProbility=0.035*1000;
			case 7: this.reproduceProbility=0.020*1000;
			case 8: this.reproduceProbility=0.020*1000;
			}*/ //total reproduceProbility, average birth rate across all education level
		    switch(this.ageEdu){
		    case 101:  this.reproduceProbility=0.07;
		    break;
		    case 102:  this.reproduceProbility=0.13;
		    break;
		    case 103:  this.reproduceProbility=0.24;
		    break;
		    case 104:  this.reproduceProbility=0.13;
		    break;
		    case 105:  this.reproduceProbility=0.08;
		    break;
		    case 106:  this.reproduceProbility=0.04;
		    break;
		    case 107:  this.reproduceProbility=0.01;
		    break;
		    case 108:  this.reproduceProbility=0.02;
		    break;
		    //no education or fundamental incompleto
		    case 201:  this.reproduceProbility=0.00;
		    break;
		    case 202:  this.reproduceProbility=0.07;
		    break;
		    case 203:  this.reproduceProbility=0.14;
		    break;
		    case 204:  this.reproduceProbility=0.12;
		    break;
		    case 205:  this.reproduceProbility=0.04;
		    break;
		    case 206:  this.reproduceProbility=0.00;
		    break;
		    case 207:  this.reproduceProbility=0.00;
		    break;
		    case 208:  this.reproduceProbility=0.09;
		    break;
		    //middle school incomplete
		    case 301:  this.reproduceProbility=0.00;
		    break;
		    case 302:  this.reproduceProbility=0.05;
		    break;
		    case 303:  this.reproduceProbility=0.17;
		    break;
		    case 304:  this.reproduceProbility=0.04;
		    break;
		    case 305:  this.reproduceProbility=0.00;
		    break;
		    case 306:  this.reproduceProbility=0.06;
		    break;
		    case 307:  this.reproduceProbility=0.08;
		    break;
		    case 308:  this.reproduceProbility=0.00;
		    break;
		    //high school incomplete
		    case 401:  this.reproduceProbility=0.00;
		    break;
		    case 402:  this.reproduceProbility=0.07;
		    break;
		    case 403:  this.reproduceProbility=0.14;
		    break;
		    case 404:  this.reproduceProbility=0.12;
		    break;
		    case 405:  this.reproduceProbility=0.04;
		    break;
		    case 406:  this.reproduceProbility=0.00;
		    break;
		    case 407:  this.reproduceProbility=0.00;
		    break;
		    case 408:  this.reproduceProbility=0.09;
		    break;
		    //high school or above
		    //data in table_96.xlsx
		    }
		}
		else {this.reproduceProbility=0.00;}
	//	System.out.println("ageEdu="+ageEdu+" reproduceProb="+this.reproduceProbility);
		if (new Random().nextDouble() < this.reproduceProbility)
		{ reproduceCheck=true;} 
		else {
			reproduceCheck=false;
		//	System.out.println("Can't reproduce");
		}
	//	System.out.println(this.reproduceProbility+" "+this.reproduceThisTime);
		this.reproduceThisTime=reproduceCheck;
	//	System.out.println("they can reproduce");
		return reproduceThisTime;	
		}
	
	public boolean isHusband() {
		return isHusband;
	}
	public void setHusband(boolean isHusband) {
		this.isHusband = isHusband;
	}
	
/* @ScheduledMethod(start = 1, interval = 1, priority = MariaPriorities.PLANNING)
	public void age() {	
		this.age=this.age+1;
		calculateLabour();
		calculatePension();
		calculateBf();
		setAgeRange(this.age);
		setEduLevel(this.getEducation());
		this.ageEdu=getAgeRange()+getEduLevel()*100;
	}
 */
	public void calculatePension () {
	// to get people's pension, male person who is older than 60, can get pension 100; 
		//female who is older than 55, get a pension.
		if (this.getGenderAge()>=55)
		  {  pension=Policy.pensionVolume;}
		else 
			{if ( this.getGenderAge()<=-60 ) 
		      pension=Policy.pensionVolume;}
		
	//	System.out.println("GenderAge="+this.getGenderAge()+" pension="+pension);
		/*if (age>=60) 
			pension=Policy.cashTransferVolume;
				
		else
			pension=0;
		
		this.pension=pension;*/
	//	System.out.println("pension="+pension);
	}
	
	
	public double getPension() {
		return pension;
		
	}
	//to calculate how much bolsa familia this person can get;
	//but not neccessarily get it tho, because families 
	//with good income may not to
	//to check average household income will be done in hhd.aget
	public void calculateBf() {
		if (this.age<18&&this.age>6) {
			bf=Policy.bfVolume;
		}
		else {
			bf=0;
		}
		
	}
	public double getBf(){
		return bf;
	}
	
	// FIXME: set contributing labour for acai, other agroforestry
	public void calculateLabour() {
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
	public void setAge(int age) {
		this.age = age;
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
	

	

}
