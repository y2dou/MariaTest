package mariaprototype.visualization.style;

import java.awt.Color;

import mariaprototype.SimpleAgent;
import repast.simphony.visualization.gis3D.GIS3DShapeFactory;
import repast.simphony.visualization.gis3D.Material;
import repast.simphony.visualization.gis3D.MaterialFactory;
import repast.simphony.visualization.gis3D.RenderableShape;
import repast.simphony.visualization.gis3D.style.StyleGIS3D;

public class NetworkedUrbanAgentGIS3D implements StyleGIS3D<SimpleAgent> {
	public NetworkedUrbanAgentGIS3D() {
		//Scene scene = ModelLoaderUtils.loadSceneFromModel(new File(""));
	}
	
	@Override
	public RenderableShape getShape(SimpleAgent obj) {
		return GIS3DShapeFactory.createBox(6);
	}
	
	@Override
	public Material getMaterial(SimpleAgent obj, Material material) {
		if (material == null) 
			material = new Material();
		
		Color color = obj.getColor();
		
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
