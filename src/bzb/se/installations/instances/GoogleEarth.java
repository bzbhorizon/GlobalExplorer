package bzb.se.installations.instances;

import java.util.Enumeration;
import java.util.Vector;

import bzb.se.exceptions.InstanceNotOwnedException;
import bzb.se.exceptions.NoSuchVisitorException;
import bzb.se.messaging.Message;
import bzb.se.meta.MessageCodes;
import bzb.se.utility.ImageProcessing;
import bzb.se.meta.Device;
import bzb.se.meta.Network;
import bzb.se.visitors.Visitor;

import java.awt.Robot;
import bzb.se.meta.Packages;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class GoogleEarth extends Instance {

	public GoogleEarth () {
		super();
	}
	
	public boolean setPortAndStart (int desiredPort) {
		boolean portSet = super.setPortAndStart(desiredPort);
		
		if (portSet) {
			// ballgame specific stuff
		}
		return portSet;
	}
	
	public final class SpecificMessageCodes {
		public final class In {
			public static final int START_BALL = 500;
			public static final int POSITION = 501;
			public static final int RESET = 502;
			public static final int ZOOM_IN = 503;
			public static final int ZOOM_OUT = 504;
			public static final int LEFT = 505;
			public static final int RIGHT = 506;
			public static final int UP = 507;
			public static final int DOWN = 508;
			public static final int PLAY = 509;
			public static final int SNAPSHOT = 511;
		}
		
		public final class Out {
		}
	}
	
	public bzb.se.installations.GoogleEarth getGoogleEarthMulti () {
		return (bzb.se.installations.GoogleEarth) getMulti();
	}
			
	private Vector currentSeeds;

	private long lastPositionUpdate = 0;
	
	public boolean handleMessage (Message receivedMessage) {
		boolean handled = super.handleMessage(receivedMessage);
		
		//if (!handled) {
			handled = true;
			switch (receivedMessage.getMessageCode()) {
			case SpecificMessageCodes.In.START_BALL:
				completeExperience();
				
				currentSeeds = new Vector();
			
				if (receivedMessage.getMessageParts().size() > 0) {
					Vector seeds = receivedMessage.getMessageParts();
					Enumeration en = seeds.elements();
					while (en.hasMoreElements()) {
						int code = Integer.parseInt((String) en.nextElement());
						currentSeeds.addElement(new Integer(code));
						System.out.println("Visitor on " + receivedMessage.getHost() + " has seed " + code);
					}

					try {
						getGoogleEarthMulti().displayKeys(getOwner(), currentSeeds);
						getGoogleEarthMulti().resetGoogleEarth(false);
					} catch (InstanceNotOwnedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (NoSuchVisitorException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				System.out.println("Received " + currentSeeds.size() + " seeds");
				break;
			case SpecificMessageCodes.In.POSITION:
				//if ((System.currentTimeMillis() - lastPositionUpdate) > 150) {
				//	lastPositionUpdate = System.currentTimeMillis();
						getGoogleEarthMulti().updatePosition(receivedMessage.getMessageParts());
						getGoogleEarthMulti().updateGoogleEarth();
				//}
				break;
			case SpecificMessageCodes.In.RESET:
				getGoogleEarthMulti().resetGoogleEarth(false);
				break;
			case SpecificMessageCodes.In.ZOOM_IN:

						getGoogleEarthMulti().zoomIn();

				break;
			case SpecificMessageCodes.In.ZOOM_OUT:

						getGoogleEarthMulti().zoomOut();

				break;
			case SpecificMessageCodes.In.LEFT:

						getGoogleEarthMulti().moveLeft();

				break;
			case SpecificMessageCodes.In.RIGHT:

						getGoogleEarthMulti().moveRight();

				break;
			case SpecificMessageCodes.In.UP:

						getGoogleEarthMulti().moveUp();

				break;
			case SpecificMessageCodes.In.DOWN:

						getGoogleEarthMulti().moveDown();

				break;
			case MessageCodes.In.Instance.LEFT:
				getGoogleEarthMulti().resetGoogleEarth(true);
				break;
			case SpecificMessageCodes.In.PLAY:
				getGoogleEarthMulti().startRadio();
				break;
			case SpecificMessageCodes.In.SNAPSHOT:
				try {
        				Robot robot = new Robot();
					Rectangle captureSize = new Rectangle(2100, 100, 664, 600);
        				BufferedImage imageOriginal = robot.createScreenCapture(captureSize);
			String imageURL = Packages.outputDir + "tmp.png";
			File imageFile = new File(imageURL);
			try {
				ImageIO.write(imageOriginal, "PNG", imageFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
					
		// send
		try {
			Visitor owner = getOwner();
			Socket sock = new Socket(owner.getVisitorHost(), Network.mobileInstancePort);

			DataOutputStream dos = new DataOutputStream(sock
					.getOutputStream());
			
			System.out.println("Started sending snapshot to " + owner.getVisitorHost());
			
			int desiredImageWidth = (int) ((double) owner.getVisitorScreenDimensions()[0] * Device.MOBILE_USEFUL_IMAGE_SCALE);
			int desiredImageHeight = (int) ((double) owner.getVisitorScreenDimensions()[1] * Device.MOBILE_USEFUL_IMAGE_SCALE);
			System.out.println(desiredImageWidth + " " + desiredImageHeight);
			double xScale = (double) desiredImageWidth / (double) imageOriginal.getWidth();
			double yScale = (double) desiredImageHeight / (double) imageOriginal.getHeight();
			System.out.println(xScale + " " + yScale);
			if (xScale < yScale) {
				desiredImageHeight = (int) (imageOriginal.getHeight() * xScale);
			} else {
				desiredImageWidth = (int) (imageOriginal.getWidth() * yScale);
			}
			System.out.println(desiredImageWidth + " " + desiredImageHeight);
			
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			try {
				ImageIO.write(ImageProcessing.resize(
						imageOriginal,
						desiredImageWidth,
						desiredImageHeight
					),"JPEG", stream);
			} catch (Exception e) {
				System.out.println ("my pic did not byte erize");
			}
			byte[] imageBytes = stream.toByteArray();
			dos.writeInt(imageBytes.length);
			dos.write(imageBytes);
			
			System.out.println("Finished sending slide");
			getGoogleEarthMulti().showHTML("<p style=\"font-family: arial; font-size: 100pt;\">SNAPSHOT TRANSFERRED TO YOUR MOBILE WALLET</p>");
			
			dos.flush();
			dos.close();
			sock.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InstanceNotOwnedException e) {
			e.printStackTrace();
		} catch (NoSuchVisitorException e) {
			e.printStackTrace();
		}


				} catch(AWTException e) {
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
