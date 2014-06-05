package mariaprototype.database;

import java.sql.Connection;

import mariaprototype.environmental.LandCell;
import mariaprototype.human.HouseholdAgent;
import mariaprototype.human.NetworkedUrbanAgent;

/**
 * A dummy database object used in place of a database when no data output is desired.
 * 
 * @author arcabrer
 *
 */
public class DummyDatabase extends Database {
	private int runID = 0;

	@Override
	public int initRun(Connection conn) { System.out.println("Run " + ++runID); return runID; }
	
	@Override
	public Connection getConnection() {
		return null;
	}

	@Override
	public void close(Connection conn) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void commit(Connection conn) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getRunID(Connection conn) {
		// TODO Auto-generated method stub
		return runID;
	}

	@Override
	public boolean isOpen(Connection conn) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void logHouseholdAction(Connection conn, HouseholdAgent a,
			String stage, String action) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void logHouseholdState(Connection conn, HouseholdAgent a,
			String stage) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void logNewHousehold(Connection conn, HouseholdAgent a) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void logNewUrbanAgent(Connection conn, NetworkedUrbanAgent a, HouseholdAgent sourceHousehold) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void logRecalledUrbanAgent(Connection conn, NetworkedUrbanAgent a, String stage) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void logUrbanAgentState(Connection conn, NetworkedUrbanAgent a,
			String stage) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void logLandCells(Connection conn, Iterable<LandCell> c) {
		// TODO Auto-generated method stub
		
	}
}
