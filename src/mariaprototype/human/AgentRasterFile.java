package mariaprototype.human;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.util.HashMap;
import java.util.Map;

import mariaprototype.Point;

public class AgentRasterFile {

	private int width;
	private int height;
	private double originx;
	private double originy;
	private double cellSize;
	
	private Map<Integer, Point> agents = new HashMap<Integer, Point>();
	
	public AgentRasterFile(InputStream stream) {
		try {
			agents = createNewAgentsFromRasterFile(stream);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private Map<Integer, Point> createNewAgentsFromRasterFile(InputStream stream) throws IOException {
		agents.clear();
		
		int type;
		BufferedReader r = new BufferedReader(new InputStreamReader(stream));
		StreamTokenizer st = new StreamTokenizer(r);

		st.parseNumbers();
		st.wordChars('_', '_');
		st.eolIsSignificant(false);
		st.lowerCaseMode(true);
		// cols
		type = st.nextToken();
		type = st.nextToken();
		width = (int) st.nval;
		// rows
		type = st.nextToken();
		type = st.nextToken();
		height = (int) st.nval;
		// xllcorner
		type = st.nextToken();
		type = st.nextToken();
		originx = st.nval; 
		// yllcorner
		type = st.nextToken();
		type = st.nextToken();
		originy = st.nval;
		// cellSize
		type = st.nextToken();
		type = st.nextToken();
		cellSize = st.nval;
		// termx and termy
		// double termx = Math.floor(originx) + cellSize * width;
		// double termy = Math.floor(originy) + cellSize * height;
		// missing
		type = st.nextToken();
		double nodata;
		if (type == StreamTokenizer.TT_NUMBER) {
			st.pushBack();
			nodata = -9999;
		} else {
			type = st.nextToken();
			nodata = st.nval;
		}
		st.ordinaryChars('E', 'E');

		double d1;
		
		for (int i = height - 1; i >= 0; i--) {
			for (int j = 0; j < width; j++) {
				st.nextToken();
				d1 = st.nval;
				
				// handle exponents
				type = st.nextToken();
				if (type != StreamTokenizer.TT_NUMBER
						&& type != StreamTokenizer.TT_EOF) {
					type = st.nextToken();
					d1 = d1 * Math.pow(10.0, st.nval);
				} else {
					st.pushBack();
				}

				if (d1 != nodata) {
					agents.put((int) d1, new Point(j, i));
				}
			}
		}
		return agents;
	}
	
	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public double getOriginx() {
		return originx;
	}

	public double getOriginy() {
		return originy;
	}
	
	public double getCellSize() {
		return cellSize;
	}

	public Map<Integer, Point> getAgents() {
		return agents;
	}

}
