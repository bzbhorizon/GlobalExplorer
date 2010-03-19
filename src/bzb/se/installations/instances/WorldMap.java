package bzb.se.installations.instances;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import bzb.se.DB;
import bzb.se.exceptions.InstanceNotOwnedException;
import bzb.se.exceptions.NoSuchVisitorException;
import bzb.se.messaging.Message;
import bzb.se.meta.Network;
import bzb.se.meta.Packages;
import bzb.se.utility.XMLProcessor;

public class WorldMap extends Instance {

	public boolean setPortAndStart (int desiredPort) {
		boolean portSet = super.setPortAndStart(desiredPort);
		
		if (portSet) {
			// globe specific stuff
			resetCurrentDiscovery();
		}
		return portSet;
	}
	
	// current discovery
	private int countryCode = COUNTRY_NONE;

	private String countryName = null;
	
	private String countryFact = null;
	
	private boolean isSeed = false;
	
	private static final int COUNTRY_NONE = -1;
	
	private void resetCurrentDiscovery () {
		countryCode = COUNTRY_NONE;
		countryName = null;
		countryFact = null;
		isSeed = false;
	}
	
	public final class SpecificMessageCodes {
		public final class In {
			public static final int TAG_CAPTURED = 500;
		}
	}
	
	private void foundCountry() {
		Socket sock = null;
		DataOutputStream dos = null;
		try {
			sock = new Socket(getOwner().getVisitorHost(),
					Network.mobileVisitorMonitorPort);
			
			dos = new DataOutputStream(sock.getOutputStream());

			System.out.println("Sending content");
			
			if (countryCode == COUNTRY_NONE) {
				dos.writeInt(COUNTRY_NONE);
			} else {
				dos.writeInt(countryCode);
				dos.writeUTF(countryName);
				dos.writeUTF(countryFact);
				dos.writeBoolean(isSeed);
			}
			
			System.out.println("Finished sending content");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InstanceNotOwnedException e) {
			e.printStackTrace();
		} catch (NoSuchVisitorException e) {
			e.printStackTrace();
		} finally {
			try {
				if (dos != null) {
					dos.flush();
					dos.close();
				}
				if (sock != null) {
					sock.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public boolean handleMessage (Message receivedMessage) {
		boolean handled = super.handleMessage(receivedMessage);
		
		if (!handled) {
			handled = true;
			switch (receivedMessage.getMessageCode()) {
			case SpecificMessageCodes.In.TAG_CAPTURED:
				System.out.println("Waiting to receive location capture");

				countryCode = Integer.parseInt((String)receivedMessage.getMessageParts().firstElement());
				System.out.println("Visitor found country " + countryCode);
				
				if (countryCode == COUNTRY_NONE) {
					System.out.println("Visitor found nothing useful");
				} else {						
					new WorldMapXMLProcessor(getMulti().db);

					if (countryName != null) {
						// experience is only complete if visitor found one of the places with a slideshow
						if (isSeed) {
							completeExperience();
						}
					} else {
						System.out.println("Visitor found nothing useful");					
						countryCode = COUNTRY_NONE;
					}
				}

				// do summat with country
				foundCountry();
				
				resetCurrentDiscovery();
				break;
			default:
				System.out.println("Unrecognised message code (" + receivedMessage.getMessageCode() + "); ignoring");
				handled = false;
				break;
			}
		}
		return handled;
	}
	
	public class WorldMapXMLProcessor extends XMLProcessor {

		public WorldMapXMLProcessor(DB db) {
			parseXML();
		}
		
		public void parseXML () {
			super.parseXML(Packages.repositoryDataURL);
		}
		
		private boolean inRepository = false;

		private boolean inContent = false;
		
		private boolean inFact = false;
		
		private boolean inDesiredContent = false;
		
		private String countryFactChild = null;
		
		private String countryFactAdult = null;
		
		public void startElement(String namespaceURI, String localName,
				String qualifiedName, Attributes atts) throws SAXException {
			if (localName.equals("repository")) {
				inRepository = true;
			} else if (localName.equals("content")) {
				inContent = true;
				
				if (atts.getValue("code") != null && Integer.parseInt(atts.getValue("code")) == countryCode) {
					inDesiredContent = true;
					
					if (atts.getValue("name") != null) {
						countryName = atts.getValue("name");
					}
					
					if (atts.getValue("isSeed") != null) {
						isSeed = Boolean.parseBoolean(atts.getValue("isSeed"));
					}
				}
			} else if (localName.equals("fact")) {
				inFact = true;
				
				if (atts.getValue("ageGroup") != null) {
					if (atts.getValue("ageGroup").equals("adult")) {
						countryFactAdult = atts.getValue("ageGroup");
					} else if (atts.getValue("ageGroup").equals("child")) {
						countryFactChild = atts.getValue("ageGroup");
					}
				}
			}
		}

		public void endElement(String namespaceURI, String localName,
				String qualifiedName) throws SAXException {
			if (localName.equals("repository")) {
				inRepository = false;
				
				try {
					if (getOwner().getVisitorAge() > 15 && countryFactAdult != null) {
						countryFact = countryFactAdult;
					} else if (getOwner().getVisitorAge() <= 15 && countryFactChild != null) {
						countryFact = countryFactChild;
					}
				} catch (InstanceNotOwnedException e) {
					e.printStackTrace();
				} catch (NoSuchVisitorException e) {
					e.printStackTrace();
				}
			} else if (localName.equals("content")) {
				inContent = false;
				
				if (inDesiredContent) {
					inDesiredContent = false;
				}
			} else if (localName.equals("fact")) {
				inFact = false;
			}
		}

		public void characters(char[] ch, int start, int length)
				throws SAXException {
			if (inRepository && inContent && inFact && inDesiredContent) {
				countryFact = new String();
				for (int i = start; i < start + length; i++) {
					countryFact += ch[i];
				}
			}
		}
		
	}
	
}
