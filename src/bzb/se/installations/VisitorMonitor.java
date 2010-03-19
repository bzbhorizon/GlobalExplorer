package bzb.se.installations;

import bzb.se.messaging.Message;
import bzb.se.meta.Installations;
import bzb.se.meta.MessageCodes;
import bzb.se.meta.Requirements;
import bzb.se.visitors.Visitor;


public class VisitorMonitor extends Multi {

	public VisitorMonitor() {
		super(Installations.VISITOR_MONITOR_ID,
				8,
				Requirements.NOT_REQUIRED,
				Requirements.NOT_REQUIRED,
				Requirements.NOT_REQUIRED,
				null,
				null);
	}
	
	public boolean handleMessage (Message receivedMessage) {
		boolean handled = super.handleMessage(receivedMessage);
		
		//if (!handled) { // visitor monitor overloads some multi responses
			handled = true;
			switch (receivedMessage.getMessageCode()) {
			case MessageCodes.In.Multi.ASSIGN_INSTANCE:
				// also if assigning instance at visitorMonitor, register the visitor
				Visitor newVisitor = new Visitor(receivedMessage.getHost(), null, false, false, false, 0, Installations.DECOUPLED_ID, null, null, new int[]{0, 0});
				newVisitor.registerInDB(db);
				break;
			default:
				handled  = false;
				break;
			}
		//}
		return handled;
	}

}
