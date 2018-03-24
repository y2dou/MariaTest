package mariaprototype.human;

import java.util.HashMap;
import java.util.Map;

import repast.simphony.engine.environment.RunState;

import com.sun.xml.internal.bind.CycleRecoverable.Context;

import mariaprototype.environmental.EnvironmentalContext;
import mariaprototype.human.messaging.Message;

/**
 * A policy is a list of available government programs, 
 * conditional on a household or individual satisfying certain requirements.
 * 
 * <p>
 * In addition, the policy can be used to set transport costs and other regional or global 
 * variables.
 * 
 * @author Raymond Cabrera
 *
 * The cash transfer Program is setted here, as a global variable; 
 * to decide whether or not farm agents can get the additional cash;
 */
public abstract class Policy implements Message {
	// a set of payouts and conditions (state variables, transport costs)
	public  static boolean pensionProgramStatic=false;
	public static double pensionVolume =0;
	public static boolean bfProgramStatic=false;
	public static double bfVolume=0;
	public static int lifeExpectancy=75;
	public static int perCapitaIncomeThreshold=3000;
	public static Map<Double, Double> pensionLists = new HashMap<Double, Double> ();
	public static Map<Double, Double> bfLists = new HashMap<Double, Double>();
	
//	public static int perCapitaIncomeThreshold=0;
	//public static double alpha=1.0;
	//public static double beta=0.0;
	//this number, the average life expectancy is from brasil stat website
	//http://brasil.estadao.com.br/noticias/geral,expectativa-de-vida-da-mulher-sobe-para-78-3-anos-e-a-do-homem-para-71,1103225
//	pensionVolume = hcontext.pensionLists.get(tick).doubleValue();
	
/*	public static double getPension(){
		double tick = RunState.getInstance().getScheduleRegistry().getModelSchedule().getTickCount();
		return hcontext.pensionLists.get(tick).doubleValue();
		
	}
	public void setPension(double ticks, double pension){
		 hcontext = (HumanContext)hcontext;
		pensionLists.put(ticks, pension);	
	}*/
	@Override
	public MessageType getMessageType() {
		return MessageType.POLICY;
	}
	
	
	/*public void setCashTransferVolume(){
		if (cashTransferProgram)
		
			cashTransferVolume=100;
		else 
			cashTransferVolume=0;
	}*/
	public abstract boolean satisfiesRequirements(HouseholdAgent h);
}
