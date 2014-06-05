package mariaprototype;

import repast.simphony.engine.schedule.ScheduleParameters;

/**
 * Priorities for all <code>ScheduledMethod</code> calls in Maria. All priorities should be set in 
 * this class instead of constant values.
 * 
 * @author Raymond Cabrera
 *
 */
public final class MariaPriorities {
	/**
	 * Priority for actions imposing spatiotemporal climatological changes.
	 */
	public static final double CLIMATOLOGY = ScheduleParameters.FIRST_PRIORITY;
	
	/**
	 * Priority for initial inter-agent message passing.
	 */
	public static final double MESSAGE_PASSING_1 = 0;
	
	/**
	 * Priority for agent planning stage.
	 */
	public static final double PLANNING = -1000;
	
	public static final double MESSAGE_PASSING_2 = -1100;
	
	/**
	 * Priority for intermediate planning-action stage.
	 */
	public static final double INTERMEDIATE = -1500;
	
	public static final double MESSAGE_PASSING_3 = -1900;
	
	/**
	 * Priority for agent action stage.
	 */
	public static final double ACTION = -2000;
	
	/**
	 * Priority for market stage.
	 */
	public static final double MARKETS = -10000;
	
	public static final double MESSAGE_PASSING_4 = -13000;

	/**
	 * Priority for spatial (cellular) biophysical changes.
	 */
	public static final double BIOPHYSICAL = -20000;
	
	/**
	 * Priority for harvesting and sales.
	 */
	public static final double HARVEST = -50000;
	
	/**
	 * Priority for retrospection and self-assessment. May be useful for adaptive models.
	 */
	public static final double RETROSPECT = -1000000;
	
	/**
	 * Priority for data preparation stage. This should be the next-to-final priority, other than the reporting stage.
	 */
	public static final double DATA_PREPARATION = -10000000;
	
	/**
	 * Priority for reporting. This is the final stage of each step.
	 */
	public static final double REPORT = -100000000;
	
	/**
	 * Priority for the end-of-run final report. This is the final stage and should be the last priority.
	 */
	public static final double FINAL_REPORT = -1000000000;
	
	/**
	 * Priority for simulation cleanup.
	 */
	public static final double CLEANUP = ScheduleParameters.LAST_PRIORITY;
}
