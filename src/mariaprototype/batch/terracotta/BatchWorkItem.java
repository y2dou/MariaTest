package mariaprototype.batch.terracotta;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A FutureTask-based wrapper around a Callable. 
 * 
 * @author Raymond Cabrera
 *
 * @param <T> Return type of the task.
 */
public class BatchWorkItem<T> extends FutureTask<Result<T>> {
	public static enum STATUS { NEW, RUNNING, CANCELLED, COMPLETE, FAILED }
	private STATUS status = STATUS.NEW;
	
	private static final AtomicInteger ID_GENERATOR = new AtomicInteger(0);
	private final int id = ID_GENERATOR.getAndIncrement();
	
	private final String source;
	
	public BatchWorkItem(Callable<Result<T>> callable, String source) {
		super(callable);
		this.source = source;
	}
	
	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		setStatus(STATUS.CANCELLED);
		return super.cancel(mayInterruptIfRunning);
	}
	
	@Override
	protected void done() {
		setStatus(STATUS.COMPLETE);
		super.done();
	}
	
	@Override
	public void run() {
		setStatus(STATUS.RUNNING);
		super.run();
	}
	
	public String getSource() {
		return source;
	}
	
	public STATUS getStatus() {
		return status;
	}
	
	public void setStatus(STATUS status) {
		this.status = status;
	}
	
	public String toXML() {
		StringBuffer buffer = new StringBuffer();
		
		buffer.append("<task id=\"");
		buffer.append(Integer.toString(id));
		buffer.append("\" source=\"");
		buffer.append(source);
		buffer.append("\"");
		
		
		// BatchQueue.getInstance().getSource(this);
		// buffer.append(" source=")
		
		if (status == STATUS.COMPLETE) {
			buffer.append(" status=\"COMPLETE\" result=\"");
			try {
				buffer.append(get().toString());
			} catch (InterruptedException e) {
			} catch (ExecutionException e) {
				buffer.append("failed");
			}
			buffer.append("\"");
		} else if (status == STATUS.FAILED) {
			buffer.append(" status=\"FAILED\"");
		} else if (status == STATUS.CANCELLED) {
			buffer.append(" status=\"CANCELLED\"");
		}
		buffer.append("\">");
		return buffer.toString();
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
		BatchWorkItem<?> other = (BatchWorkItem<?>) obj;
		if (id != other.id)
			return false;
		return true;
	}
}
