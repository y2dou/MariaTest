package mariaprototype;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.Connection;
import java.util.Iterator;

import mariaprototype.database.Database;
import mariaprototype.environmental.EnvironmentalContext;
import mariaprototype.human.HumanContext;

import org.apache.log4j.PropertyConfigurator;

import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.environment.RunState;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.parameter.Parameters;

public class MariaContextCreator extends DefaultContext<SimpleAgent> implements
		ContextBuilder<SimpleAgent> {
	public static String newline = System.getProperty("line.separator");
	private String path;
	
	private Connection conn;
	
	public boolean invalidRun = false;
	
	public Context<SimpleAgent> build(Context<mariaprototype.SimpleAgent> context) {
		// initialize logger
		PropertyConfigurator.configure("log4j.properties");

		ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
		ScheduleParameters params = ScheduleParameters.createAtEnd(MariaPriorities.FINAL_REPORT - 10);
		schedule.schedule(params, this, "finalReport");
		
		params = ScheduleParameters.createAtEnd(MariaPriorities.REPORT);
		schedule.schedule(params, this, "report");
		
		params = ScheduleParameters.createAtEnd(MariaPriorities.CLEANUP);
		schedule.schedule(params, this, "cleanup");
		
		Parameters p = RunEnvironment.getInstance().getParameters();
		
		double runlength = (Double) p.getValue("runlength");
		RunEnvironment.getInstance().endAt(runlength);
		invalidRun = false;
		
		if ((Double) p.getValue("percentOptimalHouseholds") <= 0 && 
				(Double) p.getValue("percentHeuristicHouseholds") <= 0 && 
				(Double) p.getValue("percentForwardOptimalHouseholds") <= 0 &&
				(Double) p.getValue("percentFullForwardOptimalHouseholds") <= 0) {
			System.err.println("MariaContextCreator: Skipping run: invalid parameters.");
			invalidRun = true;
		}
		
		int runID = -1;
		if (!invalidRun) {
			conn = Database.getInstance().getConnection();
			RunState.getInstance().addToRegistry("connection", conn);
			runID = Database.getInstance().initRun(conn); // initialize database
			
			if (runID < 0) { // critical error - results cannot be recorded. abort.
				System.exit(1);
			}
		}
		
		if (invalidRun) { // run is invalid, but other runs can be attempted
			RunEnvironment.getInstance().endAt(1);
		}
		
		if ((Boolean) p.getValue("outputEnabled")) {
			path = "output/" + p.getValueAsString("sweepName")	+ "/" + String.valueOf(runID);
			RunState.getInstance().addToRegistry("path", path);
			(new File(path)).mkdirs();
		}
		
		RunState.getInstance().addToRegistry("invalidRun", invalidRun);
		
		/*
		path = "output/" + RunEnvironment.getInstance().getParameters().getValueAsString("outputpath");
		if (RunEnvironment.getInstance().isBatch()) {
			path = path + "/" + String.valueOf(RunState.getInstance().getRunInfo().getRunNumber());
		}
		*/
		/*
		EnvironmentalContext envContext = (EnvironmentalContext) new EnvironmentalContextBuilder().build(null);
		context.addSubContext(envContext);
		context.addSubContext(new HumanContextBuilder(envContext).build(null));
		*/
		
		context.addSubContext(new EnvironmentalContext("EnvironmentalContext"));
		context.addSubContext(new HumanContext("HumanContext"));
		
		return context;
	}
	
	public Connection getConnection() {
		return conn;
	}
	
	public void report() {
		
	}
	
	public void finalReport() {
		if (invalidRun) {
		} else {
			Parameters p = RunEnvironment.getInstance().getParameters();
			
			if ((Boolean) p.getValue("outputEnabled") && (Boolean) p.getValue("outputParametersAsFile")) 
				writeParametersToFile(path + "/params.txt");
			
			int runID = Database.getInstance().getRunID(conn);
			Database.getInstance().commit(conn);
			
			System.out.println("Run "+runID+" complete.");
		}
	}
	
	private void writeParametersToFile(String file) {
		Parameters p = RunEnvironment.getInstance().getParameters();
		Iterator<String> parameterIter = p.getSchema().parameterNames().iterator();
		
		OutputStream os;
		Writer w = null;
		
		try {
			os = new FileOutputStream(file, false);
			w = new BufferedWriter(new OutputStreamWriter(os));
			
			// write headers
			w.write("name,value,");
			w.write(newline);
			
			while (parameterIter.hasNext()) {
				String pName = parameterIter.next();
				
				w.write(pName);
				w.write(',');
				w.write(p.getValueAsString(pName));
				w.write(',');
				w.write(newline);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (w != null)
				try {
					w.close();
				} catch (IOException e) {}
		}
	}

	public void cleanup() {
		if (!invalidRun) {
			Database.getInstance().close(conn);
			RunState.getInstance().removeFromRegistry("connection");
		}
	}
	
	// log4j.rootLogger = debug,A1;
}
