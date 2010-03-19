package bzb.se.installations;

import bzb.se.messaging.Message;
import bzb.se.meta.Installations;
import bzb.se.meta.Requirements;

public class WorldMap extends Multi {
	
	public WorldMap() {
		super(Installations.WORLDMAP_ID,
				8,
				Requirements.NOT_REQUIRED,
				Requirements.HARD_REQUIRED,
				Requirements.NOT_REQUIRED,
				new String[] {Installations.REGISTRATION_ID},
				Installations.WORLDMAP_DESC);
	}

	public boolean handleMessage (Message receivedMessage) {
		boolean handled = super.handleMessage(receivedMessage);
		
		if (!handled) {
			handled = true;
			switch (receivedMessage.getMessageCode()) {
			// case 0: by multi
			// case 1: by multi
			default:
				handled  = false;
				break;
			}
		}
		return handled;
	}
	
}
