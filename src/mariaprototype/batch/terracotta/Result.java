package mariaprototype.batch.terracotta;

/**
 * Wrapper around an arbitrary result.
 * 
 * @author Raymond Cabrera
 *
 * @param <T> Return type of result, usually <code>String</code>.
 */
public class Result<T> {
	private boolean success;
	private T result;
	
	public Result(boolean success, T result) {
		this.result = result;
		this.success = success;
	}
	
	public T getResult() {
		return result;
	}
	
	public boolean isSuccess() {
		return success;
	}
	
	@Override
	public String toString() {
		return result.toString();
	}
}
