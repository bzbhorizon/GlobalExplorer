package bzb.se.installations;

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.JPanel;

import bzb.se.exceptions.NoSuchVisitorException;
import bzb.se.gui.TextLabel;
import bzb.se.installations.thoughts.Thought;
import bzb.se.messaging.Message;
import bzb.se.meta.Device;
import bzb.se.meta.Installations;
import bzb.se.meta.Requirements;
import bzb.se.meta.ui.Fonts;
import bzb.se.meta.Packages;
import bzb.se.utility.ImageProcessing;
import bzb.se.visitors.Visitor;

public class PhotoWall extends MultiSharedGUI {
	
	public Vector thoughts;
	
	public PhotoWall() {
		super(Installations.THOUGHTS_ID,
				1,
				Requirements.NOT_REQUIRED,
				Requirements.NOT_REQUIRED,
				Requirements.NOT_REQUIRED,
				null,
				Installations.THOUGHTS_DESC);
		thoughts = new Vector();
	}
	
	public void updateSharedScreen (Visitor updater) {
		String tableHTML = "<table>";
		
		final int limit = 9;
		final int rowLimit = 3;
		int count = 0;
		int rowCount = 0;
		Enumeration e = thoughts.elements();
		while (count < limit) {
			if (rowCount == 0) {
				tableHTML += "<tr height=\"" + (int)((double)Device.INSTALLATION_RES[1] / 3.0) + "\">";
			}
			
			tableHTML += "<td valign=\"center\"><center>";
			
			if (e.hasMoreElements()) {
				Thought t = (Thought) e.nextElement();

				Visitor author = null;
				try {
					author = new Visitor(t.getAuthorHost(), db);
				} catch (NoSuchVisitorException e1) {
					e1.printStackTrace();
				}

				/*String visitorName = author.getVisitorName();
				if (visitorName == null || visitorName.equals("null")) {
					visitorName = "a visitor";
				}
				tableHTML += "<p style=\"font-weight: bold; padding: 5 0 5 0;\">Photo by " + visitorName + "</p>";*/
				try {
					File photoFile = new File(Packages.outputDir + "Photowall/" + count + "_" + rowCount + ".jpg");
					BufferedImage photo = t.getPhoto();
					double scale = ((double)Device.INSTALLATION_RES[0] / 4.0) / (double)photo.getWidth();
					BufferedImage scaledPhoto = ImageProcessing.resize(photo, (int)((double)Device.INSTALLATION_RES[0] / 4.0), (int) (photo.getHeight() * scale));
					ImageIO.write(scaledPhoto, "JPEG", photoFile);
					tableHTML += "<p style=\" padding: 5 0 5 0;\"><img src=\"" + photoFile.toURI() + "\"></p>";
				} catch (IOException e2) {
					e2.printStackTrace();
				}
				
			}
			
			count++;
			rowCount++;
			
			tableHTML += "</center></td>";
			
			if (rowCount == rowLimit) {
				tableHTML += "</tr>";
				rowCount = 0;
			}
		}
		
		tableHTML += "</table>";
		
		JPanel contentPane = getGUI().getFreshContentPane();
		TextLabel table = new TextLabel("<html><center><h1>Visitors' photos</h1>" +
				"<p style=\"font-weight: bold;\">Try taking a photo (via your 'Other activities' menu) in order to use this exhibit.</p></center>" + tableHTML + "</html>",
				Fonts.generalFont);
		contentPane.add(Box.createVerticalGlue());
		contentPane.add(table);
		table.setAlignmentX(Component.CENTER_ALIGNMENT);
		contentPane.add(Box.createVerticalGlue());
		getGUI().updateContentPane(contentPane);
	}
	
	public void addThought (Thought newThought) {
		Enumeration e = thoughts.elements();
		boolean isNew = true;
		while (e.hasMoreElements()) {
			Thought t = (Thought) e.nextElement();
			if (t.getAuthorHost().equals(newThought.getAuthorHost())
					&& t.getTimetaken() == newThought.getTimetaken()) {
				isNew = false;
				break;
			}
		}
		if (isNew) {
			if (thoughts.size() >= 9) {
				System.out.println("Removing thought " + ((Thought)thoughts.elementAt(0)).getTimetaken());
				thoughts.removeElementAt(0);
			}
			thoughts.add(newThought);
		}
	}

	public boolean handleMessage (Message receivedMessage) {
		boolean handled = super.handleMessage(receivedMessage);
		
		if (!handled) {
			handled = true;
			switch (receivedMessage.getMessageCode()) {
			// case 0: by multi
			// case 1: by multi
			default:
				handled  = false;
				break;
			}
		}
		return handled;
	}
}
