package bzb.se.installations;

import bzb.se.messaging.Message;
import bzb.se.meta.Installations;
import bzb.se.meta.Requirements;

public class Slideshow extends Multi {
	
	public Slideshow() {
		super(Installations.SLIDESHOW_ID,
				1,
				Requirements.NOT_REQUIRED,
				Requirements.NOT_REQUIRED,
				Requirements.NOT_REQUIRED,
				new String[] {Installations.WORLDMAP_ID},
				Installations.SLIDESHOW_DESC);
	}
/*
	public static class SpecificRoles {
		public static final int SLIDE_CONTROLLER = 0;
		public static final int MUSIC_CONTROLLER = 1;
	}
	
	public int getRole () {
		Enumeration e = getInstances().elements();
		while (e.hasMoreElements()) {
			Instance i = (Instance) e.nextElement();
			if (i.getRole() == SpecificRoles.SLIDE_CONTROLLER) {
				return SpecificRoles.MUSIC_CONTROLLER;
			}
		}
		return SpecificRoles.SLIDE_CONTROLLER;
	}
	*/
	public boolean handleMessage (Message receivedMessage) {
		boolean handled = super.handleMessage(receivedMessage);
		
		if (!handled) {
			handled = true;
			switch (receivedMessage.getMessageCode()) {
			// case 0: by multi
			// case 1: by multi
			default:
				handled = false;
				break;
			}
		}
		return handled;
	}
	
}
