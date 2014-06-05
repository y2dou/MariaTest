package mariaprototype.batch;

import repast.simphony.batch.BatchMain;

/**
 * A wrapper for BatchMain, set to terminate upon any exception. It is assumed that the exception will be 
 * printed or otherwise handled by BatchMain or by the throwing method. This wrapper assures that a worker 
 * will be terminated when an unrecoverable error occurs (such as an <code>OutOfMemoryError</code>).
 * 
 * @author Raymond Cabrera
 *
 */
public class TerminatingBatchMain {
	public static void main(String[] args) {
		try {
			new BatchMain().run(args);
		} catch (Exception e) {System.exit(1);}
	}
}
