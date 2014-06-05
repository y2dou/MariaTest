package mariaprototype.batch.terracotta;

import java.util.concurrent.BlockingQueue;

/**
 * <p>Parameter processor for a Terracotta-distributed parameter sweep.</p>
 * 
 * 
 * @author Raymond Cabrera
 *
 */
public class BatchWorker extends BatchClient {
	private final String id;
	private BatchWorkItem<String> workItem;
	
	private final BlockingQueue<BatchWorkItem<String>> workQueue;
	
	public BatchWorker() throws Exception {
		id = SimpleBatchQueue.getInstance().registerWorker(this);
		System.out.println("BatchWorker " + id + " registered");
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				SimpleBatchQueue.getInstance().unregisterWorker(id);
			}
		});
		
		workQueue = SimpleBatchQueue.getInstance().getWorkQueue();
		System.out.println("Queues created.");
	}
	
	public void run(String[] args) throws InterruptedException {
		listen();
	}
	
	public void listen() throws InterruptedException {
		workItem = null;
		while (true) {
			while (workItem == null && getHealth().equals(HEALTH.ALIVE)) {
				System.out.println("Waiting for new task...");
				workItem = workQueue.take();
			}
			
			if (!getHealth().equals(HEALTH.ALIVE))
				break;
			
			if (workItem != null) {
				SimpleBatchQueue.getInstance().acceptTask(workItem, id);
				workItem.run();
				SimpleBatchQueue.getInstance().completeTask(workItem, id);
				System.out.println("Task complete.");
			}
			
			workItem = null;
		}
	}
	
	public static void main(String[] args) {
		try {
			new BatchWorker().run(args);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
