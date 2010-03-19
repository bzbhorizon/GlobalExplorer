package bzb.se.installations.instances;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import javax.imageio.ImageIO;

import bzb.se.exceptions.InstanceNotOwnedException;
import bzb.se.exceptions.NoSuchVisitorException;
import bzb.se.installations.thoughts.Thought;
import bzb.se.messaging.Message;
import bzb.se.meta.Packages;

public class PhotoWall extends Instance {

	public PhotoWall () {
		super();
	}
	
	public boolean setPortAndStart (int desiredPort) {
		boolean portSet = super.setPortAndStart(desiredPort);
		
		if (portSet) {
			// thoughts specific stuff
		}
		return portSet;
	}
	
	public final class SpecificMessageCodes {
		public final class In {
			public static final int PUSH_THOUGHTS = 500;
		}
		
		public final class Out {
		}
	}
	
	public bzb.se.installations.PhotoWall getThoughtsMulti () {
		return (bzb.se.installations.PhotoWall) getMulti();
	}
		
	public static class ThoughtTypes {
		public static final int PHOTO = 0;
		public static final int TEXT = 1;
	}
		
	public boolean handleMessage (Message receivedMessage) {
		boolean handled = super.handleMessage(receivedMessage);
		
		//if (!handled) {
			handled = true;
			switch (receivedMessage.getMessageCode()) {
			case SpecificMessageCodes.In.PUSH_THOUGHTS:
				completeExperience();
				try {
					Socket s = getInstanceServer().accept();
					s.setReceiveBufferSize(1000);
					InputStream is = s.getInputStream();
										
					while (true) {
						int timetaken = is.read();
						
						System.out.println(timetaken);
						
						if (timetaken == -1) {
							break;
						}
						
						BufferedImage photo = ImageIO.read(is);
						if (photo != null) {
							System.out.println(photo.getWidth() + " " + photo.getHeight());
							try {
								Thought p = new Thought(getOwner().getVisitorHost(), photo, timetaken);
								getThoughtsMulti().addThought(p);
							} catch (InstanceNotOwnedException e) {
								e.printStackTrace();
							} catch (NoSuchVisitorException e) {
								e.printStackTrace();
							}
						}
						
						System.out.println("Received thought");
					}
					
					if (is != null) {
						is.close();
					}
					if (s != null) {
						s.close();
					}
					
					try {
						getThoughtsMulti().updateSharedScreen(getOwner());
					} catch (InstanceNotOwnedException e) {
						e.printStackTrace();
					} catch (NoSuchVisitorException e) {
						e.printStackTrace();
					}
				} catch (IOException e) {
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
