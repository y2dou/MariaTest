package mariaprototype.environmental;

import java.awt.Color;

public enum LandUse {
	ACAI(0x54CC3E) {
		@Override
		public String toString() {
			return "acai";
		}
	}, MANIOCGARDEN(0xFF534C) {
		@Override
		public String toString() {
			return "maniocgarden";
		}
	}, FOREST(0x154229) {
		@Override
		public String toString() {
			return "forest";
		}
	}, FIELDS(0xCC723E) {
		@Override
		public String toString() {
			return "fields";
		}
	}, FALLOW(0x99867B) {
		@Override
		public String toString() {
			return "fallow";
		}
	};
	
	private final Color color;
	
	private LandUse(int rgb) {
		color = new Color(rgb);
	}
	
	private LandUse(Color color) {
		this.color = color;
	}
	
	public Color getColor() {
		return color;
	}

	// corollary: acai, annuals, timber, cattle, acai (in riverine areas)
}
