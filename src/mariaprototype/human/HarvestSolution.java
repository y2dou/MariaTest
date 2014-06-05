package mariaprototype.human;

import java.util.List;

public class HarvestSolution {
	private double acai;
	private double intenseAcai;
	private double gardens;
	private double timber;
	
	private List<NetworkedUrbanAgent> recall = null;

	public double getAcai() {
		return acai;
	}

	public void setAcai(double acai) {
		this.acai = acai;
	}

	public double getIntenseAcai() {
		return intenseAcai;
	}

	public void setIntenseAcai(double intenseAcai) {
		this.intenseAcai = intenseAcai;
	}

	public double getGardens() {
		return gardens;
	}

	public void setGardens(double gardens) {
		this.gardens = gardens;
	}

	public double getTimber() {
		return timber;
	}

	public void setTimber(double timber) {
		this.timber = timber;
	}

	public List<NetworkedUrbanAgent> getRecall() {
		return recall;
	}

	public void setRecall(List<NetworkedUrbanAgent> recall) {
		this.recall = recall;
	}
}
