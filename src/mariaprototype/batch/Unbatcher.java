package mariaprototype.batch;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;

import repast.simphony.parameter.ParameterTreeSweeper;
import repast.simphony.parameter.Parameters;
import repast.simphony.parameter.ParametersCreator;
import repast.simphony.parameter.Schema;
import repast.simphony.parameter.SweeperProducer;
import repast.simphony.parameter.bsf.ScriptRunner;
import repast.simphony.parameter.groovy.GroovyRunner;
import repast.simphony.parameter.xml.XMLSweeperProducer;

/**
 * Reads in a parameter file and generates a list of parameters.
 * 
 * @author arcabrer
 * 
 */
public class Unbatcher {
	private List<Parameters> parameterList;
	
	private ParametersCreator creator;
	private ParameterTreeSweeper sweeper;
	
	private Queue<String> parameterFiles;
	private Parameters params;
	private Schema schema;

	public Unbatcher() {
		parameterList = new LinkedList<Parameters>();
		parameterFiles = new LinkedList<String>();
		reset();
	}
	
	public void reset() {
		parameterList.clear();
	}

	public void init(boolean interactive) {
		/*
		AbstractRunner scheduleRunner = null;

		if (interactive)
			scheduleRunner = new InteractivBatchRunner();
		else
			scheduleRunner = new BatchScheduleRunner();

		runEnvironmentBuilder = new DefaultRunEnvironmentBuilder(
				scheduleRunner, true);
		controller = new DefaultController(runEnvironmentBuilder);
		*/
	}
	
	private void setParameterFile(String filename) throws IOException {
		SweeperProducer producer = null;
		if (filename != null) {
			if (filename.endsWith("xml")) {
				producer = new XMLSweeperProducer(new File(filename).toURI().toURL());
			} else if (filename.endsWith("bsh")) {
				producer = new ScriptRunner(new File(filename));
			} else if (filename.endsWith("groovy")) {
				producer = new GroovyRunner(new File(filename));
			} else {
				return;
			}
		}
		sweeper = producer.getParameterSweeper();
		params = producer.getParameters();
		schema = params.getSchema();
		creator = new ParametersCreator();
	}

	public void addParameterFile(String filename) throws IOException {
		parameterFiles.add(filename);
	}
	
	public boolean hasNext() throws IOException {
		if (sweeper == null || sweeper.atEnd()) {
			// switch to new paramter file, if possible
			String paramFile = parameterFiles.poll();
			if (paramFile == null)
				return false;
			
			setParameterFile(paramFile);
			
			return true;
		}
		
		return true;
	}
	
	/**
	 * Retrieves the next set of Parameters. This method also updates the acquired by the <code>getParameters()</code> function,
	 * but does not guarantee that the returned Parameters object will be the same or a new instance.
	 * 
	 * @param params
	 * @throws IOException
	 */
	public Parameters next() throws IOException {
		if (hasNext()) {
			sweeper.next(params);
			
			// clone params into newParameters
			creator.addParameters(params);
			Parameters newParameters = creator.createParameters();
			Iterator<String> paramNames = schema.parameterNames().iterator();
			while (paramNames.hasNext()) {
				String paramName = paramNames.next();
				newParameters.setValue(paramName, params.getValue(paramName));
			}
			
			// parameterList.add(newParameters);
			return newParameters;
		}
		
		throw new NoSuchElementException();
	}
	
	/**
	 * Retrieves the next set of Parameters and returns them into the <code>params</code> argument.
	 * 
	 * @param params
	 * @throws IOException
	 */
	public void next(Parameters params) throws IOException {
		if (hasNext()) {
			sweeper.next(params);
			
			return;
		}
		
		throw new NoSuchElementException();
	}
	
	/**
	 * 
	 * 
	 * @return 
	 */
	public Parameters getParameters() {
		return params;
	}
	
	public static void main(String[] args) throws IOException {
		new Unbatcher().addParameterFile(args[0]);
	}

}
