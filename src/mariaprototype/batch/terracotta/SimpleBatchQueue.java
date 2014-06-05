package mariaprototype.batch.terracotta;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SimpleBatchQueue<T extends BatchWorkItem<?>> {
	private static final int WORK_QUEUE_LIMIT = 100000;
	
	private static SimpleBatchQueue<BatchWorkItem<String>> instance = new SimpleBatchQueue<BatchWorkItem<String>>();
	
	private BlockingQueue<T> workQueue = new LinkedBlockingQueue<T>(WORK_QUEUE_LIMIT);
	
	// completed jobs
	private Map<String, BlockingQueue<T>> completedQueues = Collections.synchronizedMap(new HashMap<String, BlockingQueue<T>>());
	
	private Map<String, T> taskAssignments = Collections.synchronizedMap(new HashMap<String, T>());
	
	public static SimpleBatchQueue<BatchWorkItem<String>> getInstance() {
		return instance;
	}
	
	public SimpleBatchQueue() {
		
	}
	
	public synchronized String registerMaster(BatchMaster master) throws Exception {
		String id = BatchClient.registerForNotifications();
		completedQueues.put(id, new LinkedBlockingQueue<T>());
		System.out.println("Parameter producer " + id + " registered.");
		
		return id;
	}
	
	/**
	 * Unregister master, cancelling all outstanding jobs. The current job is left to complete.
	 * 
	 * @param id Master node ID.
	 */
	public synchronized void unregisterMaster(String id) {
		BlockingQueue<T> queue = completedQueues.remove(id);
		if (queue != null) {
			System.out.println("Parameter producer " + id + " unregistered.");
		}
	}
	
	public synchronized String registerWorker(BatchWorker worker) throws Exception {
		String id = BatchClient.registerForNotifications();
		System.out.println("Worker " + id + " registered.");
		return id;
	}
	
	/**
	 * Unregister worker, re-assigning all scheduled jobs to other workers.
	 * 
	 * @param id
	 */
	public synchronized void unregisterWorker(String id) {
		System.out.println("Worker " + id + " unregistered.");
		
		T task = taskAssignments.remove(id);
		if (task != null) {
			if (!workQueue.offer(task))
				System.err.println("Work queue full.");
		}		
	}
	
	public synchronized void add(T task) {
		try {
			workQueue.put(task);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized BlockingQueue<T> getWorkQueue() {
		return workQueue;
	}
	
	public synchronized BlockingQueue<T> getCompletedQueue(String id) {
		return completedQueues.get(id);
	}
	
	public synchronized void acceptTask(T task, String id) {
		taskAssignments.put(id, task);
	}
	
	public synchronized void completeTask(T task, String id) {
		try {
			completedQueues.get(task.getSource()).put(task);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		taskAssignments.remove(id);
	}
	
	public synchronized T getTask(BatchWorker worker) {
		return taskAssignments.get(worker);
	}
	
	public synchronized String getXmlData() {
		// iterate through each producer queue, consumer queue, and job results
		StringBuffer buffer = new StringBuffer();
		buffer.append("<workqueues>");
			buffer.append("<queue id=\"workQueue\"");
			
			for (T task : workQueue) {
				buffer.append(task.toXML());
			}
			
			buffer.append("</queue>");
		buffer.append("</workqueues>");
		
		buffer.append("<completedqueues>");
		for (Map.Entry<String, BlockingQueue<T>> queueEntry : completedQueues.entrySet()) {
			buffer.append("<queue id=\"");
			buffer.append(queueEntry.getKey());
			buffer.append("\">");
			
			for (T task : queueEntry.getValue()) {
				buffer.append(task.toXML());
			}
			
			buffer.append("</queue>");
		}
		buffer.append("</completedqueues>");
		
		buffer.append("<activetasks>");
		for (Map.Entry<String, T> assignmentEntry : taskAssignments.entrySet()) {
			buffer.append("<assignment worker=\"");
			buffer.append(assignmentEntry.getKey());
			buffer.append("\">");
			
			buffer.append(assignmentEntry.getValue().toXML());
			
			buffer.append("</assignment>");
		}
		buffer.append("</activetasks>");
		
		return buffer.toString();
	}
	
}
