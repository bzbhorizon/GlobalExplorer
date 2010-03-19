package bzb.se.installations.instances;

import java.util.Enumeration;
import java.util.Vector;

import bzb.se.exceptions.InstanceNotOwnedException;
import bzb.se.exceptions.MessageNotSentException;
import bzb.se.exceptions.NoSuchVisitorException;
import bzb.se.messaging.Message;
import bzb.se.meta.MessageCodes;
import bzb.se.meta.Network;
import bzb.se.visitors.Visitor;

public class Friends extends Instance {

	public Friends () {
		super();
	}
	
	public boolean setPortAndStart (int desiredPort) {
		boolean portSet = super.setPortAndStart(desiredPort);
		
		if (portSet) {
			// where am i specific stuff
		}
		return portSet;
	}
	
	public bzb.se.installations.Friends getFriendsMulti () {
		return (bzb.se.installations.Friends) getMulti();
	}
	
	public final class SpecificMessageCodes {
		public final class In {
			public static final int ACCEPT_GROUP = 500;
		}
		public final class Out {
			public static final int GROUP_CREATED = 500;
		}
	}
	
	public boolean handleMessage (Message receivedMessage) {
		boolean handled = super.handleMessage(receivedMessage);
		
		//if (!handled) {
			handled = true;
			switch (receivedMessage.getMessageCode()) {
			case MessageCodes.In.Instance.COUPLED:
				completeExperience();
				try {
					getFriendsMulti().updateSharedScreen(getOwner(), true);
				} catch (InstanceNotOwnedException e) {
					e.printStackTrace();
				} catch (NoSuchVisitorException e) {
					e.printStackTrace();
				}
				break;
			case MessageCodes.In.Instance.DECOUPLING:
				try {
					getFriendsMulti().updateSharedScreen(getOwner(), false);
				} catch (InstanceNotOwnedException e) {
					e.printStackTrace();
				} catch (NoSuchVisitorException e) {
					e.printStackTrace();
				}
				break;
			case MessageCodes.In.Instance.LEFT:
				getFriendsMulti().updateSharedScreen(null, false);
				break;
			case SpecificMessageCodes.In.ACCEPT_GROUP:
				try {
					Vector friends = getFriendsMulti().createGroup(getOwner());
					if (friends.size() > 0) {
						Message reply = new Message(SpecificMessageCodes.Out.GROUP_CREATED, getOwner().getVisitorHost(), Network.mobilePacketPort);
						Enumeration e = friends.elements();
						while (e.hasMoreElements()) {
							Visitor friend = (Visitor) e.nextElement();
							reply.addMessagePart(friend.getVisitorName());
						}
						try {
							reply.send(getFriendsMulti().getMessenger());
						} catch (MessageNotSentException e1) {
							e1.printStackTrace();
						}
					}
				} catch (InstanceNotOwnedException e) {
					e.printStackTrace();
				} catch (NoSuchVisitorException e) {
					e.printStackTrace();
				}
				break;
			default:
				System.out.println("Unrecognised message code (" + receivedMessage.getMessageCode() + "); ignoring");
				handled = false;
				break;
			}
		//}
		return handled;
	}
		
}
