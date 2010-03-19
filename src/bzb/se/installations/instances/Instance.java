package bzb.se.installations.instances;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Vector;

import javax.imageio.ImageIO;

import bzb.se.exceptions.ConnectNotReadyException;
import bzb.se.exceptions.InstanceAlreadyOwnedException;
import bzb.se.exceptions.InstanceNotOwnedException;
import bzb.se.exceptions.MessageNotSentException;
import bzb.se.exceptions.NoSuchPointOfInterestException;
import bzb.se.exceptions.NoSuchVisitorException;
import bzb.se.installations.Multi;
import bzb.se.macroenvironment.EnvironmentMap;
import bzb.se.messaging.Message;
import bzb.se.meta.Device;
import bzb.se.meta.MessageCodes;
import bzb.se.meta.Network;
import bzb.se.meta.Timings;
import bzb.se.utility.ImageProcessing;
import bzb.se.visitors.Visitor;

public abstract class Instance {
	
	private ServerSocket instSock;
	
	public String ownerHost;
	
	protected Multi multi;
		
	private OwnerTimeout ownerTimeout;
	
	private int role;
	
	public static class Roles {
		public static final int NO_ROLE = -1;
	}
	
	public Instance () {
		reset();
	}
	
	public void setMulti (Multi multi) {
		this.multi = multi;
	}
	
	public int getPort () throws ConnectNotReadyException {
		if (instSock != null) {
			return instSock.getLocalPort();
		} else {
			throw (new ConnectNotReadyException());
		}
	}
	
	public ServerSocket getInstanceServer () {
		return instSock;
	}
	
	public String getName () {
		return this.getClass().getSimpleName();
	}
	
	public int getRole () {
		return role;
	}
	
	public void setRole (int role) {
		this.role = role;
	}
	
	public boolean setPortAndStart (int desiredPort) {
		try {
			instSock = new ServerSocket(desiredPort);
			System.out.println("Instance takes port: " + instSock.getLocalPort());
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	
	public void setOwner (String newOwnerHost) throws InstanceAlreadyOwnedException {
		if (ownerHost == null) {
			ownerHost = newOwnerHost;
			ownerTimeout.reset();
			new Thread(ownerTimeout).start();
		} else {
			throw (new InstanceAlreadyOwnedException());
		}
	}
	
	public class OwnerTimeout implements Runnable {

		private long countingSince = 0;
		
		private boolean alertMulti = true;
		
		private boolean running = false;
		
		public OwnerTimeout () {
			countingSince = System.currentTimeMillis();
		}
		
		public long getInactivePeriod () {
			return System.currentTimeMillis() - countingSince;
		}
		
		public boolean isRunning () {
			return running;
		}
		
		public void run() {
			running = true;
			while (getInactivePeriod() < Timings.InstanceOwnerTimeout) {
				try {
					Thread.sleep(Timings.Minute);
					System.out.println(getOwner().getVisitorHost() + " has been inactive at " + getName() + " for " +  getInactivePeriod());
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (InstanceNotOwnedException e) {
					stop();
				} catch (NoSuchVisitorException e) {
					e.printStackTrace();
				}
			}
			if (alertMulti) {
				try {
					System.out.println(getOwner().getVisitorHost() + " kicked from instance " + getName() + " for " + Timings.InstanceOwnerTimeout + "ms inactivity");
					freeInstance();
				} catch (InstanceNotOwnedException e) {
					e.printStackTrace();
				} catch (NoSuchVisitorException e) {
					e.printStackTrace();
				}
			}
			running = false;
		}
		
		public void reset () {
			countingSince = System.currentTimeMillis();
		}
		
		public void dontAlert () {
			alertMulti = false;
		}
		
		public void doAlert () {
			alertMulti = true;
		}
		
		public void stop () {
			dontAlert();
			countingSince = 0;
		}
		
	}
	
	public Visitor getOwner () throws InstanceNotOwnedException, NoSuchVisitorException {
		if (ownerHost != null) {
			return new Visitor(ownerHost, getMulti().db);
		} else {
			throw (new InstanceNotOwnedException());
		}
	}
	
	public Multi getMulti () {
		if (multi != null) {
			return multi;
		}
		return null;
	}
	
	public void reset () {
		ownerHost = null;
		ownerTimeout = new OwnerTimeout();
		setRole(Roles.NO_ROLE);
	}
	
	public void close (boolean freeInstance) {
		if (freeInstance) {
			freeInstance();
		}
		
		ownerTimeout.stop();
		
		try {
			if (instSock != null) {
				instSock.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Instance closed");
	}
	
	public boolean ownerHasCompletedExperience () {
		Visitor owner;
		try {
			owner = getOwner();
		
			if (owner.getCompleteExperiences().contains(getName())) {
				return true;
			} else {
				return false;
			}
		} catch (NoSuchVisitorException e) {
			e.printStackTrace();
			return false;
		} catch (InstanceNotOwnedException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public void completeExperience () {
		if (!ownerHasCompletedExperience()) {
			try {				
				Visitor owner;
				try {
					owner = getOwner();
					System.out.println("Visitor " + owner.getVisitorHost() + " has completed experience");
					owner.addCompleteExperience(getName());
					owner.updateVisitorInDB(multi.db);
					
					Message completed = new Message(MessageCodes.Out.Instance.COMPLETED_EXPERIENCE, owner.getVisitorHost(), Network.mobilePacketPort);
					try {
						completed.send(multi.getMessenger());
					} catch (MessageNotSentException e) {
						e.printStackTrace();
					}
				} catch (NoSuchVisitorException e) {
					e.printStackTrace();
				}
			} catch (InstanceNotOwnedException e) {
				e.printStackTrace();
			}
		} else {
			try {
				System.out.println(getOwner().getVisitorHost() + " has already completed their experience at this instance; ignoring");
			} catch (InstanceNotOwnedException e) {
				e.printStackTrace();
			} catch (NoSuchVisitorException e) {
				e.printStackTrace();
			}
		}
	}
	
	public Vector generateSuitableDirectionsTo (Visitor visitor) {
		String from = visitor.getVisitorLastKnownPosition();
		String to = getName();
		return generateSuitableDirectionsTo (from, to, visitor);
	}
	
	public Vector generateSuitableDirectionsTo (String to, Visitor visitor) {
		String from = visitor.getVisitorLastKnownPosition();
		return generateSuitableDirectionsTo (from, to, visitor);
	}
	
	public Vector generateSuitableDirectionsTo (String from, String to, Visitor visitor) {
		Vector directions;
		Vector scaledDirections = null;
		EnvironmentMap eMap = new EnvironmentMap(getMulti().db);
		try {
			directions = eMap.getDirections(from, to, getMulti().db);
			
			scaledDirections = new Vector();
			if (directions.size() > 0) {
				int desiredMaxWidth = (int) Math.round(((double) visitor.getVisitorScreenDimensions()[0] * Device.MOBILE_USEFUL_IMAGE_SCALE));
				int desiredMaxHeight = (int) Math.round(((double) visitor.getVisitorScreenDimensions()[1] * Device.MOBILE_USEFUL_IMAGE_SCALE));
				
				System.out.println("Limiting direction steps for visitor on " + visitor.getVisitorHost() + " to " + desiredMaxWidth + "px by " + desiredMaxHeight + "px");
				
				Iterator i = directions.iterator();
				while (i.hasNext()) {
					Vector step = (Vector) i.next();
					BufferedImage originalImage = (BufferedImage) step.elementAt(1);
					double widthScale = (double)originalImage.getWidth() / (double)desiredMaxWidth;
					double heightScale = (double)originalImage.getHeight() / (double)desiredMaxHeight;
					System.out.println(widthScale + " " + heightScale);
					if (widthScale > heightScale) {
						heightScale = widthScale;
					} else {
						widthScale = heightScale;
					}
					int desiredWidth = (int) ((double)originalImage.getWidth() / widthScale);
					int desiredHeight = (int) ((double)originalImage.getHeight() / heightScale);
					System.out.println(desiredWidth + " " + desiredHeight);
					
					Vector scaledDirection = new Vector();
					scaledDirection.addElement(step.elementAt(0));
					scaledDirection.addElement(ImageProcessing.resize(originalImage, desiredWidth, desiredHeight));
					scaledDirections.addElement(scaledDirection);
				}
			}
		} catch (NoSuchPointOfInterestException e) {
			e.printStackTrace();
		}

		return scaledDirections;
	}
	
	public void sendDirectionsTo (Vector directions, String expectantHost) {
		try {		
			Socket sock = new Socket(expectantHost, Network.mobileVisitorMonitorPort);

			DataOutputStream dos = new DataOutputStream(sock
					.getOutputStream());
			
			System.out.println("Started sending directions back to " + expectantHost);
			
			if (directions != null) {
				Iterator i = directions.iterator();
				while (i.hasNext()) {
					Vector step = (Vector) i.next();
					
					ByteArrayOutputStream stream = new ByteArrayOutputStream();
					try {
						ImageIO.write((BufferedImage) step.elementAt(1),"JPEG", stream);
					} catch (Exception e) {
						System.out.println ("my pic did not byte erize");
					}
					byte[] imageBytes = stream.toByteArray();
					dos.writeInt(imageBytes.length);
					dos.write(imageBytes);
					dos.writeUTF((String) step.elementAt(0));
					
					System.out.println("Sent step");
				}
			}
			
			dos.writeInt(0);
			dos.writeInt(0);
			dos.writeInt(0);
			dos.writeInt(0);
			
			System.out.println("Finished sending directions");
			
			dos.flush();
			dos.close();
			sock.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void freeInstance () {
		reset();
		getMulti().updateFreeCapacityInDB();
	}
	
	public boolean handleMessage (Message receivedMessage) {
		boolean handled = true;
		
		// instances should only handle messages from their owner?
		try {
			if (receivedMessage.getHost().equals(ownerHost)) {
				switch (receivedMessage.getMessageCode()) {
				case MessageCodes.In.Instance.ACTIVITY:
					if (ownerTimeout.isRunning()) {
						System.out.println(receivedMessage.getHost() + " is active at " + getName() + "; resetting owner timeout");
						ownerTimeout.reset();
					}
					break;
				case MessageCodes.In.Instance.PULL_DIRECTIONS:
					Vector directions = null;
					
					String destination = (String) receivedMessage.getMessageParts().elementAt(0);
					if (!destination.equals(getName())) {
						System.out.println("Unexpected: visitor intends to pull directions to an installation other than this one (" + destination + ")");
					} else {
						System.out.println("Visitor on " + receivedMessage.getHost() + " is pulling directions");
						try {
							directions = generateSuitableDirectionsTo(new Visitor(receivedMessage.getHost(), getMulti().db));
						} catch (NoSuchVisitorException e1) {
							System.out.println("Unexpected exception (no such visitor: " + receivedMessage.getHost() + "); ignoring");
						}
					}
					
					sendDirectionsTo(directions, receivedMessage.getHost());
					
					if (((String)receivedMessage.getMessageParts().elementAt(1)).equals("2")) {
						Visitor owner = getOwner();
						owner.addCompleteExperience(destination);
						owner.updateVisitorInDB(getMulti().db);
					}
					break;
				case MessageCodes.In.Instance.COMPLETED_EXPERIENCE:
					String visitorCompletedExperienceAt = (String) receivedMessage.getMessageParts().elementAt(0);
					if (visitorCompletedExperienceAt.equals(getName())) {
						if (getOwner().getVisitorHost().equals(receivedMessage.getHost())) {
							completeExperience();
						} else {
							System.out.println("Unexpected exception (visitor is not the owner); ignoring");
						}
					} else {
						System.out.println("Unexpected exception: visitor is telling us that it completed an experience at some other installation (" + visitorCompletedExperienceAt + "); ignoring");
					}
					break;
				case MessageCodes.In.Instance.ARRIVED:
					System.out.println(getOwner().getVisitorHost() + " has arrived and is coupling");
					break;
				case MessageCodes.In.Instance.COUPLED:
					System.out.println(getOwner().getVisitorHost() + " has coupled");
					break;
				case MessageCodes.In.Instance.DECOUPLED:
					System.out.println(getOwner().getVisitorHost() + " has decoupled");
					break;
				case MessageCodes.In.Instance.LEFT:
					System.out.println(getOwner().getVisitorHost() + " has left");
					freeInstance();
					handled = false;
					break;
				case MessageCodes.In.Instance.DECOUPLING:
					System.out.println(getOwner().getVisitorHost() + " is decoupling");
					break;
				default:
					System.out.println("Message code (" + receivedMessage.getMessageCode() + ") unrecognised by Instance; ignoring");
					handled = false;
					break;
				}
			} else {
				handled = false;
				System.out.println("Message from someone (" + receivedMessage.getHost() + ") who isn't the owner; ignoring");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return handled;
	}
	
}
