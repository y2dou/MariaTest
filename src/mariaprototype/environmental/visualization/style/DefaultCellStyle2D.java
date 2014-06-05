package mariaprototype.environmental.visualization.style;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import repast.simphony.visualization.visualization2D.style.DefaultStyle2D;

public class DefaultCellStyle2D extends DefaultStyle2D {
	protected Rectangle2D rect = new Rectangle2D.Float(0, 0, 5, 5);
	protected Shape s = new Rectangle2D.Float(0, 0, 5, 5);

}
