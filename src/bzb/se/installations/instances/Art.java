package bzb.se.installations.instances;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import javax.imageio.ImageIO;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import bzb.se.DB;
import bzb.se.exceptions.InstanceNotOwnedException;
import bzb.se.exceptions.NoSuchVisitorException;
import bzb.se.messaging.Message;
import bzb.se.meta.Network;
import bzb.se.meta.Packages;
import bzb.se.utility.ImageProcessing;
import bzb.se.utility.XMLProcessor;

public class Art extends Instance {
	
	public boolean setPortAndStart (int desiredPort) {
		return super.setPortAndStart(desiredPort);
	}
	
	private final static double USEFUL_TAG_SCALE = 0.90;
	
	private final static String tagURL = "tag.png";
	
	public final class SpecificMessageCodes {
		public final class In {
			public static final int PULL_TAGS = 500;
		}
	}
	
	public Vector getTagPair (int code) {
		ARTXMLProcessor ARTXML = new ARTXMLProcessor(code, getMulti().db);
		
		Vector tagPair = new Vector();
		tagPair.add(ARTXML.countryName);
		
		try {
			int desiredMaxWidth = (int) Math.round(((double) getOwner().getVisitorScreenDimensions()[0] * USEFUL_TAG_SCALE));
			int desiredMaxHeight = (int) Math.round(((double) getOwner().getVisitorScreenDimensions()[1] * USEFUL_TAG_SCALE));
			
			System.out.println("Limiting tag for visitor on " + getOwner().getVisitorHost() + " to " + desiredMaxWidth + "px by " + desiredMaxHeight + "px");
			
			String filename = Packages.contentDir + code + "/Art/";
			if (getOwner().getVisitorAge() > 15) {
				filename += Packages.adultDir + tagURL;
			} else {
				filename += Packages.childDir + tagURL;
			}
			System.out.println(filename);
			BufferedImage originalImage = ImageIO.read(new File(filename));
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
			tagPair.addElement(ImageProcessing.resize(originalImage, desiredWidth, desiredHeight));
		} catch (InstanceNotOwnedException e) {
			e.printStackTrace();
		} catch (NoSuchVisitorException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return tagPair;
	}
	
	public void sendTagsTo (Vector tagPairs, String expectantHost) {
		try {		
			Socket sock = new Socket(expectantHost, Network.mobileInstancePort);

			DataOutputStream dos = new DataOutputStream(sock
					.getOutputStream());
			
			System.out.println("Started sending tags back to " + expectantHost);
			
			if (tagPairs != null) {
				Iterator i = tagPairs.iterator();
				while (i.hasNext()) {
					Vector tagPair = (Vector) i.next();
					
					ByteArrayOutputStream stream = new ByteArrayOutputStream();
					try {
						ImageIO.write((BufferedImage) tagPair.elementAt(1),"JPEG", stream);
					} catch (Exception e) {
						System.out.println ("my pic did not byte erize");
					}
					byte[] imageBytes = stream.toByteArray();
					dos.writeInt(imageBytes.length);
					dos.write(imageBytes);
					dos.writeUTF((String) tagPair.elementAt(0));
					
					System.out.println("Sent tag");
				}
			}
			
			dos.writeInt(0);
			
			System.out.println("Finished sending tags");
			completeExperience();
			
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
			case SpecificMessageCodes.In.PULL_TAGS:
				Vector tagPairs = new Vector();
				
				Vector seeds = receivedMessage.getMessageParts();
				Enumeration e = seeds.elements();
				while (e.hasMoreElements()) {
					int code = Integer.parseInt((String) e.nextElement());
					
					Vector tagPair = getTagPair(code);
					System.out.println("Visitor on " + receivedMessage.getHost() + " is pulling tag for " + (String) tagPair.get(0));
					
					tagPairs.addElement(tagPair);
				}
							
				sendTagsTo(tagPairs, receivedMessage.getHost());
				break;
			default:
				System.out.println("Unrecognised message code (" + receivedMessage.getMessageCode() + "); ignoring");
				handled = false;
				break;
			}
		}
		return handled;
	}
	
	public class ARTXMLProcessor extends XMLProcessor {

		private int targetCode;
		
		private String countryName = null;
		
		public ARTXMLProcessor(int code, DB db) {
			targetCode = code;
			parseXML();
		}
		
		public void parseXML () {
			super.parseXML(Packages.repositoryDataURL);
		}
		
		public void startElement(String namespaceURI, String localName,
				String qualifiedName, Attributes atts) throws SAXException {
			if (localName.equals("content")) {
				if (atts.getValue("code") != null && Integer.parseInt(atts.getValue("code")) == targetCode) {
					if (atts.getValue("name") != null) {
						countryName = atts.getValue("name");
					}
				}
			}
		}

		public void endElement(String namespaceURI, String localName,
				String qualifiedName) throws SAXException {}

		public void characters(char[] ch, int start, int length)
				throws SAXException {}
		
	}
	
}
