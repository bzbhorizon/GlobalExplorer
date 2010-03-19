package bzb.se.installations.instances;

import java.awt.Color;
import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.EndOfMediaEvent;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.NoPlayerException;
import javax.media.Player;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import bzb.se.DB;
import bzb.se.exceptions.InstanceNotOwnedException;
import bzb.se.exceptions.MessageNotSentException;
import bzb.se.exceptions.NoSuchVisitorException;
import bzb.se.gui.CentredBackgroundBorder;
import bzb.se.gui.TextLabel;
import bzb.se.messaging.Message;
import bzb.se.meta.Device;
import bzb.se.meta.Network;
import bzb.se.meta.Packages;
import bzb.se.meta.ui.Dimensions;
import bzb.se.meta.ui.Fonts;
import bzb.se.meta.ui.Images;
import bzb.se.utility.ImageProcessing;
import bzb.se.utility.XMLProcessor;
import bzb.se.visitors.Visitor;

public class Slideshow extends GUIInstance {

	public Slideshow () {
		super();
	}
	
	public boolean setPortAndStart (int desiredPort) {
		boolean portSet = super.setPortAndStart(desiredPort);
		
		if (portSet) {
			// slideshow specific stuff
			musicPlayer = new MusicPlayer();
		}
		return portSet;
	}
	
	public void reset () {
		super.reset();
		
		currentSeeds = new Vector();
		currentCode = -1;
		
		if (musicPlayer != null) {
			musicPlayer.stop();
		}
	}
	
	public Vector currentSeeds;
	
	public int currentCode;
	
	public MusicPlayer musicPlayer;
	
	// for flag screen
	public Vector rows;
	
	public int currentColumn = 0;
	
	public int currentRow = 0;
	
	public final class SpecificMessageCodes {
		public final class In {
			public final static int PUSH_SEEDS = 500;
			public final static int SEEDS_KEY_PRESSED = 501;
			public final static int SLIDESHOW_KEY_PRESSED = 502;
			public static final int NEW_SLIDESHOW = 503;
			public static final int READY_FOR_SLIDE = 504;
		}
		
		public final class Out {
			public final static int TO_FLAGS = 500;
			public final static int TO_SLIDESHOW = 501;
			public final static int PREPARE_FOR_SLIDE = 502;
		}
	}
	
	public final class FlagKeysPressed {
		public static final int PRESSED_UP = 0;
		public static final int PRESSED_LEFT = 1;
		public static final int PRESSED_RIGHT = 2;
		public static final int PRESSED_DOWN = 3;
		public static final int PRESSED_SELECT = 4;
	}
	
	public final class SlideshowKeysPressed {
		public static final int PRESSED_VIEW_LESS = 0;
		public static final int PRESSED_PREVIOUS = 1;
		public static final int PRESSED_NEXT = 2;
		public static final int PRESSED_VIEW_MORE = 3;
		public static final int PRESSED_SAVE = 4;
	}
	
	public final class SavingState {
		public static final int NONE = 0;
		public static final int SAVING = 1;
		public static final int SAVED = 2;
	}
	
	protected void showCouplingInstructionPanel() {
		// keep welcome until they couple (coupling instructions on device)
	}
	
	private static final String SLIDESHOW_XML_FILENAME = "slideshow.xml";
	
	private static final String THUMB_FILENAME = "/thumb.png";
	
	private static final String BACKGROUND_FILENAME = "/bg.png";
	
	private static final double FLAG_SCALE_FACTOR = 5.0;
	
	protected void showFlagChoicePanel () {
		new Thread(new Runnable() {
			public void run () {
				try {
					Message prod = new Message(SpecificMessageCodes.Out.TO_FLAGS, getOwner().getVisitorHost(), Network.mobilePacketPort);
					prod.send(multi.getMessenger());
				} catch (MessageNotSentException e) {
					e.printStackTrace();
				} catch (InstanceNotOwnedException e) {
					e.printStackTrace();
				} catch (NoSuchVisitorException e) {
					e.printStackTrace();
				}
			}
		}).start();
		
		JPanel contentPane = getFreshContentPane();

		TextLabel title = new TextLabel("<html><center>Starting the slideshow</center></html>",
				Fonts.titleFont);

		TextLabel instructions = null;
		if (currentSeeds != null && currentSeeds.size() > 1) {
			instructions = new TextLabel(
					"<html><center><p style=\"font-weight: bold;\">The keys that you found at the World Map have unlocked slideshows for you at this exhibit! The flags below correspond to your keys.</p>" +
					"<p style=\"font-weight: bold; color: yellow;\">Use your mobile's directional pad</span> to highlight and select a flag and start a slideshow about the country it represents.</p>",
					Fonts.generalFont);
		} else if (currentSeeds != null && currentSeeds.size() == 1) {	
			instructions = new TextLabel(
					"<html><center><p style=\"font-weight: bold;\">The key that you found at the World Map has unlocked a slideshow for you at this exhibit! The flag below corresponds to your key.</p>" +
					"<p style=\"font-weight: bold; color: yellow;\">Press the centre of your mobile's directional pad</span> to start the slideshow about your key location.</p>",
					Fonts.generalFont);
		}
		
		contentPane.add(Box.createVerticalGlue());
		contentPane.add(title);
		title.setAlignmentX(Component.CENTER_ALIGNMENT);
		contentPane.add(Box.createRigidArea(Dimensions.verticalSpacer));
		contentPane.add(instructions);
		instructions.setAlignmentX(Component.CENTER_ALIGNMENT);
		contentPane.add(Box.createRigidArea(Dimensions.largeVerticalSpacer));
		
		JPanel flagPane = new JPanel();
		flagPane.setOpaque(false);
		flagPane.setLayout(new BoxLayout(flagPane, BoxLayout.LINE_AXIS));
		flagPane.add(Box.createHorizontalGlue());
		
		if (currentSeeds != null) {
			int widthLeft = Device.INSTALLATION_RES[0];
			int desiredFlagWidth = (int) (Device.INSTALLATION_RES[0] / FLAG_SCALE_FACTOR);
			
			rows = new Vector();
			Vector row = new Vector();
			
			Enumeration codes = currentSeeds.elements();
			boolean finishedFlagPane = false;
			while (codes.hasMoreElements()) {
				int code = ((Integer) codes.nextElement()).intValue();
				row.addElement(new Integer(code));
				if (currentCode == -1) {
					currentCode = code;
				}
				
				ImageIcon flagImage;
				JLabel flag = null;
				try {
					BufferedImage flagOriginal = ImageIO.read(new File(
							Packages.contentDir + code + THUMB_FILENAME));
					flagImage = new ImageIcon(
							ImageProcessing.resize(
									flagOriginal,
									desiredFlagWidth,
									flagOriginal.getHeight() * desiredFlagWidth / flagOriginal.getWidth()
								)
							);
					flag = new JLabel(flagImage);
					
					widthLeft -= flagImage.getIconWidth();
					System.out.println(widthLeft);
					
					if (widthLeft < desiredFlagWidth) {
						flagPane.add(Box.createHorizontalGlue());
						
						contentPane.add(flagPane);
						flagPane.setAlignmentX(Component.CENTER_ALIGNMENT);
						contentPane.add(Box.createRigidArea(Dimensions.verticalSpacer));
						
						rows.addElement(row);
						
						finishedFlagPane  = true;
						
						row = new Vector();
						
						flagPane = new JPanel();
						flagPane.setOpaque(false);
						flagPane.setLayout(new BoxLayout(flagPane, BoxLayout.LINE_AXIS));
						flagPane.add(Box.createHorizontalGlue());
						
						widthLeft = Device.INSTALLATION_RES[0];
					} else {
						flagPane.add(Box.createRigidArea(Dimensions.horizontalSpacer));
						finishedFlagPane = false;
					}
					
					if (code == currentCode) {
						// selected flag - highlight it
						flag.setBorder(BorderFactory.createMatteBorder(20, 20, 20, 20, Color.GREEN));
						flagPane.add(flag);
						
						currentColumn = row.size() - 1;
						currentRow = rows.size();
						System.out.println(currentRow + ", " + currentColumn);
					} else {
						flagPane.add(flag);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			if (!finishedFlagPane) {
				flagPane.add(Box.createHorizontalGlue());
				
				contentPane.add(flagPane);
				flagPane.setAlignmentX(Component.CENTER_ALIGNMENT);
				contentPane.add(Box.createRigidArea(Dimensions.verticalSpacer));
				
				rows.addElement(row);
			}
		}
		
		contentPane.add(Box.createRigidArea(Dimensions.verticalSpacer));
		instructions = new TextLabel("<html><center><p style=\"font-weight: bold;\">Alternatively, press the button marked 'Leave' on your phone to end your experience here.</p></center></html>",
				Fonts.generalFont);
		contentPane.add(instructions);
		instructions.setAlignmentX(Component.CENTER_ALIGNMENT);

		contentPane.add(Box.createVerticalGlue());

		updateContentPane(contentPane);
	}
	
	protected void showNoSeedsPanel () {
		JPanel contentPane = getFreshContentPane();

		TextLabel title = new TextLabel("<html><center>Starting the slideshow</center></html>",
				Fonts.titleFont);

		TextLabel instructions = new TextLabel(
				"<html><center>You need to have collected a <b>seed</b> from the World Map before you can start a slideshow. Press 'Leave' on your mobile and return to the World Map to scan more tags!</center></html>",
				Fonts.generalFont);

		contentPane.add(Box.createVerticalGlue());
		contentPane.add(title);
		title.setAlignmentX(Component.CENTER_ALIGNMENT);
		contentPane.add(Box.createRigidArea(Dimensions.verticalSpacer));
		contentPane.add(instructions);
		instructions.setAlignmentX(Component.CENTER_ALIGNMENT);
		contentPane.add(Box.createVerticalGlue());
		updateContentPane(contentPane);
	}
	
	public void grabSlideshowContent () {
		new RepositoryXMLProcessor(getMulti().db);
		new SlideshowXMLProcessor(getMulti().db);
		
		musicPlayer.loadTracks();
		musicPlayer.start();
	}
	
	// for slideshow
	public String countryName = null;
	
	public String countryFact = null;
	
	public Vector countrySlides = new Vector();
	
	public int currentSlide = 0;
	
	public int currentViewDepth = 0;
	
	public int saving = SavingState.NONE;
	
	public void resetSlideshowContent () {
		countryName = null;
		countryFact = null;
		countrySlides = new Vector();
		currentSlide = 0;
		currentViewDepth = 0;
		saving = SavingState.NONE;
		
		if (musicPlayer != null) {
			musicPlayer.stop();
		}
	}
	
	protected void showSlideshowTitlePanel () {
		new Thread(new Runnable () {
			public void run () {
				try {
					Message prod = new Message(SpecificMessageCodes.Out.TO_SLIDESHOW, getOwner().getVisitorHost(), Network.mobilePacketPort);
					prod.send(multi.getMessenger());
				} catch (MessageNotSentException e) {
					e.printStackTrace();
				} catch (InstanceNotOwnedException e) {
					e.printStackTrace(); 
				} catch (NoSuchVisitorException e) {
					e.printStackTrace();
				}
			}
		}).start();
		
		JPanel contentPane = getFreshContentPane();

		TextLabel title = new TextLabel("<html><center>" + countryName + "</center></html>",
				Fonts.titleFont);

		contentPane.add(Box.createVerticalGlue());
		contentPane.add(title);
		title.setAlignmentX(Component.CENTER_ALIGNMENT);
		contentPane.add(Box.createRigidArea(Dimensions.largeVerticalSpacer));
		
		ImageIcon flagImage;
		JLabel flag = null;		
		try {
			BufferedImage flagOriginal = ImageIO.read(new File(
					Packages.contentDir + currentCode + THUMB_FILENAME));
			
			int desiredFlagWidth = (int) (Device.INSTALLATION_RES[0] * 0.75);
			int desiredFlagHeight = (int) (Device.INSTALLATION_RES[1] * 0.75);
			System.out.println(desiredFlagWidth + " " + desiredFlagHeight);
			double xScale = (double) desiredFlagWidth / (double) flagOriginal.getWidth();
			double yScale = (double) desiredFlagHeight / (double) flagOriginal.getHeight();
			System.out.println(xScale + " " + yScale);
			if (xScale < yScale) {
				desiredFlagHeight = (int) (flagOriginal.getHeight() * xScale);
			} else {
				desiredFlagWidth = (int) (flagOriginal.getWidth() * yScale);
			}
			System.out.println(desiredFlagWidth + " " + desiredFlagHeight);
			
			flagImage = new ImageIcon(
					ImageProcessing.resize(
							flagOriginal,
							desiredFlagWidth,
							desiredFlagHeight
						)
					);
			flag = new JLabel(flagImage);
			contentPane.add(flag);
			flag.setAlignmentX(Component.CENTER_ALIGNMENT);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		contentPane.add(Box.createRigidArea(Dimensions.largeVerticalSpacer));
		
		TextLabel instruction = new TextLabel("<html><center><p style=\"color: yellow; font-weight: bold;\">Tilt your phone's directional pad to the RIGHT to view the first slide</p><p style=\"font-weight: bold;\">Tilt LEFT and RIGHT within the slideshow to move backwards and forwards through the slides respectively.</p></center></html>",
				Fonts.generalFont);

		contentPane.add(instruction);
		instruction.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		contentPane.add(Box.createVerticalGlue());

		updateContentPane(contentPane);
		
		completeExperience();
		
		musicPlayer.start();
	}
	
	public static final String CONTENT_IMAGE_URL = "contentImage.png";
	
	public void showSlideshowContentPanel () {
		JPanel contentPane = getFreshContentPane();
		

		BufferedImage slideshowBackgroundOriginal;
		try {
			slideshowBackgroundOriginal = ImageIO.read(new File(
					Packages.contentDir + currentCode + BACKGROUND_FILENAME));
		
			int desiredBackgroundWidth = Device.INSTALLATION_RES[0];
			int desiredBackgroundHeight = Device.INSTALLATION_RES[1];
			double xScale = (double) desiredBackgroundWidth / (double) slideshowBackgroundOriginal.getWidth();
			double yScale = (double) desiredBackgroundHeight / (double) slideshowBackgroundOriginal.getHeight();
			System.out.println(xScale + " " + yScale);
			if (xScale < yScale) {
				desiredBackgroundHeight = (int) (slideshowBackgroundOriginal.getHeight() * xScale);
			} else {
				desiredBackgroundWidth = (int) (slideshowBackgroundOriginal.getWidth() * yScale);
			}
			System.out.println(desiredBackgroundWidth + " " + desiredBackgroundHeight);
			
			BufferedImage slideshowBackground = ImageProcessing.resize(
					slideshowBackgroundOriginal,
					desiredBackgroundWidth,
					desiredBackgroundHeight
						);
			
			CentredBackgroundBorder bg = new CentredBackgroundBorder(
					slideshowBackground);
			contentPane.setBorder(bg);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		contentPane.add(Box.createRigidArea(Dimensions.largeVerticalSpacer));

		JPanel titlePane = new JPanel();
		titlePane.setOpaque(false);
		titlePane.setLayout(new BoxLayout(titlePane, BoxLayout.LINE_AXIS));
		titlePane.add(Box.createRigidArea(Dimensions.largeHorizontalSpacer));
		
		TextLabel title = new TextLabel("<html><center>" + countryName + "</center></html>",
				Fonts.titleFont);

		titlePane.add(title);
		title.setAlignmentY(Component.CENTER_ALIGNMENT);
		titlePane.add(Box.createRigidArea(Dimensions.horizontalSpacer));
		
		ImageIcon flagImage;
		JLabel flag = null;		
		try {
			BufferedImage flagOriginal = ImageIO.read(new File(
					Packages.contentDir + currentCode + THUMB_FILENAME));
			
			int desiredFlagWidth = (int) (Device.INSTALLATION_RES[0] * 0.1);
			int desiredFlagHeight = (int) (Device.INSTALLATION_RES[1] * 0.1);
			System.out.println(desiredFlagWidth + " " + desiredFlagHeight);
			double xScale = (double) desiredFlagWidth / (double) flagOriginal.getWidth();
			double yScale = (double) desiredFlagHeight / (double) flagOriginal.getHeight();
			System.out.println(xScale + " " + yScale);
			if (xScale < yScale) {
				desiredFlagHeight = (int) (flagOriginal.getHeight() * xScale);
			} else {
				desiredFlagWidth = (int) (flagOriginal.getWidth() * yScale);
			}
			System.out.println(desiredFlagWidth + " " + desiredFlagHeight);
			
			flagImage = new ImageIcon(
					ImageProcessing.resize(
							flagOriginal,
							desiredFlagWidth,
							desiredFlagHeight
						)
					);
			flag = new JLabel(flagImage);
			titlePane.add(flag);
			flag.setAlignmentY(Component.CENTER_ALIGNMENT);
		} catch (IOException e) {
			e.printStackTrace();
		}
		titlePane.add(Box.createHorizontalGlue());
		
		contentPane.add(titlePane);
		titlePane.setAlignmentX(Component.CENTER_ALIGNMENT);
		contentPane.add(Box.createRigidArea(Dimensions.largeVerticalSpacer));
		
		Slide slide = (Slide) countrySlides.get(currentSlide - 1);
		
		if (currentViewDepth > 0) {
			String contentHTML = new String("<html><center><table>");
			
			BufferedImage imageOriginal = slide.getImage();
			
			int desiredImageWidth = (int) (Device.INSTALLATION_RES[0] * 0.25);
			int desiredImageHeight = (int) (Device.INSTALLATION_RES[1] * 0.25);
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
			
			String imageURL = Packages.outputDir + "Slideshow/" + CONTENT_IMAGE_URL;
			
			try {
				File image = new File(imageURL);
				ImageIO.write(ImageProcessing.resize(
						imageOriginal,
						desiredImageWidth,
						desiredImageHeight
					), "PNG", image);
				
				contentHTML += "<tr><td rowspan=\"" + currentViewDepth + "\" valign=\"top\"><img src=\"" + image.toURI() + "\"></td>";
			} catch (IOException e) {
				e.printStackTrace();
				
				contentHTML += "<tr><td rowspan=\"" + currentViewDepth + "\"></td>";
			}
			
			int added = 0;
			while (added < currentViewDepth) {
				if (added > 0) {
					contentHTML += "<tr>";
				}
				
				contentHTML += "<td valign=\"top\"><p style=\"font-weight: bold;\">" + ((Slide) countrySlides.get(currentSlide - 1)).getParagraphs().get(added) + "</p></td></tr>";
				
				added++;
			}
			
			
			contentHTML += "<tr><td colspan=\"2\"><center><p style=\"color: yellow; font-weight: bold;\">";
			if (currentViewDepth < ((Slide) countrySlides.get(currentSlide - 1)).getParagraphs().size()) {
				contentHTML += "Tilt DOWN to view more text";
			}
			if (currentViewDepth < ((Slide) countrySlides.get(currentSlide - 1)).getParagraphs().size() - 1 && currentViewDepth > 0) {
				contentHTML += ";";
			}
			if (currentViewDepth > 0) {
				contentHTML += " UP to view less text.";
			}
			contentHTML += "</p></td></tr>";
			
			switch (saving) {
			case SavingState.SAVING:				
				contentHTML += "<tr><td colspan=\"2\"><center><p><span style=\"color: green; font-weight: bold;\">Saving slide to your mobile's wallet ...</span></center></p></td></tr>";
				break;
			case SavingState.SAVED:
				contentHTML += "<tr><td colspan=\"2\"><center><p><span style=\"color: green; font-weight: bold;\">Saved slide</span></p></center></td></tr>";
				break;
			default:
				contentHTML += "<tr><td colspan=\"2\"><center><p><span style=\"color: green; font-weight: bold;\">Press the centre of your phone's directional pad to save this slide to your mobile.</span></p></center></td></tr>";
				break;
			}
			if (currentSeeds.size() > 1) {
				contentHTML += "<tr><td colspan=\"2\"><center><p><span style=\"color: red; font-weight: bold;\">Press your phone's right shoulder button to stop this slideshow.</span></center></p></td></tr>";
			} else {
				contentHTML += "<tr><td colspan=\"2\"><center><p><span style=\"color: red; font-weight: bold;\">Press your phone's right shoulder button to stop this slideshow.</span></center></p></td></tr>";
			}
			
			contentHTML += "</table></center></html>";
			TextLabel contentLabel = new TextLabel(contentHTML, Fonts.generalFont);
			contentPane.add(contentLabel);
			contentLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
			System.out.println(contentHTML);
		} else {		
			BufferedImage imageOriginal = slide.getImage();
			
			int desiredImageWidth = (int) (Device.INSTALLATION_RES[0] * 0.6);
			int desiredImageHeight = (int) (Device.INSTALLATION_RES[1] * 0.6);
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
			
			String imageURL = Packages.outputDir + "Slideshow/" + CONTENT_IMAGE_URL;
			File imageFile = new File(imageURL);
			
			String contentHTML = "<html><center>";
			try {
				ImageIO.write(ImageProcessing.resize(
						imageOriginal,
						desiredImageWidth,
						desiredImageHeight
					), "PNG", imageFile);
				
				contentHTML  += "<img src=\"" + imageFile.toURI() + "\">";
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			contentPane.add(Box.createRigidArea(Dimensions.verticalSpacer));
			
			if (((Slide) countrySlides.get(currentSlide - 1)).getParagraphs().size() > 0) {
				contentHTML += "<p><span style=\"color: yellow; font-weight: bold;\">Tilt DOWN to view some text</span></p>";
			}
			
			contentHTML += "<p><span style=\"color: red; font-weight: bold;\">Press your phone's right shoulder button to stop this slideshow.</span></p>";
			
			contentHTML += "</center></html>";
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			TextLabel contentLabel = new TextLabel(contentHTML, Fonts.generalFont);
			contentPane.add(contentLabel);
			contentLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		}
		
		contentPane.add(Box.createVerticalGlue());
		
		JPanel arrowPane = new JPanel();
		arrowPane.setOpaque(false);
		arrowPane.setLayout(new BoxLayout(arrowPane, BoxLayout.LINE_AXIS));
		arrowPane.add(Box.createRigidArea(Dimensions.horizontalSpacer));
		if (currentSlide > 0) {
			try {
				arrowPane.add(new JLabel(new ImageIcon(ImageIO.read(new File(
						Images.leftArrow)))));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		arrowPane.add(Box.createHorizontalGlue());
		if (currentSlide < countrySlides.size()) {
			try {
				arrowPane.add(new JLabel(new ImageIcon(ImageIO.read(new File(
						Images.rightArrow)))));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		arrowPane.add(Box.createRigidArea(Dimensions.horizontalSpacer));
		contentPane.add(arrowPane);
		arrowPane.setAlignmentX(Component.CENTER_ALIGNMENT);
		contentPane.add(Box.createRigidArea(Dimensions.largeVerticalSpacer));

		updateContentPane(contentPane);
	}
	
	public void showCurrentSlidePanel () {
		if (currentSlide > 0) {
			showSlideshowContentPanel();
			
			System.out.println("Current slide is " + currentSlide + " of " + countrySlides.size() + " at depth " + currentViewDepth + " of " + ((Slide) countrySlides.get(currentSlide - 1)).getParagraphs().size());
		} else {
			showSlideshowTitlePanel();
		}
	}
	
	public void saveCurrent () {
		// send
		try {
			Visitor owner = getOwner();
			Socket sock = new Socket(owner.getVisitorHost(), Network.mobileInstancePort);

			DataOutputStream dos = new DataOutputStream(sock
					.getOutputStream());
			
			System.out.println("Started sending slide to " + owner.getVisitorHost());
			
			Slide slide = (Slide) countrySlides.get(currentSlide - 1);
			BufferedImage imageOriginal = slide.getImage();
			
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
			dos.writeUTF(slide.getSubject());
			dos.writeInt(currentCode);
			
			System.out.println("Finished sending slide");
			
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
		
		saving = SavingState.SAVED;
		showCurrentSlidePanel();
		
		saving = SavingState.NONE;
	}
	
	public boolean handleMessage (Message receivedMessage) {
		boolean handled = super.handleMessage(receivedMessage);
		
		if (!handled) {
			handled = true;
			int keyCode;
			switch (receivedMessage.getMessageCode()) {
			case SpecificMessageCodes.In.PUSH_SEEDS:
				Vector seeds = receivedMessage.getMessageParts();
				Enumeration e = seeds.elements();
				while (e.hasMoreElements()) {
					int code = Integer.parseInt((String) e.nextElement());
					currentSeeds.addElement(new Integer(code));
					System.out.println("Visitor on " + receivedMessage.getHost() + " has seed " + code);
				}
				System.out.println("Received " + currentSeeds.size() + " seeds");
				
				if (currentSeeds.size() > 0) {
					showFlagChoicePanel();
				} else {
					showNoSeedsPanel();
				}
				break;
			case SpecificMessageCodes.In.SEEDS_KEY_PRESSED:
				if (currentSeeds.size() > 0) {
					System.out.println("Currently selected code " + currentCode + " row " + currentRow + " column " + currentColumn);
					
					// update current code
					keyCode = Integer.parseInt((String) receivedMessage.getMessageParts().elementAt(0));
					switch (keyCode) {
					case FlagKeysPressed.PRESSED_UP:
						System.out.println("Up");
						if (currentRow > 0) {
							currentRow--;
						}
						break;
					case FlagKeysPressed.PRESSED_LEFT:
						System.out.println("Left");
						if (currentColumn > 0) {
							currentColumn--;
						}
						break;
					case FlagKeysPressed.PRESSED_RIGHT:
						System.out.println("Right");
						if (currentColumn < ((Vector) rows.get(currentRow)).size() - 1) {
							currentColumn++;
						}
						break;
					case FlagKeysPressed.PRESSED_DOWN:
						System.out.println("Down");
						if (currentRow < rows.size() - 1) {
							currentRow++;
						}
						break;
					case FlagKeysPressed.PRESSED_SELECT:
						System.out.println("Selected " + currentCode);
						grabSlideshowContent();
						showCurrentSlidePanel();
						break;
					}
					
					currentCode = ((Integer)((Vector) rows.get(currentRow)).get(currentColumn)).intValue();
					
					System.out.println("New currently selected code " + currentCode + " row " + currentRow + " column " + currentColumn);
					
					if (keyCode != FlagKeysPressed.PRESSED_SELECT) {
						showFlagChoicePanel();
					}
				}
				break;
			case SpecificMessageCodes.In.SLIDESHOW_KEY_PRESSED:
				keyCode = Integer.parseInt((String) receivedMessage.getMessageParts().elementAt(0));
				
				switch (keyCode) {
				case SlideshowKeysPressed.PRESSED_VIEW_LESS:
					System.out.println("View less");
					
					if (currentViewDepth > 0) {
						currentViewDepth--;
					}
					break;
				case SlideshowKeysPressed.PRESSED_PREVIOUS:
					System.out.println("Previous");
					
					if (currentSlide > 0) {
						currentSlide--;
						currentViewDepth = 0;
					}
					break;
				case SlideshowKeysPressed.PRESSED_NEXT:
					System.out.println("Next");
					
					if (currentSlide < countrySlides.size()) {
						currentSlide++;
						currentViewDepth = 0;
					}
					break;
				case SlideshowKeysPressed.PRESSED_VIEW_MORE:
					System.out.println("View more");
					
					currentViewDepth++;
					
					if (currentSlide > 0) {
						int maxViewDepth = ((Slide) countrySlides.get(currentSlide - 1)).getParagraphs().size();
						if (currentViewDepth > maxViewDepth) {
							currentViewDepth = maxViewDepth;
						}
					}
					break;
				case SlideshowKeysPressed.PRESSED_SAVE:
					System.out.println("Save");
					
					saving = SavingState.SAVING;
					showCurrentSlidePanel();
					
					new Thread(new Runnable () {
						public void run () {
							try {
								Message prod = new Message(SpecificMessageCodes.Out.PREPARE_FOR_SLIDE, getOwner().getVisitorHost(), Network.mobilePacketPort);
								prod.send(multi.getMessenger());
							} catch (MessageNotSentException e) {
								e.printStackTrace();
							} catch (InstanceNotOwnedException e) {
								e.printStackTrace(); 
							} catch (NoSuchVisitorException e) {
								e.printStackTrace();
							}
						}
					}).start();
					break;
				}
				
				showCurrentSlidePanel();
				break;
			case SpecificMessageCodes.In.NEW_SLIDESHOW:
				resetSlideshowContent();
				showFlagChoicePanel();
				break;
			case SpecificMessageCodes.In.READY_FOR_SLIDE:
				saveCurrent();
				break;
			default:
				System.out.println("Unrecognised message code (" + receivedMessage.getMessageCode() + "); ignoring");
				handled = false;
				break;
			}
		}
		return handled;
	}
	
	public class RepositoryXMLProcessor extends XMLProcessor {

		public RepositoryXMLProcessor(DB db) {
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
				
				if (atts.getValue("code") != null && Integer.parseInt(atts.getValue("code")) == currentCode) {
					inDesiredContent = true;
					
					if (atts.getValue("name") != null) {
						countryName = atts.getValue("name");
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
	
	public class SlideshowXMLProcessor extends XMLProcessor {

		public SlideshowXMLProcessor(DB db) {
			parseXML();
			
			System.out.println(countrySlides.size());
		}
		
		public void parseXML () {
			super.parseXML(Packages.contentDir + currentCode + "/Slideshow/" + SLIDESHOW_XML_FILENAME);
		}
		
		private boolean inSlideshow = false;

		private boolean inSuitableSection = false;
		
		private boolean inText = false;
		
		private Vector paragraphs = new Vector();
		
		private String subject = null;
		
		private String imageURL = null;

		private String temp = new String();
		
		public void startElement(String namespaceURI, String localName,
				String qualifiedName, Attributes atts) throws SAXException {
			if (localName.equals("slideshow")) {
				inSlideshow = true;
			} else if (localName.equals("section")) {
				if (atts.getValue("ageGroup") != null) {
					try {
						if (getOwner().getVisitorAge() > 15 && atts.getValue("ageGroup").equals("adult")) {
							inSuitableSection = true;
						} else if (getOwner().getVisitorAge() <= 15 && atts.getValue("ageGroup").equals("child")) {
							inSuitableSection = true;
						} else {
							inSuitableSection = false;
						}
					} catch (InstanceNotOwnedException e) {
						e.printStackTrace();
					} catch (NoSuchVisitorException e) {
						e.printStackTrace();
					}
				} else {
					inSuitableSection = true;
				}
				
				if (inSuitableSection) {
					if (atts.getValue("subject") != null) {
						subject = atts.getValue("subject");
					}
					
					if (atts.getValue("image") != null) {
						imageURL = atts.getValue("image");
					}
				}
			} else if (localName.equals("text")) {
				inText = true;
				temp = new String();
			}
		}

		public void endElement(String namespaceURI, String localName,
				String qualifiedName) throws SAXException {
			if (localName.equals("slideshow")) {
				inSlideshow = false;
			} else if (localName.equals("section")) {
				if (inSuitableSection) {
					BufferedImage image = null;
					try {
						System.out.println(Packages.contentDir + currentCode + "/Slideshow/" + imageURL);
						image = ImageIO.read(new File(Packages.contentDir + currentCode + "/Slideshow/" + imageURL));
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					countrySlides.addElement(new Slide(subject, paragraphs, image));
				}
				
				inSuitableSection = false;
				subject = null;
				paragraphs = new Vector();
				imageURL = null;
			} else if (localName.equals("text")) {
				inText = false;
				paragraphs.addElement(temp );
			}
		}

		public void characters(char[] ch, int start, int length)
				throws SAXException {
			if (inSlideshow && inSuitableSection && inText) {
				for (int i = start; i < start + length; i++) {
					temp += ch[i];
				}
			}
		}
		
	}
	
	public class Slide {
		
		public Vector paragraphs;
		
		public String subject;
		
		public BufferedImage image;
		
		public Slide (String subject, Vector paragraphs, BufferedImage image) {
			this.paragraphs = paragraphs;
			this.subject = subject;
			this.image = image;
		}
		
		public String getSubject () {
			return subject;
		}
		
		public Vector getParagraphs () {
			return paragraphs;
		}
		
		public BufferedImage getImage () {
			return image;
		}
		
	}
	
	public static class PlayerState {
		public static int STOPPED = 0;
		public static int PLAYING = 1;
	}
	
	public class MusicPlayer implements Runnable {
		
		public int currentState;
		
		public File[] tracks;
		
		public Player player;
		
		public MusicPlayer () {
			currentState = PlayerState.STOPPED;
		}
		
		public void run () {
			currentState = PlayerState.PLAYING;
			
			while (currentState == PlayerState.PLAYING) {
				if (player == null || player.getState() != Player.Started) {
					int rand = (int) Math.round(Math.random() * (double) (tracks.length - 1));
					System.out.println("Selected track " + rand + " of " + tracks.length);
					try {
						MediaLocator ml = new MediaLocator(tracks[rand].toURL());
						player = Manager.createPlayer(ml);
						player.addControllerListener(new PlayerController());
						player.realize();
						player.start();
					} catch (MalformedURLException e) {
						e.printStackTrace();
					} catch (NoPlayerException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			stop();
		}
		
		private class PlayerController implements ControllerListener {

			public void controllerUpdate(ControllerEvent event) {
				if (event instanceof EndOfMediaEvent) {
					player.stop();								
					player.close();
				}
			}
			
		}
		
		public void loadTracks () {
			FileFilter mp3Filter = new FileFilter() {
				public boolean accept(File file) {
					if (!file.isDirectory() && (file.getName().endsWith("mp3") || file.getName().endsWith("MP3"))) {
						return true;
					} else {
						return false;
					}
				}
			};
			
			String musicDir = Packages.contentDir + currentCode + "/Slideshow/";
			
			try {
				if (getOwner().getVisitorAge() > 15) {
					musicDir += Packages.adultDir;
				} else {
					musicDir += Packages.childDir;
				}
			
				File backgroundMusicDir = new File(musicDir);
				tracks = backgroundMusicDir.listFiles(mp3Filter);
				
				for (int i = 0; i < tracks.length; i++) {
					System.out.println("Adding track " + tracks[i].getAbsolutePath());
				}
			} catch (InstanceNotOwnedException e) {
				e.printStackTrace();
			} catch (NoSuchVisitorException e) {
				e.printStackTrace();
			}
		}
		
		public void start () {
			if (tracks != null && currentState != PlayerState.PLAYING) {
				new Thread(this).start();
			}
		}
		
		public void stop () {
			currentState = PlayerState.STOPPED;
			
			if (player != null) {
				player.stop();								
				player.close();
			}
		}
		
	}
	
}
