package mariaprototype.human;

public enum Neighbourhood {
	MOORE {
		private final int[] x = {-1, 0, 1, -1, 1, -1, 0, 1};
		private final int[] y = {-1, -1, -1, 0, 0, 1, 1, 1};
		private final double[] weights = {1, 1, 1, 1, 1, 1, 1, 1};
		
		@Override
		public int[] getX() {
			return x;
		}
		
		@Override
		public int[] getY() {
			return y;
		}
		
		@Override
		public double[] getWeights() {
			return weights;
		}
	},
	
	VON_NEUMANN {
		private final int[] x = {0, -1, 1, 0};
		private final int[] y = {-1, 0, 0, 1};
		private final double[] weights = {1, 1, 1, 1};
		
		@Override
		public int[] getX() {
			return x;
		}
		
		@Override
		public int[] getY() {
			return y;
		}
		
		@Override
		public double[] getWeights() {
			return weights;
		}
	};
	
	public abstract int[] getX();
	public abstract int[] getY();
	public abstract double[] getWeights();
	
	private Neighbourhood() {

	}
}
