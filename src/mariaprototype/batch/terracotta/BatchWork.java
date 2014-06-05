package mariaprototype.batch.terracotta;

import java.io.File;

import repast.simphony.batch.BatchScenarioLoader;
import repast.simphony.batch.BatchScheduleRunner;
import repast.simphony.batch.InteractivBatchRunner;
import repast.simphony.engine.controller.DefaultController;
import repast.simphony.engine.environment.AbstractRunner;
import repast.simphony.engine.environment.ControllerRegistry;
import repast.simphony.engine.environment.DefaultRunEnvironmentBuilder;
import repast.simphony.engine.environment.RunListener;
import repast.simphony.parameter.ParameterConstants;
import repast.simphony.parameter.Parameters;
import repast.simphony.parameter.ParametersCreator;

/**
 * <p>A re-implementation of repast.simphony.batch.BatchRunner for a distributed parameter sweep. In particular, it is 
 * designed for batches of single runs.</p>
 * 
 * @author Raymond Cabrera
 *
 */
public class BatchWork implements RunListener {
	private int id;
	
	private Parameters params;
	private String resourceFile;

	private boolean pause;
	private Object monitor = new Object();

	public BatchWork(Parameters params, String resourceFile) {
		this.params = params;
		this.resourceFile = resourceFile;
	}

	public void run() throws Exception {
		AbstractRunner scheduleRunner = null;
		final boolean interactive = false;
		if (interactive)
			scheduleRunner = new InteractivBatchRunner();
		else
			scheduleRunner = new BatchScheduleRunner();

		scheduleRunner.addRunListener(this);
		DefaultRunEnvironmentBuilder runEnvironmentBuilder = new DefaultRunEnvironmentBuilder(
				scheduleRunner, true);
		DefaultController controller = new DefaultController(
				runEnvironmentBuilder);
		controller.setScheduleRunner(scheduleRunner);

		BatchScenarioLoader loader = new BatchScenarioLoader(new File(resourceFile));
		ControllerRegistry registry = loader.load(runEnvironmentBuilder);
		controller.setControllerRegistry(registry);
		
		if (!params.getSchema().contains(ParameterConstants.DEFAULT_RANDOM_SEED_USAGE_NAME)) {
			ParametersCreator creator = new ParametersCreator();
			creator.addParameters(params);
			creator.addParameter(ParameterConstants.DEFAULT_RANDOM_SEED_USAGE_NAME, Integer.class,
							(int) System.currentTimeMillis(), false);
			params = creator.createParameters();
		}

		controller.batchInitialize();
		controller.runParameterSetters(params);
		controller.runInitialize(params);
		controller.execute();
		pause = true;
		waitForRun();
		controller.runCleanup();
		controller.batchCleanup();
	}

	protected void waitForRun() {
		synchronized (monitor) {
			while (pause) {
				try {
					monitor.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
					break;
				}
			}
		}
	}

	protected void notifyMonitor() {
		synchronized (monitor) {
			monitor.notify();
		}
	}

	@Override
	public void paused() {
		
	}

	@Override
	public void restarted() {
		
	}

	@Override
	public void started() {
		
	}

	@Override
	public void stopped() {
		pause = false;
		notifyMonitor();
	}
	
	public void setId(int id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BatchWork other = (BatchWork) obj;
		if (id != other.id)
			return false;
		return true;
	}
}
