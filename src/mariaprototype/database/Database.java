package mariaprototype.database;

import java.sql.Connection;

import mariaprototype.environmental.LandCell;
import mariaprototype.human.HouseholdAgent;
import mariaprototype.human.NetworkedUrbanAgent;

public abstract class Database {
	// private static Database instance = new H2Database("jdbc:h2:db/h2log");
	//private static Database instance = new HSQLDatabase("jdbc:hsqldb:db/hsqllog", true);
	//private static Database instance = new HSQLDatabase("jdbc:hsqldb:hsql://localhost/hsqlmaria", false);
	// private static Database instance = new DummyDatabase();
	//private static Database instance = new MySQLDatabase("jdbc:mysql://env-cfi-7.uwaterloo.ca:3306/maria", "maria", "mariaprototype");
	//private static Database instance = new MySQLDatabase("jdbc:mysql://129.97.146.227:3306/maria", "maria", "mariaprototype");
	private static Database instance = new MySQLDatabase("jdbc:mysql://localhost/test", "root", "555738");
	
	public static Database getInstance() {
		return instance;
	}
	
	/**
	 * Initialize the database. This method should be called at the beginning of every run.
	 * 
	 */
	public abstract int initRun(Connection conn);
	
	public abstract Connection getConnection();
	
	public abstract void logNewHousehold(Connection conn, HouseholdAgent a);
	public abstract void logHouseholdState(Connection conn, HouseholdAgent a, String stage);
	public abstract void logHouseholdAction(Connection conn, HouseholdAgent a, String stage, String action);
	
	public abstract void logNewUrbanAgent(Connection conn, NetworkedUrbanAgent a, HouseholdAgent sourceHousehold);
	public abstract void logRecalledUrbanAgent(Connection conn, NetworkedUrbanAgent a, String stage);
	public abstract void logUrbanAgentState(Connection conn, NetworkedUrbanAgent a, String stage);
	
	// public abstract void logHouseholdNetwork(Network n);
	public abstract void logLandCells(Connection conn, Iterable<LandCell> c);
	
	public abstract void commit(Connection conn);
	/**
	 * Close the database. This method should be called at the end of a batch run.
	 */
	public abstract void close(Connection conn);
	
	public abstract int getRunID(Connection conn);
	public abstract boolean isOpen(Connection conn);	
}
