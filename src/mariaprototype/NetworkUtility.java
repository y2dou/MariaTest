package mariaprototype;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Iterator;

import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;

public class NetworkUtility {
	public static void outputNetworkToCSV(Network<SimpleAgent> network, OutputStream os) {
		Writer w = new BufferedWriter(new OutputStreamWriter(os));
		try {
			w.write("source,target,reciprocity,");
			w.write(MariaContextCreator.newline);
			
			Iterator<RepastEdge<SimpleAgent>> edgeIter = network.getEdges().iterator();
			while (edgeIter.hasNext()) {
				RepastEdge<SimpleAgent> edge = edgeIter.next();
				w.write(String.valueOf(edge.getSource().getID()));
				w.write(',');
				w.write(String.valueOf(edge.getTarget().getID()));
				w.write(',');
				w.write(String.valueOf(edge.getWeight()));
				w.write(',');
				w.write(MariaContextCreator.newline);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				w.close();
			} catch (IOException e) {}
		}
	}
}
