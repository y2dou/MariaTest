package mariaprototype.human;

import java.util.List;
import java.util.Map;

public class DevelopmentSolution {
	private double acai = 0;
	private double garden = 0;
	private double maintainAcai = 0;
	private double maintainGarden = 0;
	
	private Map<Person, JobOffer> employ = null;
	private List<NetworkedUrbanAgent> recall = null;

	public double getAcai() {
		return acai;
	}

	public void setAcai(double acai) {
		this.acai = acai;
	}

	public double getGarden() {
		return garden;
	}

	public void setGarden(double garden) {
		this.garden = garden;
	}

	public double getMaintainAcai() {
		return maintainAcai;
	}

	public void setMaintainAcai(double maintainAcai) {
		this.maintainAcai = maintainAcai;
	}

	public double getMaintainGarden() {
		return maintainGarden;
	}

	public void setMaintainGarden(double maintainGarden) {
		this.maintainGarden = maintainGarden;
	}

	public Map<Person, JobOffer> getEmploy() {
		return employ;
	}

	public void setEmploy(Map<Person, JobOffer> employ) {
		this.employ = employ;
	}

	public List<NetworkedUrbanAgent> getRecall() {
		return recall;
	}

	public void setRecall(List<NetworkedUrbanAgent> recall) {
		this.recall = recall;
	}
	
	
	
	
}
