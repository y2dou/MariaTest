package mariaprototype.environmental.visualization.style;

import java.awt.Color;
import java.awt.Font;

import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Vector3f;

import mariaprototype.environmental.LandCell;
import mariaprototype.environmental.LandscapeCell;
import mariaprototype.environmental.WaterCell;
import mariaprototype.visualization.ColorUtility;
import repast.simphony.visualization.visualization3D.AppearanceFactory;
import repast.simphony.visualization.visualization3D.ShapeFactory;
import repast.simphony.visualization.visualization3D.style.Style3D;
import repast.simphony.visualization.visualization3D.style.TaggedAppearance;
import repast.simphony.visualization.visualization3D.style.TaggedBranchGroup;

public class LandscapeElevationNodeStyle implements Style3D<LandscapeCell> {
	private static final Color TAN = new Color(205, 133, 63);
	private static final float HEIGHT_FACTOR = 0.005f;

	public TaggedBranchGroup getBranchGroup(LandscapeCell agent,
			TaggedBranchGroup taggedGroup) {

		if (taggedGroup == null || taggedGroup.getTag() == null) {
			taggedGroup = new TaggedBranchGroup("DEFAULT");
			Shape3D cube = ShapeFactory.createCube(.03f, "DEFAULT");

			Transform3D trans = new Transform3D();
			
			// note: agent.getElevation() can be replaced by a lookup to the Elevation grid value layer
			// translate the cube up or down based on its elevation
			trans.set(new Vector3f(0, HEIGHT_FACTOR * (float) agent.getElevation(), 0));
			
			//trans.setScale(new Vector3d(1, 0.05, 1));
			TransformGroup grp = new TransformGroup(trans);

			grp.addChild(cube);
			taggedGroup.getBranchGroup().addChild(grp);

			return taggedGroup;
		}
		return null;
	}

	public float[] getRotation(LandscapeCell o) {
		return null;
	}

	public String getLabel(LandscapeCell o, String currentLabel) {
		return null;
	}

	public Color getLabelColor(LandscapeCell t, Color currentColor) {
		return Color.YELLOW;
	}

	public Font getLabelFont(LandscapeCell t, Font currentFont) {
		return null;
	}

	public LabelPosition getLabelPosition(LandscapeCell o,
			LabelPosition curentPosition) {
		return LabelPosition.NORTH;
	}

	public float getLabelOffset(LandscapeCell t) {
		return .035f;
	}

	public TaggedAppearance getAppearance(LandscapeCell agent,
			TaggedAppearance taggedAppearance, Object shapeID) {
		if (taggedAppearance == null) {
			taggedAppearance = new TaggedAppearance();
		}

		Color color;
		
		if (agent instanceof WaterCell) {
			color = Color.blue;
		} else if (agent instanceof LandCell) {
			color = ColorUtility.blend(TAN, Color.green, ((LandCell) agent).getFertility());
		} else {
			color = new Color(0x00FFFFFF, true);
		}
		
		AppearanceFactory.setMaterialAppearance(taggedAppearance.getAppearance(), color);

		return taggedAppearance;

	}

	public float[] getScale(LandscapeCell o) {
		return null;
	}

}
