package mariaprototype.visualization.style;

import java.awt.Color;

import mariaprototype.SimpleAgent;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.visualization.gis3D.Material;
import repast.simphony.visualization.gis3D.MaterialFactory;
import repast.simphony.visualization.gis3D.style.EdgeStyleGIS3D;

public class HouseholdNetworkStyleGIS3D implements EdgeStyleGIS3D<RepastEdge<SimpleAgent>> {
	@Override
	public float edgeRadius(RepastEdge<SimpleAgent> obj) {
		return 0.0005f * (float) obj.getWeight();
	}
	
	@Override
	public Material getMaterial(RepastEdge<SimpleAgent> obj, Material material) {
		if (material == null)
			material = new Material();
		
		// Color color = new Color(1f, 0.647f, 0f, (float) obj.getWeight()); // orange
		//Color color = new Color(1f, (float) obj.getWeight() * 0.5f, 0f, (float) obj.getWeight() * 0.5f + 0.5f); // orange-red
		Color color = obj.getSource().getColor();
		
		return MaterialFactory.setMaterialAppearance(material, color);
	}
	
	@Override
	public boolean isScaled(RepastEdge<SimpleAgent> object) {
		return false;
	}
}
