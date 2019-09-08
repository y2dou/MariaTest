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
import repast.simphony.engine.environment.RunState;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;

public class Person extends SimpleAgent {

	private double labour;
	//available labour, exclude kids at school and elders who receive pension
	private double totalLabour; 
	//all labour at hhd;
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
	private boolean schoolAttendence=false;//for sum up labour 
	private double wage;
	

	private double subsistenceUnit;
    private double subsistenceAcaiUnit;
    private double subsistenceManiocUnit;

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
		if (age<55&&age>=50) {this.ageRange=9;}
		if (age<60&&age>=55) {this.ageRange=10;}
		if (age<70&&age>=60) {this.ageRange=11;}
		if (age<80&&age>=70) {this.ageRange=12;}
		if (age>=80) {this.ageRange=13;}
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
			switch (this.ageRange) {
			case 1: this.reproduceProbility=0.045;
			break;
			case 2: this.reproduceProbility=0.082;
			break;
			case 3: this.reproduceProbility=0.195;
			break;
			case 4: this.reproduceProbility=0.113;
			break;
			case 5: this.reproduceProbility=0.066;
			break;
			case 6: this.reproduceProbility=0.035;
			break;
			case 7: this.reproduceProbility=0.020;
			break;
			case 8: this.reproduceProbility=0.020;
			break;
			}
			//total reproduceProbility, average birth rate across all education level
		/*    switch(this.ageEdu){
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
		    */
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
		double pnsn = 0;
		double pension = 0;
		if (Policy.pensionProgramStatic){
			pnsn = Policy.pensionVolume;
		}
		else {
			pnsn = Policy.pensionLists.get(RunState.getInstance().getScheduleRegistry().getModelSchedule().getTickCount()).doubleValue();
		}
		
		if (getGenderAge()>=55) {
		    pension=pnsn;
		   }
		else {if ( getGenderAge()<=-60 ) {
				pension=pnsn;
	//		System.out.println("tick = "+ tick+", pension = "+ pension);
				}
		else {
			pension = 0;
		}
		}
		this.pension=pension;
	//	System.out.println("pension: "+pension);
	}
	
	
	public double getPension() {
		return pension;
		
	}
	//to calculate how much bolsa familia this person can get;
	//but not neccessarily get it tho, because families 
	//with good income may not to
	//to check average household income will be done in hhd.aget
	public void calculateBf(int age) {
		double bolsafamilia = 0;
//		if (Policy.bfProgramStatic){
//			bolsafamilia = Policy.bfVolume;
//		}
		if (Policy.bfLists.containsKey(-1.0)){
			bolsafamilia = Policy.bfLists.get(-1.0).doubleValue();
		}
		else {
			bolsafamilia = Policy.bfLists.get(RunState.getInstance().getScheduleRegistry().getModelSchedule().getTickCount()).doubleValue();
		}
         double bf=0;
		if (age<18&&age>6) {
			bf=bolsafamilia;
		}
		else {
			bf=0;
		}
		this.bf=bf;
	//	System.out.println("bolsa familia = "+bf);
	}
	public double getBf(){
		return bf;
	}
	
	// FIXME: set contributing labour for acai, other agroforestry
	public void calculateLabour() {
		// labour (person-months)	
		labour=0;
		totalLabour=0;
	 switch (this.ageRange) {
		    case 0: 
		    	if(this.age<7) { labour=0;}
		    	else {labour=0.1;}
		    	break;
		    case 1: case 2:
		    	labour = age/19d;
		    	break;
		    case 3: case 4: case 5: case 6: case 7: case 8: case 9:
		    	labour =1;
		    	break;
		    case 10: case 11: case 12: case 13:	    	
		    	    labour = (double) 55.0/age;
		    //	System.out.println("L291 Person not lazy");
		    	break;
		}
			
    //		labour =(double)60.0/age;
		if (isFemale) labour /= 2; // reduce contributing labour: same as LUCITA
		totalLabour=labour;
		
		if (this.getPension()>0)     	{labour =0;}
		if (this.isSchoolAttendence())  {labour =0;}
	}
	
	public double getLabour() {
		return labour;
	}
	public double getTotalLabour() {
		return totalLabour;
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
	
	public void setSubsistenceUnit(int age){
		Random rand = new Random();
		if (age<18) {
			subsistenceUnit = 200 + rand.nextInt(20);
		} else if (age<60) {
			subsistenceUnit = 300 + rand.nextInt(50);
		} else 
			subsistenceUnit = 200+ rand.nextInt(20);
		
		//each person has to spend a random number between 1000-2000 per year;
		this.subsistenceUnit = subsistenceUnit;
	}

	 public double getSubsistenceUnit() {
		    
			return subsistenceUnit;
		}
	
/*	 public void setSubsistenceUnit(int age) {
	//		
		   Random r= new Random();
		if (age<7){subsistenceUnit=r.nextGaussian()*2+10;}
		//   if (age<7){subsistenceUnit=10;}
			else {
				if (age<18) { subsistenceUnit=r.nextGaussian()*3+20;	}
		//		if (age<18) { subsistenceUnit=20;	}
				else { 	if (age<50) {subsistenceUnit=r.nextGaussian()*4+30;	}
		//		else { 	if (age<50) {subsistenceUnit=30;	}
		//		else { subsistenceUnit=20;}
					    else { subsistenceUnit=r.nextGaussian()*3+20;}
				     }
			      }
		  // if (age<7){subsistenceUnit=r.nextGaussian()*2+20;}
			   if (age<7){subsistenceUnit=r.nextGaussian()*2+10;}
				else {
			//		if (age<18) { subsistenceUnit=r.nextGaussian()*3+30;	}
					if (age<18) { subsistenceUnit=r.nextGaussian()*2+20;	}
			//		else { 	if (age<50) {subsistenceUnit=r.nextGaussian()*4+40;	}
					else { 	if (age<50) {subsistenceUnit=r.nextGaussian()*2+30;	}
					else { subsistenceUnit=r.nextGaussian()*2+20;}
			//			    else { subsistenceUnit=r.nextGaussian()*3+30;}
					     }
				      }
			
			this.subsistenceUnit = subsistenceUnit;
		
		}
	 */
		public double getSubsistenceAcaiUnit() {
			return subsistenceAcaiUnit;
		}
		public void setSubsistenceAcaiUnit(int age) {
			Random r= new Random();
			   if (age<7){subsistenceAcaiUnit=r.nextGaussian()*5+50;}
				//   if (age<7){subsistenceUnit=10;}
					else {
						if (age<18) { subsistenceAcaiUnit=r.nextGaussian()*10+100;	}
				//		if (age<18) { subsistenceUnit=20;	}
						else { 	if (age<50) {subsistenceAcaiUnit=r.nextGaussian()*10+200;	}
				//		else { 	if (age<50) {subsistenceUnit=30;	}
				//		else { subsistenceUnit=20;}
							    else { subsistenceAcaiUnit=r.nextGaussian()*10+100;}
						     }
					      }
				if (this.isFemale) {
					subsistenceAcaiUnit=subsistenceAcaiUnit * 0.5;
				}
				this.subsistenceAcaiUnit = subsistenceAcaiUnit;
		}
		public double getSubsistenceManiocUnit() {
			return subsistenceManiocUnit;
		}
		
		public void setSubsistenceManiocUnit(int age) {
			
			Random r= new Random();
			/*   if (age<7){subsistenceManiocUnit=r.nextGaussian()*2+400;}
				//   if (age<7){subsistenceUnit=10;}
					else {
						if (age<18) { subsistenceManiocUnit=r.nextGaussian()*30+500;	}
				//		if (age<18) { subsistenceUnit=20;	}
						else { 	if (age<50) {subsistenceManiocUnit=r.nextGaussian()*40+600;	}
				//		else { 	if (age<50) {subsistenceUnit=30;	}
				//		else { subsistenceUnit=20;}
							    else { subsistenceManiocUnit=r.nextGaussian()*30+500;}
						     }
					      } */
			   if (age<7){subsistenceManiocUnit=r.nextGaussian()*20+50;}
				//   if (age<7){subsistenceUnit=10;}
					else {
						if (age<18) { subsistenceManiocUnit=r.nextGaussian()*30+100;	}
				//		if (age<18) { subsistenceUnit=20;	}
						else { 	if (age<50) {subsistenceManiocUnit=r.nextGaussian()*30+200;	}
				//		else { 	if (age<50) {subsistenceUnit=30;	}
				//		else { subsistenceUnit=20;}
							    else { subsistenceManiocUnit=r.nextGaussian()*30+100;}
						     }
					      }
			   if (this.isFemale) {
				   subsistenceManiocUnit = subsistenceManiocUnit*0.5;
			   }
				this.subsistenceManiocUnit = subsistenceManiocUnit;
			
		}

		public boolean isSchoolAttendence() {
			return schoolAttendence;
		}
		public void setSchoolAttendence(boolean schoolAttendence) {
			this.schoolAttendence = schoolAttendence;
		}
		
		public double getWage() {
			return wage;
		}
		public void setWage(double wage) {
			this.wage = wage;
		}

}
