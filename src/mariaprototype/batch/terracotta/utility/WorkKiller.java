package mariaprototype.batch.terracotta.utility;

import mariaprototype.batch.terracotta.BatchClient;
import mariaprototype.batch.terracotta.SimpleBatchQueue;

public class WorkKiller extends BatchClient {
	public void run() {
		SimpleBatchQueue.getInstance().getWorkQueue().clear();
	}
	
	public static void main(String[] args) {
		new WorkKiller().run();
	}
}
