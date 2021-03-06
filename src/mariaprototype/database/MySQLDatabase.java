package mariaprototype.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.hsqldb.Types;

import mariaprototype.environmental.LandCell;
import mariaprototype.environmental.LandUse;
import mariaprototype.human.HouseholdAgent;
import mariaprototype.human.MyLandCell;
import mariaprototype.human.NetworkedUrbanAgent;
import mariaprototype.human.Person;
import mariaprototype.human.Policy;
import mariaprototype.human.messaging.MarketPrices;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.environment.RunState;
import repast.simphony.parameter.Parameters;

public class MySQLDatabase extends Database {
	public static final String DRIVER = "com.mysql.jdbc.Driver";
    public MarketPrices marketPrices;
	private int connections = 1;

	private BlockingQueue<Connection> connectionPool = new LinkedBlockingQueue<Connection>();
	private Map<Connection, Integer> activeConnections = new HashMap<Connection, Integer>();

	public MySQLDatabase(final String url, final String username,
			final String password) {
		try {
			Class.forName(DRIVER);

			for (int i = 0; i < connections; i++) {
				Connection conn = DriverManager.getConnection(url, username,
						password);
				// set connection parameters
				conn.setAutoCommit(false);
				connectionPool.add(conn);
			}

			Thread shutdownThread = new Thread() {
				@Override
				public void run() {
					for (Connection conn : connectionPool) {
						try {
							conn.close();
						} catch (SQLException e) {
						}
					}
				}
			};
			shutdownThread.setName("MySQL shutdown thread");
			Runtime.getRuntime().addShutdownHook(shutdownThread);

			// database should have been initialized already

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	@Override
	public Connection getConnection() {
		try {
			return connectionPool.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.exit(1);
			return null;
		}
	}

	@Override
	public int initRun(Connection conn) {
		if (conn == null)
			return -1;

		int runID;
		try {
			Parameters p = RunEnvironment.getInstance().getParameters();

			PreparedStatement s = conn
					.prepareStatement(
							"INSERT INTO tblrun(randomSeed, sweepName, rundate, "
									+ "numHouseholds, numPersons, numOffers, lambdaOffers, offerValueLow, offerValueHigh, "
									+ "percentHeuristicHouseholds, percentOptimalHouseholds, percentForwardOptimalHouseholds, percentFullForwardOptimalHouseholds, " 
									+ "percentChayanovHouseholds, percentMinLabourHouseholds, percentSubsistenceHouseholds, "
									+ "percentMovingAverageLinearOptimizingHouseholds, percentMovingAverageSubsistenceHouseholds,percentMovingAverageMinLabourHouseholds,"
									+ "labourMultiplier, capitalMultiplier, "
									+ "pension, bf,"
									+ "climateIndicator,"
									+ "acaiMultiplier, maniocMultiplier, timberMultiplier, "
									+ "acaiPrice, maniocPrice, timberPrice, "
									+ "acaiLabour, maniocLabour, fallowLabour, forestFallowLabour, maintainAcaiLabour, maintainManiocLabour, "
									+ "acaiCost, maniocCost, fallowCost, forestFallowCost, maintainAcaiCost, maintainManiocCost, "
									+ "harvestAcaiLabour, harvestManiocLabour, harvestTimberLabour "
									+ ") " +

									"VALUES (?, ?, ?, " 
									+ "?, ?, ?, ?, ?, ?, "
									+ "?, ?, ?, ?, "
									+ "?,?,?,"
									+ "?,?,?,"
									+ "?, ?, "
									+ "?, ?,"
									+ "?,"
									+ "?, ?, ?,"
									+ "?, ?, ?,"
									+ "?, ?, ?, ?, ?, ?, " 
									+ "?, ?, ?, ?, ?, ?, " 
									+ "?, ?, ?" + ")",

							PreparedStatement.RETURN_GENERATED_KEYS);

			int i = 1;
			s.setInt(i++, (Integer) p.getValue("randomSeed"));
			s.setString(i++, p.getValueAsString("sweepName"));
			s.setTimestamp(i++, new Timestamp(System.currentTimeMillis()));

			s.setInt(i++, (Integer) p.getValue("numHouseholds"));
			s.setInt(i++, (Integer) p.getValue("numPersons"));
			s.setInt(i++, (Integer) p.getValue("numOffers"));
			s.setDouble(i++, (Double) p.getValue("lambdaOffers"));

			double offerSpread = (Double) p.getValue("offerValueSpread");
			double offerAverage = (Double) p.getValue("offerValueAverage");

			s.setDouble(i++, offerAverage - offerSpread);
			s.setDouble(i++, offerAverage + offerSpread);

			s.setDouble(i++, (Double) p.getValue("percentHeuristicHouseholds"));
			s.setDouble(i++, (Double) p.getValue("percentOptimalHouseholds"));
			s.setDouble(i++, (Double) p.getValue("percentForwardOptimalHouseholds"));
			s.setDouble(i++, (Double) p.getValue("percentFullForwardOptimalHouseholds"));
			s.setDouble(i++, (Double) p.getValue("percentChayanovHouseholds"));
			s.setDouble(i++, (Double) p.getValue("percentMinLabourHouseholds"));
			s.setDouble(i++, (Double) p.getValue("percentSubsistenceHouseholds"));
			s.setDouble(i++, (Double) p.getValue("percentMovingAverageLinearOptimizingHouseholds"));
			s.setDouble(i++, (Double) p.getValue("percentMovingAverageSubsistenceHouseholds"));
			s.setDouble(i++, (Double) p.getValue("percentMovingAverageMinLabourHouseholds"));
			
			s.setDouble(i++, (Double) p.getValue("labourMultiplier"));
			s.setDouble(i++, (Double) p.getValue("capitalMultiplier"));

			s.setDouble(i++, (Double) p.getValue("pension"));
			s.setDouble(i++, (Double) p.getValue("bf"));
			
			 s.setDouble(i++, (Double) p.getValue("climateIndicator"));
			 
			double globalMultiplier = (Double) p
					.getValue("priceStreamMultiplier");
			double acaiMultiplier = (Double) p.getValue("acaiMultiplier");
			s.setDouble(i++, acaiMultiplier > 0 ? acaiMultiplier
					: globalMultiplier);

			double maniocMultiplier = (Double) p.getValue("maniocMultiplier");
			s.setDouble(i++, maniocMultiplier > 0 ? maniocMultiplier
					: globalMultiplier);

			double timberMultiplier = (Double) p.getValue("timberMultiplier");
			s.setDouble(i++, timberMultiplier > 0 ? timberMultiplier
					: globalMultiplier);

			s.setDouble(i++, (Double) p.getValue("acaiPrice"));
			s.setDouble(i++, (Double) p.getValue("maniocPrice"));
			s.setDouble(i++, (Double) p.getValue("timberPrice"));
           
			s.setDouble(i++, (Double) p.getValue("acaiLabour"));
			s.setDouble(i++, (Double) p.getValue("maniocLabour"));
			s.setDouble(i++, (Double) p.getValue("fallowLabour"));
			s.setDouble(i++, (Double) p.getValue("forestFallowLabour"));
			s.setDouble(i++, (Double) p.getValue("maintainAcaiLabour"));
			s.setDouble(i++, (Double) p.getValue("maintainManiocLabour"));

			s.setDouble(i++, (Double) p.getValue("acaiCost"));
			s.setDouble(i++, (Double) p.getValue("maniocCost"));
			s.setDouble(i++, (Double) p.getValue("fallowCost"));
			s.setDouble(i++, (Double) p.getValue("forestFallowCost"));
			s.setDouble(i++, (Double) p.getValue("maintainAcaiCost"));
			s.setDouble(i++, (Double) p.getValue("maintainManiocCost"));

			s.setDouble(i++, (Double) p.getValue("harvestAcaiLabour"));
			s.setDouble(i++, (Double) p.getValue("harvestManiocLabour"));
			s.setDouble(i++, (Double) p.getValue("harvestTimberLabour"));
			// System.out.println("testttttt");
			s.executeUpdate();

			ResultSet rs = s.getGeneratedKeys();
			if (rs.next()) {
				runID = rs.getInt(1);
			} else {
				runID = -1;
			}
			// conn.commit();
			s.close();

			activeConnections.put(conn, runID);

			System.out.println("Initialized run " + runID);
		} catch (SQLException e) {
			e.printStackTrace();
			runID = -1;
		}
		return runID;
	}

	@Override
	public int getRunID(Connection conn) {
		if (conn == null)
			return -1;

		return activeConnections.get(conn);
	}

	@Override
	public void close(Connection conn) {
		if (conn == null)
			return;

		activeConnections.remove(conn);
		connectionPool.add(conn);
	}

	@Override
	public void commit(Connection conn) {
		if (conn == null)
			return;

		try {
			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean isOpen(Connection conn) {
		try {
			return conn != null && !conn.isClosed();
		} catch (SQLException e) {
			return false;
		}
	}

	@Override
	public void logNewHousehold(Connection conn, HouseholdAgent a) {
		if (conn == null)
			return;

		try {
			PreparedStatement ps = conn
					.prepareStatement("INSERT INTO tblHousehold"
							+ "(householdID, runID, isUpland) VALUES " + "(?, ?, ?)");
			ps.setInt(1, a.getID());
			ps.setInt(2, getRunID(conn));
			ps.setBoolean(3, a.isUpLand());
		//to record if this household is on upland;
		//Yue, Sep 29, 2015
			ps.execute();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void logNewUrbanAgent(Connection conn, NetworkedUrbanAgent a,
			HouseholdAgent source) {
		if (conn == null)
			return;

		try {
			PreparedStatement ps = conn
					.prepareStatement("INSERT INTO tblUrbanAgent"
							+ "(urbanagentID, runID, sourceHousehold, settlementyear, age, isfemale, education, wage, employer) VALUES "
							+ "(?, ?, ?, ?, ?, ?, ?, ?, ?)");

			ps.setInt(1, a.getID());
			ps.setInt(2, getRunID(conn));
			ps.setInt(3, source.getID());
			ps.setDouble(4, RunState.getInstance().getScheduleRegistry()
					.getModelSchedule().getTickCount());
			ps.setInt(5, a.getPerson().getAge());
			ps.setBoolean(6, a.getPerson().isFemale());
			ps.setInt(7, a.getPerson().getEducation());
			ps.setDouble(8, a.getWage());
			ps.setString(9, a.getEmployer().getName());

			ps.execute();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void logRecalledUrbanAgent(Connection conn, NetworkedUrbanAgent a,
			String stage) {
		if (conn == null)
			return;

		try {
			PreparedStatement ps = conn
					.prepareStatement("UPDATE tblUrbanAgent "
							+ "SET leavingyear=?, leavingstage=? "
							+ "WHERE urbanagentid=? AND runid=?");
			ps.setDouble(1, RunState.getInstance().getScheduleRegistry()
					.getModelSchedule().getTickCount());
			ps.setString(2, stage);
			ps.setInt(3, a.getID());
			ps.setInt(4, getRunID(conn));

			ps.execute();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void logUrbanAgentState(Connection conn, NetworkedUrbanAgent a,
			String stage) {
		if (conn == null)
			return;

		try {
			PreparedStatement ps = conn
					.prepareStatement("INSERT INTO tblUrbanAgentState"
							+ "(urbanagentID, runID, tick, stage, capital) VALUES "
							+ "(?, ?, ?, ?, ?)");

			ps.setInt(1, a.getID());
			ps.setInt(2, getRunID(conn));
			ps.setDouble(3, RunState.getInstance().getScheduleRegistry()
					.getModelSchedule().getTickCount());
			ps.setString(4, stage);
			ps.setDouble(5, a.getCapital());

			ps.execute();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void logHouseholdState(Connection conn, HouseholdAgent a,
			String stage) {
		if (conn == null)
			return;

		try {
			int numAcai = 0;
			int numFallow = 0;
			int numForest = 0;
			int numManiocGarden = 0;
			int numFields = 0;
			int numOther = 0;
			double labour = 0;

			PreparedStatement psM = conn
					.prepareStatement("INSERT INTO tblHouseholdMembers "
							+ "(householdID, runID, tick, stage, memberID, age, isFemale, isAtSchool,education) VALUES (?, ?, ?, ?, ?, ?,?, ?, ?)");
			for (Person p : a.getFamilyMembers()) {
			//	labour += p.getLabour();
				// log member's gender and age 
				//and other information
				psM.setInt(1, a.getID());
				psM.setInt(2, getRunID(conn));
				psM.setDouble(3, RunState.getInstance().getScheduleRegistry()
						.getModelSchedule().getTickCount());
				psM.setString(4, stage);
				psM.setInt(5, p.getID());
				psM.setInt(6, p.getAge());
				psM.setBoolean(7, p.isFemale());
				psM.setBoolean(8, p.isSchoolAttendence());
				psM.setInt(9, p.getEducation());
		//		psM.setDouble(10, p.getLabour());
			//	psM.setDouble(11, p.getTotalLabour());
				psM.execute();
			}
			psM.close();

			for (MyLandCell c : a.getTenure().values()) {
				switch (c.getLandUse()) {
				case ACAI:
					numAcai++;
					break;
				case MANIOCGARDEN:
					numManiocGarden++;
					break;
				case FIELDS:
					numFields++;
					break;
				case FALLOW:
					numFallow++;
					break;
				case FOREST:
					numForest++;
					break;
				default:
					numOther++;
				}
			}

			// PreparedStatement ps =
			// conn.prepareStatement("INSERT INTO tblHouseholdState" +
			// "(householdID, runID, tick, stage, capital, labour, acai, maniocgarden, fields, forest, fallow, other, "
			// +
			// "harvestAcai, harvestManioc, harvestTimber) VALUES " +
			// "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			PreparedStatement ps = conn
					.prepareStatement("INSERT INTO tblHouseholdState"
							+ "(householdID, runID, tick, stage, "
							+ "aveFemaleEdu, husEdu,"
							+ "capital, labour, totalLabour, pension, bf, wage,subReq, "
							+ "acai, maniocgarden, fields, forest, fallow, other, "
							+ "harvestAcai, harvestManioc, harvestTimber) VALUES "
							+ "(?, ?, ?, ?, ?, ?,?, ?, ?,?,?,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			int i = 1;
			ps.setInt(i++, a.getID());
			ps.setInt(i++, getRunID(conn));
			ps.setDouble(i++, RunState.getInstance().getScheduleRegistry()
					.getModelSchedule().getTickCount());
			ps.setString(i++, stage);
			ps.setDouble(i++, a.getAveFemaleEdu());
			ps.setDouble(i++, a.getHusbandEdu());
	//		ps.setDouble(i++, a.getPerCapitaIncome());
			ps.setDouble(i++, a.getCapital());
			ps.setDouble(i++, a.getLabour());
			ps.setDouble(i++, a.getTotalLabour());
			ps.setDouble(i++, a.getPension());
			ps.setDouble(i++, a.getBf());
			ps.setDouble(i++, a.getWage());
			ps.setDouble(i++, a.getTotalSubsistenceRequirement());
	//		ps.setDouble(i++, a.getAnnualIncome());
			// new add;
			
			ps.setInt(i++, numAcai);
			ps.setInt(i++, numManiocGarden);
			ps.setInt(i++, numFields);
			ps.setInt(i++, numForest);
			ps.setInt(i++, numFallow);
			ps.setInt(i++, numOther);
			ps.setDouble(i++, a.getAcaiYield());
			ps.setDouble(i++, a.getManiocYield());
			ps.setDouble(i++, a.getTimberYield());
			ps.execute();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void logHouseholdAction(Connection conn, HouseholdAgent a,
			String stage, String action) {
		if (conn == null)
			return;

		// log events worth logging
		try {
			PreparedStatement ps = conn
					.prepareStatement("INSERT INTO tblHouseholdAction"
							+ "(householdID, runID, tick, stage, actionName) VALUES "
							+ "(?, ?, ?, ?, ?)");
			ps.setInt(1, a.getID());
			ps.setInt(2, getRunID(conn));
			ps.setDouble(3, RunState.getInstance().getScheduleRegistry()
					.getModelSchedule().getTickCount());
			ps.setString(4, stage);
			ps.setString(5, action);
			ps.execute();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void logLandCells(Connection conn, Iterable<LandCell> landCells) {
		if (conn == null)
			return;

		try {
			PreparedStatement ps = conn.prepareStatement("INSERT INTO tblLand"
					+ "(cellID, runID, yearDeforested, isUpland, owner) VALUES "
					+ "(?, ?, ?, ?,?)");
			for (LandCell c : landCells) {
				if (c.getYearDeforested() >= 0) {
					ps.setInt(1, c.getID());
					ps.setInt(2, getRunID(conn));
					ps.setDouble(3, c.getYearDeforested());
                    ps.setBoolean(4, c.isUpland());
					
					if (c.getLandHolder() == null)
						ps.setNull(5, Types.INTEGER);
					else
						ps.setInt(5, c.getLandHolder().getID());

					ps.executeUpdate();
				}
			}
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
