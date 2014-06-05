package mariaprototype.visualization.style;

import java.awt.Color;

import mariaprototype.SimpleAgent;
import mariaprototype.human.HouseholdAgent;
import repast.simphony.visualization.gis3D.GIS3DShapeFactory;
import repast.simphony.visualization.gis3D.Material;
import repast.simphony.visualization.gis3D.MaterialFactory;
import repast.simphony.visualization.gis3D.RenderableShape;
import repast.simphony.visualization.gis3D.style.StyleGIS3D;

public class HouseholdStyleGIS3D implements StyleGIS3D<SimpleAgent> {
	public HouseholdStyleGIS3D() {
		//Scene scene = ModelLoaderUtils.loadSceneFromModel(new File(""));
	}
	
	@Override
	public RenderableShape getShape(SimpleAgent obj) {
		
		return GIS3DShapeFactory.createSphere(6);
	}
	
	@Override
	public Material getMaterial(SimpleAgent obj, Material material) {
		if (material == null) 
			material = new Material();
		
		// HouseholdAgent h = (HouseholdAgent) obj;
		Color color = ((HouseholdAgent) obj).getColor();
		
		return MaterialFactory.setMaterialAppearance(material, color);
	}
	
	@Override
	public float[] getScale(SimpleAgent obj) {
		return null;
	}
	
	@Override
	public boolean isScaled(SimpleAgent object) {
		return false;
	}
}
