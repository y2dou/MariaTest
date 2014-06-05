package mariaprototype.batch.terracotta;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import mariaprototype.batch.Unbatcher;
import repast.simphony.batch.BatchMain;
import repast.simphony.parameter.Parameters;

/**
 * <p>Parameter producer for a Terracotta-distributed parameter sweep.</p>
 * 
 * <p>This class takes in a (directory of) paramter sweep file(s) and distributes parameter sets to a common queue. It then listens for results 
 * on a result queue until all tasks have been returned.</p>
 * 
 * @author Raymond Cabrera
 * @see BatchWorker
 * @see SimpleBatchQueue
 *
 */
public class BatchMaster extends BatchClient {
	private final String id;
	
	private BlockingQueue<BatchWorkItem<String>> completedQueue;
	
	private final boolean collectTimingStats = true;
	
	public BatchMaster() throws Exception {
		id = SimpleBatchQueue.getInstance().registerMaster(this);
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				SimpleBatchQueue.getInstance().unregisterMaster(id);
			}
		});
		
		completedQueue = SimpleBatchQueue.getInstance().getCompletedQueue(id);
	}
	
	public void run(String[] args) throws Exception {
		String resourceFile = args[0];
		Unbatcher unbatcher = new Unbatcher();
		File paramFile = new File(args[1]);
		System.out.println("Unbatcher created, paramter file loaded.");
		Set<BatchWorkItem<String>> tasks = new HashSet<BatchWorkItem<String>>();
		if (paramFile.isDirectory()) {
			for (File file : paramFile.listFiles()) {
				unbatcher.addParameterFile(file.getPath());
			}
		} else {
			unbatcher.addParameterFile(args[1]);
		}
	
		// ListIterator<Parameters> iterator = unbatcher.getIterator();
		int counter = 0;
		while (unbatcher.hasNext()) {
			
			BatchWorkItem<String> task = new BatchWorkItem<String>(new ParameterRun(unbatcher.next(), resourceFile), id);
			System.out.print(".");
			
			if (++counter >= 50) {
				counter = 0;
				System.out.println();
			}
			
			SimpleBatchQueue.getInstance().add(task);
			
			tasks.add(task);
		}
		
		System.out.println();
		waitForResults(tasks);
	}
	
	private void waitForResults(Collection<BatchWorkItem<String>> tasks) {
		LinkedList<Long> completedTimes;
		long startTime;
		if (collectTimingStats) {
			System.out.println("Stats collection enabled.");
			startTime = System.currentTimeMillis();
			completedTimes = new LinkedList<Long>();
		}
		
		System.out.println("Checking for completed tasks...");
		while (!tasks.isEmpty()) {
			BatchWorkItem<String> task = null;
			try {
				while (task == null) {
					
					task = completedQueue.take();
					
					if (collectTimingStats) {
						completedTimes.add(System.currentTimeMillis());
					}
				
					if (task != null) {
						break;
					}
				}
				
				if (task == null)
					break;
				
				tasks.remove(task);
				try {	
					if (task.get().isSuccess()) {
						System.out.println("Task completed: " + task.get().toString());
					} else {
						System.err.println("Task failed: " + task.get().toString());
					}
				} catch (ExecutionException e) {
					System.err.println("Task failed with exception: " + e.getCause());
				}
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
		
		System.out.println("");
		if (collectTimingStats) {
			// using the set of completed times, calculate the average
			long totalInterarrivalTime = 0;
			
			ListIterator<Long> iter = completedTimes.listIterator();
			try {
				Long lastTime = iter.next();
				while (iter.hasNext()) {
					Long thisTime = iter.next();
					totalInterarrivalTime += thisTime - lastTime; 
					lastTime = thisTime;
				}
			} catch (NoSuchElementException e) {
				e.printStackTrace();
			}
			
			long totalTime = System.currentTimeMillis() - startTime;
			
			double avgInterarrivalTime = (double) totalInterarrivalTime / (double) completedTimes.size();
			double avgTime = (double) totalTime / (double) completedTimes.size();
			
			System.out.println("Average interarrival time: " + avgInterarrivalTime);
			System.out.println("Average time (simple): " + avgTime);
		}
	}
	
	private static Result<String> runBatch(String repastArgs, String paramFile, String resourceFile) {
		try {
			BatchMain.main(new String[]{repastArgs, "-params", paramFile, resourceFile});
			return new Result<String>(true, new Date().toString());
		} catch (Exception e) {
			e.printStackTrace();
			return new Result<String>(false, e.toString());
		}		
	}
	
	private static Result<String> runBatch(Parameters params, String resourceFile) {
		try {
			new BatchWork(params, resourceFile).run();
			return new Result<String>(true, new Date().toString());
		} catch (Exception e) {
			e.printStackTrace();
			return new Result<String>(false, e.toString());
		}		
	}
	
	@SuppressWarnings("unused")
	private static class CallableRun implements Callable<Result<String>> {
		private String repastArgs;
		private String paramFile;
		private String resourceFile;

		public CallableRun(String repastArgs, String paramFile,
				String resourceFile) {
			super();
			this.repastArgs = repastArgs;
			this.paramFile = paramFile;
			this.resourceFile = resourceFile;
		}

		public Result<String> call() throws InterruptedException {
			return runBatch(repastArgs, paramFile, resourceFile);
		}
	}
	
	private static class ParameterRun implements Callable<Result<String>> {
		private Parameters params;
		private String resourceFile;

		public ParameterRun(Parameters params,
				String resourceFile) {
			super();
			this.params = params;
			this.resourceFile = resourceFile;
		}

		public Result<String> call() throws InterruptedException {
			return runBatch(params, resourceFile);
		}
	}
	
	public static void main(String[] args) {
		try {
			new BatchMaster().run(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
