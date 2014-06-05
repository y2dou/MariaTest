package mariaprototype;

import org.rosuda.JRI.Rengine;

public class R {
	private static R instance = new R();
	
	public static R getInstance() {
		return instance;
	}
	
	private Rengine re;
	
	private R() {
		init();
	}
	
	public void init() {
		if (re != null) {
			//end();
		}
		
		String[] rArgs = new String[1];
		rArgs[0] = "--no-save";
		
		if (!Rengine.versionCheck()) {
			System.err.println("Rengine version check failed.");
			System.exit(1);
		}
		
		re = new Rengine(rArgs, false, new RConsole());
		if (!re.waitForR()) {
			System.out.println("Cannot load R");
			System.exit(1);
		}
	}
	
	public void end() {
		re.end();
		re = null;
	}
	
	public Rengine getREngine() {
		return re;
	}
}
