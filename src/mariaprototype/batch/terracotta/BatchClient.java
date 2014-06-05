package mariaprototype.batch.terracotta;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MBeanServerNotification;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

/**
 * <p>Common class for all Terracotta clients, both master and worker. This class contains methods to register and unregister clients.</p>
 * 
 * <p>Health is currently unused, but may be utilized in the future.</p>
 * 
 * @author Raymond Cabrera
 *
 */
public abstract class BatchClient {
	public static enum HEALTH { ALIVE, DEAD }
	private HEALTH health = HEALTH.ALIVE; 
	
	public BatchClient() {
		
	}
	
	// adapted from Terracotta sample application: shared work queue
	protected static final String registerForNotifications() throws Exception {
		List<MBeanServer> servers = MBeanServerFactory.findMBeanServer(null);
		if (servers.size() == 0) {
			System.err.println("WARNING: No JMX servers found, unable to register for notifications.");
			return "0";
		}

		MBeanServer server = (MBeanServer) servers.get(0);
		final ObjectName clusterBean = new ObjectName("org.terracotta:type=Terracotta Cluster,name=Terracotta Cluster Bean");
		ObjectName delegateName = ObjectName.getInstance("JMImplementation:type=MBeanServerDelegate");
		final List<Object> clusterBeanBag = new ArrayList<Object>();

		// listener for newly registered MBeans
		NotificationListener listener0 = new NotificationListener() {
			public void handleNotification(Notification notification, Object handback) {
				synchronized (clusterBeanBag) {
					System.out.println("Notification of new MBean received.");
					clusterBeanBag.add(handback);
					clusterBeanBag.notifyAll();
				}
			}
		};

		// filter to let only clusterBean passed through
		NotificationFilter filter0 = new NotificationFilter() {
			private static final long serialVersionUID = 1L;

			public boolean isNotificationEnabled(Notification notification) {
				if (notification.getType().equals("JMX.mbean.registered")
						&& ((MBeanServerNotification) notification).getMBeanName().equals(clusterBean)) {
					return true;
				}
				return false;
			}
		};

		// add our listener for clusterBean's registration
		server.addNotificationListener(delegateName, listener0, filter0, clusterBean);

		// because of race condition, clusterBean might already have registered
		// before we registered the listener
		Set<ObjectName> allObjectNames = server.queryNames(null, null);

		if (!allObjectNames.contains(clusterBean)) {
			synchronized (clusterBeanBag) {
				while (clusterBeanBag.isEmpty()) {
					clusterBeanBag.wait();
				}
			}
		}

		// clusterBean is now registered, no need to listen for it
		server.removeNotificationListener(delegateName, listener0);

		// listener for clustered bean events
		NotificationListener listener1 = new NotificationListener() {
			public void handleNotification(Notification notification, Object handback) {
				String nodeId = notification.getMessage();
				System.out.println("Notification of disconnected node " + nodeId + " received.");
				SimpleBatchQueue.getInstance().unregisterWorker(nodeId);
				SimpleBatchQueue.getInstance().unregisterMaster(nodeId);
			}
		};

		// filter for nodeDisconnected notifications only
		NotificationFilter filter1 = new NotificationFilter() {
			private static final long serialVersionUID = 1L;

			public boolean isNotificationEnabled(Notification notification) {
				return notification.getType().equals("com.tc.cluster.event.nodeDisconnected");
			}
		};

		// now that we have the clusterBean, add listener for membership events
		server.addNotificationListener(clusterBean, listener1, filter1, clusterBean);
		return (server.getAttribute(clusterBean, "NodeId")).toString();
	}
	
	public HEALTH getHealth() {
		return health;
	}
	
	public synchronized void setHealth(HEALTH health) {
		this.health = health;
	}
}
