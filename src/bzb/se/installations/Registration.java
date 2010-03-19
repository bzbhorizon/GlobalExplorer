package bzb.se.installations;

import bzb.se.messaging.Message;
import bzb.se.meta.Installations;
import bzb.se.meta.Requirements;

public class Registration extends Multi {

	public Registration() {
		super(Installations.REGISTRATION_ID, 1, Requirements.NOT_REQUIRED,
				Requirements.NOT_REQUIRED, Requirements.SOFT_REQUIRED, null,
				Installations.REGISTRATION_DESC);
	}

	public boolean handleMessage(Message receivedMessage) {
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
