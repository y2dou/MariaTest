package mariaprototype.human;

import repast.simphony.space.graph.RepastEdge;

/**
 * 
 * @author arcabrer
 *
 * @deprecated Use an edge weight instead.
 * @param <T>
 */
@Deprecated()
public class ReciprocityEdge<T> extends RepastEdge<T> {
	private double recprocity = 0; // +'ve if source is winning, -'ve if target is winning  

	public ReciprocityEdge(T source, T target, double weight) {
		super(source, target, true, weight);
	}

	public ReciprocityEdge(T source, T target) {
		super(source, target, true);
	}
	
	public double getRecprocity() {
		return recprocity;
	}
	
	public void setRecprocity(double recprocity) {
		this.recprocity = recprocity;
	}

}
