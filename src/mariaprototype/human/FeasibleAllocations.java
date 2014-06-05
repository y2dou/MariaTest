package mariaprototype.human;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

import mariaprototype.human.landallocation.AcaiGoodnessComparator;
import mariaprototype.human.landallocation.HousegardenGoodnessComparator;

import javolution.util.FastTable;

public class FeasibleAllocations {
	// order potential expansion actions by best allocation: k-NN for now
	private PriorityQueue<MyLandCell> toIntensifyAcai = new PriorityQueue<MyLandCell>(11, ACAI_COUNT_COMPARATOR);
	private PriorityQueue<MyLandCell> toManiocGarden = new PriorityQueue<MyLandCell>(11, MANIOCGARDEN_COUNT_COMPARATOR);
	private FastTable<MyLandCell> toHarvestAcai = new FastTable<MyLandCell>();
	private FastTable<MyLandCell> toHarvestIntenseAcai = new FastTable<MyLandCell>();
	private FastTable<MyLandCell> toHarvestHousegarden = new FastTable<MyLandCell>();
	private FastTable<MyLandCell> toHarvestTimber = new FastTable<MyLandCell>();
	private FastTable<MyLandCell> toMaintainAcai = new FastTable<MyLandCell>();
	private FastTable<MyLandCell> toMaintainManiocGarden = new FastTable<MyLandCell>();
	private FastTable<MyLandCell> toPossiblyDevelop = new FastTable<MyLandCell>();
	
	private FastTable<MyLandCell> toForestFallow = new FastTable<MyLandCell>(); // de-intensify																	// acai
	private FastTable<MyLandCell> toFallow = new FastTable<MyLandCell>();
	
	private Map<Person, JobOffer> employables = new HashMap<Person, JobOffer>();
	
	public FeasibleAllocations() {
		
	}
	
	public void reset() {
		// maintenance
		toMaintainAcai.clear();
		toMaintainManiocGarden.clear();
		
		// harvest actions
		toHarvestAcai.clear();
		toHarvestIntenseAcai.clear();
		toHarvestTimber.clear();
		toHarvestHousegarden.clear();
		
		// possibilities
		toPossiblyDevelop.clear();
		
		// land use change
		toForestFallow.clear();
		toFallow.clear();
		toManiocGarden.clear();
		toIntensifyAcai.clear();
		
		employables.clear();
	}
	
	public FastTable<MyLandCell> getToFallow() {
		return toFallow;
	}
	
	public FastTable<MyLandCell> getToForestFallow() {
		return toForestFallow;
	}
	
	public PriorityQueue<MyLandCell> getToIntensifyAcai() {
		return toIntensifyAcai;
	}
	public PriorityQueue<MyLandCell> getToManiocGarden() {
		return toManiocGarden;
	}
	public FastTable<MyLandCell> getToHarvestAcai() {
		return toHarvestAcai;
	}
	public FastTable<MyLandCell> getToHarvestIntenseAcai() {
		return toHarvestIntenseAcai;
	}
	public FastTable<MyLandCell> getToHarvestHousegarden() {
		return toHarvestHousegarden;
	}
	public FastTable<MyLandCell> getToHarvestTimber() {
		return toHarvestTimber;
	}
	public FastTable<MyLandCell> getToMaintainAcai() {
		return toMaintainAcai;
	}
	public FastTable<MyLandCell> getToMaintainManiocGarden() {
		return toMaintainManiocGarden;
	}
	public FastTable<MyLandCell> getToPossiblyDevelop() {
		return toPossiblyDevelop;
	}
	public Map<Person, JobOffer> getEmployables() {
		return employables;
	}



	// land use allocation heuristics
	public static Comparator<MyLandCell> ACAI_COUNT_COMPARATOR = new AcaiGoodnessComparator();
	public static Comparator<MyLandCell> MANIOCGARDEN_COUNT_COMPARATOR = new HousegardenGoodnessComparator();
}
