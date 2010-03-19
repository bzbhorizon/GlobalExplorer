package bzb.se.installations.instances;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import bzb.se.adverts.Advert;
import bzb.se.adverts.POIAdvert;
import bzb.se.exceptions.InstallationNotFoundException;
import bzb.se.exceptions.InstanceNotOwnedException;
import bzb.se.exceptions.MessageNotSentException;
import bzb.se.exceptions.NoSuchVisitorException;
import bzb.se.macroenvironment.EnvironmentMap;
import bzb.se.messaging.Message;
import bzb.se.meta.Installations;
import bzb.se.meta.MessageCodes;
import bzb.se.meta.Network;
import bzb.se.visitors.Visitor;

public class VisitorMonitor extends Instance {

	private class InstallationSummary {
		public String installationName;
		
		public String installationHost;
		
		public int installationPort;
		
		public InstallationSummary (String desiredInstallationName) throws InstallationNotFoundException {
			installationName = desiredInstallationName;
			
			System.out
				.println("Checking for installations registered with name " + desiredInstallationName);

			try {
				ResultSet rs = multi.db.stmt
					.executeQuery("SELECT * FROM installations WHERE instanceName='"
						+ desiredInstallationName
						+ "'");
				if (rs.next()) {
					System.out
							.println("Installation found with name " + desiredInstallationName);

					installationHost = rs.getString("host");
					installationPort = rs.getInt("port");
				} else {
					throw (new InstallationNotFoundException());
				}
			} catch (SQLException e) {
				throw (new InstallationNotFoundException());
			}
		}
		
		public int getFreeCapacity () {
			int freeCapacity = 0;
			
			System.out
				.println("Checking for installations registered with name " + installationName);

			ResultSet rs;
			try {
				rs = multi.db.stmt
					.executeQuery("SELECT * FROM installations WHERE instanceName='"
						+ installationName
						+ "'");
			
				if (rs.next()) {
					freeCapacity = rs.getInt("freeCapacity");
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			return freeCapacity;
		}
		
	}
	
	private Vector generateSuitableAdvertsFor (Visitor visitor, String selectedInstallation) {
		Vector adverts = new Vector();
		
		ResultSet rs;
		Vector installations = null;
		try {
			if (selectedInstallation != null) {
				rs = multi.db.stmt
					.executeQuery("SELECT * FROM installations WHERE instanceName='" + selectedInstallation + "' AND NOT instanceName='" + Installations.VISITOR_MONITOR_ID + "'");
			} else {
				rs = multi.db.stmt.executeQuery("SELECT * FROM installations WHERE NOT instanceName='" + Installations.VISITOR_MONITOR_ID + "'");
			}
		
			installations = new Vector();
			while (rs.next()) {
				installations.addElement(rs.getString("instanceName"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		Iterator i = installations.iterator();
		while (i.hasNext()) {
			String installation = (String) i.next();
			Advert advert;
			try {
				advert = new Advert(installation, visitor, multi.db);
				System.out.println("Advert for " + advert.getInstanceName() + " has priority " + advert.getPriority());
				if (advert.getPriority() != Advert.Priorities.NEVER_SUITABLE) {
					adverts.addElement(advert);
				}
			} catch (InstallationNotFoundException e) {
				e.printStackTrace();
			} catch (NoSuchVisitorException e) {
				e.printStackTrace();
			}
		}
		
		adverts.addAll(getPOIList());
		
		Collections.sort(adverts);
		
		return adverts;
	}
	
	private Vector getPOIList () {
		Vector pois = new Vector();
		Vector temp = new Vector();		
		
		new EnvironmentMap(multi.db); // refresh POI list in db
		
		try {
			ResultSet rs = multi.db.stmt.executeQuery("SELECT * FROM noninstallations");
			while (rs.next()) {
				temp.addElement(rs.getString("poiId"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		Enumeration e = temp.elements();
		while (e.hasMoreElements()) {
			pois.addElement(new POIAdvert((String) e.nextElement(), getMulti().db));
		}
	
		return pois;
	}
	
	private Vector getFriendsListOf (Visitor visitor) {
		Vector friends = new Vector();
		
		ResultSet rs;
		try {
		/*	rs = multi.db.stmt
					.executeQuery("SELECT host from visitors WHERE NOT host='" + ownerHost + "'");

			Vector temp = new Vector();
			while (rs.next()) {
				temp.addElement(rs.getString("host"));
			}
			
			if (temp.size() > 0) {
				Iterator i = temp.iterator();
				while (i.hasNext()) {
					try {
						Visitor friend = new Visitor((String) i.next(), getMulti().db);	
						if (!friends.contains(friend)) {
							friends.addElement(friend);
						}
					} catch (NoSuchVisitorException e) {
						e.printStackTrace();
					}
				}
			}
*/
			rs = multi.db.stmt
					.executeQuery("SELECT hostB FROM friends WHERE hostA='" + visitor.getVisitorHost() + "'");
		
			while (rs.next()) {
				Visitor friend;
				try {
					friend = new Visitor(rs.getString("hostB"), getMulti().db);
					if (!friends.contains(friend)) {
						friends.addElement(friend);
					}
				} catch (NoSuchVisitorException e) {
					e.printStackTrace();
				}
			}
			
			rs = multi.db.stmt
					.executeQuery("SELECT hostA FROM friends WHERE hostB='" + visitor.getVisitorHost() + "'");
		
			while (rs.next()) {
				try {
					Visitor friend = new Visitor(rs.getString("hostA"), getMulti().db);
					if (!friends.contains(friend)) {
						friends.addElement(friend);
					}
				} catch (NoSuchVisitorException e) {
					e.printStackTrace();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return friends;
	}
	
	private void sendAdvertsTo (Vector adverts, String expectantHost) {
		try {		
			Socket sock = new Socket(expectantHost,
					Network.mobileVisitorMonitorPort);

			DataOutputStream dos = new DataOutputStream(sock
					.getOutputStream());
			
			System.out.println("Started sending adverts");
			
			if (adverts != null) {
				Iterator i = adverts.iterator();
				while (i.hasNext()) {
					Advert advert = (Advert) i.next();
					dos.writeInt(advert.getPriority());
					dos.writeUTF(advert.getInstanceName());
					dos.writeUTF(advert.getDescription());
					System.out.println("Sent advert for " + advert.getInstanceName() + " (" + advert.getPriority() + ")");
				}
			}
			
			dos.writeInt(Advert.Priorities.NEVER_SUITABLE);
			dos.writeInt(Advert.Priorities.NEVER_SUITABLE);
			dos.writeInt(Advert.Priorities.NEVER_SUITABLE);
			dos.writeInt(Advert.Priorities.NEVER_SUITABLE);
			
			System.out.println("Finished sending adverts");
			
			dos.flush();
			dos.close();
			sock.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void sendFriendsListTo (Vector friends, String expectantHost) {
		try {		
			Socket sock = new Socket(expectantHost,
					Network.mobileVisitorMonitorPort);

			DataOutputStream dos = new DataOutputStream(sock
					.getOutputStream());
			
			System.out.println("Started sending friends");
			
			if (friends != null) {
				Iterator i = friends.iterator();
				while (i.hasNext()) {
					Visitor friend = (Visitor) i.next();
					dos.writeInt(friend.getVisitorAge());
					dos.writeUTF(friend.getVisitorName());
					dos.writeUTF(friend.getVisitorLastKnownPosition());
					System.out.println("Sent advert for " + friend.getVisitorName() + " (" + friend.getVisitorAge() + ")");
				}
			}
			
			dos.writeInt(-1);
			dos.writeInt(-1);
			dos.writeInt(-1);
			dos.writeInt(-1);
			
			System.out.println("Finished sending friends");
			
			dos.flush();
			dos.close();
			sock.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean handleMessage (Message receivedMessage) {
		boolean handled = super.handleMessage(receivedMessage);
		
		if (!handled) {
			handled = true;
			switch (receivedMessage.getMessageCode()) {
			// case 0: by multi
			// case 1: by multi
			case MessageCodes.In.Instance.VisitorMonitor.REGISTER_CAPABILITY_PROFILE:
				Visitor newVisitor;
				try {
					newVisitor = new Visitor(receivedMessage.getHost(), multi.db);
					newVisitor.setVisitorHasBT(Boolean.parseBoolean((String)receivedMessage.getMessageParts().elementAt(0)));
					newVisitor.setVisitorHasFaceCamera(Boolean.parseBoolean((String)receivedMessage.getMessageParts().elementAt(1)));
					newVisitor.setVisitorHasMainCamera(Boolean.parseBoolean((String)receivedMessage.getMessageParts().elementAt(2)));
					newVisitor.setVisitorScreenDimensions(new int[]{Integer.parseInt((String) receivedMessage.getMessageParts().elementAt(3)), Integer.parseInt((String) receivedMessage.getMessageParts().elementAt(4))});
					newVisitor.updateVisitorInDB(multi.db);
				} catch (NoSuchVisitorException e2) {
					System.out.println("Unexpected exception (no such visitor: " + receivedMessage.getHost() + "); ignoring");
				}
				break;
			case MessageCodes.In.Instance.VisitorMonitor.LOCATE_INSTALLATION:
				String installationToLocate = (String) receivedMessage.getMessageParts().elementAt(0);
				System.out.println("Visitor on " + receivedMessage.getHost() + " wants host and port for " + installationToLocate);
				try {
					InstallationSummary desiredInstallation = new InstallationSummary(installationToLocate);
					Message reply = new Message(MessageCodes.Out.Instance.VisitorMonitor.INSTALLATION_LOCATION, receivedMessage.getHost(), receivedMessage.getPort());
					reply.addMessagePart(desiredInstallation.installationName);
					reply.addMessagePart(desiredInstallation.installationHost);
					reply.addMessagePart(String.valueOf(desiredInstallation.installationPort));
					reply.send(multi.getMessenger());
					
					// send directions
				} catch (MessageNotSentException e) {
					System.out.println("Unable to reply with desired installation location; ignoring");
					try {
						Message reply = new Message(MessageCodes.Out.Instance.VisitorMonitor.LOCATION_UNKNOWN, receivedMessage.getHost(), receivedMessage.getPort());
						reply.addMessagePart(installationToLocate);
						reply.send(multi.getMessenger());
					} catch (MessageNotSentException e1) {
						System.out.println("Unable to reply negatively; unknown");
					}
				} catch (InstallationNotFoundException e) {
					System.out.println("Unable to locate desired installation; ignoring");
					try {
						Message reply = new Message(MessageCodes.Out.Instance.VisitorMonitor.LOCATION_UNKNOWN, receivedMessage.getHost(), receivedMessage.getPort());
						reply.addMessagePart(installationToLocate);
						reply.send(multi.getMessenger());
					} catch (MessageNotSentException e1) {
						System.out.println("Unable to reply negatively; unknown");
					}
				}
				break;
			case MessageCodes.In.Instance.VisitorMonitor.PULL_ADVERTS:
				Vector adverts = null;
				
				if (receivedMessage.getMessageParts().size() > 0) {
					String selectedInstallation = (String) receivedMessage.getMessageParts().firstElement();
					System.out.println("Visitor on " + receivedMessage.getHost() + " has selected " + selectedInstallation);
					try {
						adverts = generateSuitableAdvertsFor(new Visitor(receivedMessage.getHost(), multi.db), selectedInstallation);
					} catch (NoSuchVisitorException e) {
						System.out.println("Unexpected exception (no such visitor: " + receivedMessage.getHost() + "); ignoring");
					}
				} else {
					System.out.println("Visitor on " + receivedMessage.getHost() + " is pulling adverts");
					try {
						adverts = generateSuitableAdvertsFor(new Visitor(receivedMessage.getHost(), multi.db), null);
					} catch (NoSuchVisitorException e) {
						System.out.println("Unexpected exception (no such visitor: " + receivedMessage.getHost() + "); ignoring");
					}
				}
				
				sendAdvertsTo(adverts, receivedMessage.getHost());
				break;
			case MessageCodes.In.Instance.VisitorMonitor.PULL_DIRECTIONS:
				Vector directions = null;
				
				String destination = (String) receivedMessage.getMessageParts().elementAt(0);
				System.out.println("Visitor on " + receivedMessage.getHost() + " is pulling directions to " + destination);
				try {
					directions = generateSuitableDirectionsTo(destination, new Visitor(receivedMessage.getHost(), getMulti().db));
				} catch (NoSuchVisitorException e1) {
					System.out.println("Unexpected exception (no such visitor: " + receivedMessage.getHost() + "); ignoring");
				}
							
				sendDirectionsTo(directions, receivedMessage.getHost());
				
				if (((String)receivedMessage.getMessageParts().elementAt(1)).equals("2")) {
					Visitor owner;
					try {
						owner = getOwner();
						owner.addCompleteExperience(destination);
						owner.updateVisitorInDB(getMulti().db);
					} catch (InstanceNotOwnedException e) {
						e.printStackTrace();
					} catch (NoSuchVisitorException e) {
						e.printStackTrace();
					}
				}
				break;
			case MessageCodes.In.Instance.VisitorMonitor.PULL_POIS:
				Vector pois = getPOIList();
				System.out.println("Visitor on " + receivedMessage.getHost() + " is pulling list of pois");
				
				sendAdvertsTo(pois, receivedMessage.getHost());
				break;
			case MessageCodes.In.Instance.VisitorMonitor.UPDATE_LOCATION:
				try {
					Visitor movedVisitor = new Visitor(receivedMessage.getHost(), getMulti().db);
					movedVisitor.setVisitorLastKnownPosition((String) receivedMessage.getMessageParts().firstElement());
					movedVisitor.updateVisitorInDB(getMulti().db);
				} catch (NoSuchVisitorException e) {
					System.out.println("Unexpected exception (no such visitor: " + receivedMessage.getHost() + "); ignoring");
				}
				break;
			case MessageCodes.In.Instance.LEFT:
				try {
					Visitor leavingVisitor = new Visitor(receivedMessage.getHost(), getMulti().db);
					leavingVisitor.unregisterFromDB(getMulti().db);
				} catch (NoSuchVisitorException e) {
					System.out.println("No such visitor found in ME to unregister; ignoring");
				}
				break;
			case MessageCodes.In.Instance.VisitorMonitor.PULL_FRIENDS:
				Vector friends = null;
				
				System.out.println("Visitor on " + receivedMessage.getHost() + " is pulling friends");
				try {
					friends = getFriendsListOf(new Visitor(receivedMessage.getHost(), getMulti().db));
				} catch (NoSuchVisitorException e1) {
					System.out.println("Unexpected exception (no such visitor: " + receivedMessage.getHost() + "); ignoring");
				}
							
				sendFriendsListTo(friends, receivedMessage.getHost());
				break;
			default:
				System.out.println("Message code (" + receivedMessage.getMessageCode() + ") unrecognised by VisitorMonitor; ignoring");
				handled = false;
				break;
			}
		}
		return handled;
	}
	
}
