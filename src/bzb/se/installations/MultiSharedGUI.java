package bzb.se.installations;

import bzb.se.gui.GUI;

public class MultiSharedGUI extends Multi {
	
	private GUI gui;
	
	public MultiSharedGUI(String instanceName, int desiredCapacity, int requiresBT,
			int requiresMainCamera, int requiresFaceCamera, String[] prerequisiteInstances,
			String description) {
		super(instanceName, desiredCapacity, requiresBT,
				requiresMainCamera, requiresFaceCamera, prerequisiteInstances,
				description);
		
		gui = new GUI(this);
		gui.setupGUI();
	}
	
	public GUI getGUI () {
		return gui;
	}

}
