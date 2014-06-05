package mariaprototype.visualization;

import java.awt.Color;

public class ColorUtility {
	public static final Color blend(Color color0, Color color1, double fuzzyValue) {
		Double redBlend = blendIntToDouble(color0.getRed(), color1.getRed(), fuzzyValue);
		Double greenBlend = blendIntToDouble(color0.getGreen(), color1.getGreen(), fuzzyValue);
		Double blueBlend = blendIntToDouble(color0.getBlue(), color1.getBlue(), fuzzyValue);
		
		Color color;
		try {
			color = new Color(redBlend.intValue(), greenBlend.intValue(), blueBlend.intValue());
		} catch (IllegalArgumentException e) {
			System.err.println("Fuzzy value: 	" + fuzzyValue);
			System.err.println("Red: 			" + redBlend);
			System.err.println("Green: 			" + greenBlend);
			System.err.println("Blue: 			" + blueBlend);
			e.printStackTrace();
			
			return Color.DARK_GRAY;
		}
		return color;
	}
	
	public static final double blendIntToDouble(int int1, int int2, double fuzzyValue) {
		return Math.round(int1 * (1d - fuzzyValue) + int2 * fuzzyValue);
	}
	
	public static final int blendInt(int int1, int int2, double fuzzyValue) {
		return (int) blendIntToDouble(int1, int2, fuzzyValue);
	}
}
