package bzb.se.installations;

import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JPanel;

import bzb.se.exceptions.InstanceNotOwnedException;
import bzb.se.exceptions.NoSuchVisitorException;
import bzb.se.installations.instances.Instance;
import bzb.se.messaging.Message;
import bzb.se.meta.Device;
import bzb.se.meta.Installations;
import bzb.se.meta.Requirements;
import bzb.se.meta.ui.Fonts;
import bzb.se.utility.ImageProcessing;
import bzb.se.visitors.Visitor;

public class Friends extends MultiSharedGUI {
	
	public Friends() {
		super(Installations.FRIENDS_ID,
				5,
				Requirements.NOT_REQUIRED,
				Requirements.NOT_REQUIRED,
				Requirements.NOT_REQUIRED,
				new String[] {Installations.REGISTRATION_ID},
				Installations.FRIENDS_DESC);
	}
	
	public void updateSharedScreen (Visitor updater, boolean updaterVisible) {
		JPanel contentPane = getGUI().getFreshContentPane();
		FriendMap map = new FriendMap(updater, updaterVisible);
		contentPane.add(map);
		map.setAlignmentX(Component.CENTER_ALIGNMENT);
		getGUI().updateContentPane(contentPane);
	}
	
	public class FriendMap extends JComponent {
		
		private boolean updaterVisible;
		
		private Visitor updater;
		
		public FriendMap (Visitor updater, boolean updaterVisible) {
			this.updater = updater;
			this.updaterVisible = updaterVisible;
			this.setSize(Device.INSTALLATION_RES[0], Device.INSTALLATION_RES[1]);
		}
		
		public void drawVisitorCard (Graphics g, Visitor visitor, int[] graphicsCenterCoords, int[] cardCenterCoords) {
			System.out.println(cardCenterCoords[0] + " " + cardCenterCoords[1] + " " + graphicsCenterCoords[0] + " " + graphicsCenterCoords[1]);
			
			g.setColor(Color.WHITE);
			g.drawLine(graphicsCenterCoords[0], graphicsCenterCoords[1], graphicsCenterCoords[0] + cardCenterCoords[0], graphicsCenterCoords[1] + cardCenterCoords[1]);
			
			g.setFont(Fonts.smallTitleFont);
			FontMetrics fm = g.getFontMetrics();
			String textString = visitor.getVisitorName();
			if (textString == null || textString.equals("null")) {
				textString = "Unnamed";
			}
			
			final int padding = 10;
			int cardHeight = fm.getHeight() + 20;
			double cardWidth = 0.0;
			
			BufferedImage visitorPhoto = visitor.getVisitorPhoto();
			if (visitorPhoto != null) {
				double desiredMax;
				double bHeight;
				double bWidth;
				double proportion = 5.0;
				if (visitorPhoto.getWidth() > visitorPhoto.getHeight()) {
					desiredMax = (double) Device.INSTALLATION_RES[0] / proportion;
					bHeight = visitorPhoto.getHeight() * desiredMax / (double) visitorPhoto.getWidth();
					bWidth = desiredMax;
				} else {
					desiredMax = (double) Device.INSTALLATION_RES[1] / proportion;
					bWidth = visitorPhoto.getWidth() * desiredMax / (double) visitorPhoto.getHeight();
					bHeight = desiredMax;
				}
				visitorPhoto = ImageProcessing.resize(visitorPhoto, (int)bWidth, (int)bHeight);
				cardHeight += visitorPhoto.getHeight() + padding;
				cardWidth = visitorPhoto.getWidth();
			}
			
			if (fm.stringWidth(textString) > cardWidth) {
				cardWidth = fm.stringWidth(textString);
			}
						
			g.fillRoundRect(graphicsCenterCoords[0] + cardCenterCoords[0] - (int)(cardWidth / 2.0) - padding, graphicsCenterCoords[1] + cardCenterCoords[1] - (int)((double)cardHeight / 2.0) - padding, (int)cardWidth + padding * 2, cardHeight, (int)(cardWidth / 10.0), (int)(cardWidth / 10.0));
			g.setColor(Color.BLACK);
			System.out.println((fm.stringWidth(textString) / 2) + " " + (cardHeight / 2 + padding / 2));
			g.drawString(textString, graphicsCenterCoords[0] + cardCenterCoords[0] - fm.stringWidth(textString) / 2, graphicsCenterCoords[1] + cardCenterCoords[1] - cardHeight / 2 + 2 * padding);
			if (visitorPhoto != null) {
				g.drawImage(visitorPhoto, graphicsCenterCoords[0] + cardCenterCoords[0] - visitorPhoto.getWidth() / 2, graphicsCenterCoords[1] + cardCenterCoords[1] - visitorPhoto.getHeight() / 2 + fm.getHeight() / 2 - padding, null);
			}
		}
		
		public void paint (Graphics g) {
			Vector members = getCurrentGroup();
			if (updater != null) {
				Enumeration e = members.elements();
				while (e.hasMoreElements()) {
					boolean display = false;
					Visitor member = (Visitor) e.nextElement();
					if (member.getVisitorHost().equals(updater.getVisitorHost())) {
						if (updaterVisible) {
							System.out.println(member.getVisitorHost());
							display = true;
						}
					} else {
						System.out.println(member.getVisitorHost());
						display = true;
					}
					if (!display) {
						members.remove(member);
					}
				}
			}
			
			if (members.size() > 0) {
				double[] center = new double[]{(double)Device.INSTALLATION_RES[0] / 2.0,
						(double)Device.INSTALLATION_RES[1] / 2.0};
				double circleDiam = 100.0;
				Enumeration e;
				if (members.size() > 1) {
					double degrees = 360.0 / members.size();
					
					System.out.println(degrees + " " + center[0] + " " + center[1]);
					double thisDegrees = 0;
					e = members.elements();
					while (e.hasMoreElements()) {
						Visitor member = (Visitor) e.nextElement();
						thisDegrees += degrees;
						double[] coords = new double[2];
						coords[0] = Math.sin(Math.toRadians(thisDegrees)) * center[1] / 2.0;
						coords[1] = Math.sqrt(Math.pow((center[1] / 2), 2) - Math.pow((coords[0]), 2));
						if (thisDegrees >= 0 && thisDegrees < 90 || thisDegrees > 270 && thisDegrees <= 360) {
							coords[1] *= -1;
						}
						coords[0] *= (double)Device.INSTALLATION_RES[0] / (double)Device.INSTALLATION_RES[1];
						System.out.println(Math.sin(Math.toRadians(thisDegrees)) + " " + member.getVisitorName() + " " + thisDegrees + " " + coords[0] + " " + coords[1]);
						
						drawVisitorCard(g, member, new int[]{(int) center[0], (int) center[1]}, new int[]{(int) coords[0], (int) coords[1]});
					}
					
					g.setFont(Fonts.smallTitleFont);
					FontMetrics fm = g.getFontMetrics();
					String textString = "GROUP";
					int centerCircleDiam = (int) ((double)fm.stringWidth(textString) * 1.2);
					g.setColor(Color.GREEN);
					g.fillOval((int)center[0] - (int)(centerCircleDiam / 2.0), (int)center[1] - (int)(centerCircleDiam / 2.0), (int)centerCircleDiam, (int)centerCircleDiam);
					g.setColor(Color.BLACK);
					g.drawString(textString, (int)center[0] - fm.stringWidth(textString) / 2, (int)center[1] + fm.getDescent());
				} else {
					g.setColor(Color.RED);
					
					e = members.elements();
					while (e.hasMoreElements()) {
						//Visitor member = (Visitor) e.nextElement();
						g.fillRoundRect((int)center[0] - (int)(circleDiam / 2.0), (int)center[1] - (int)circleDiam, (int)circleDiam, (int)circleDiam * 2, (int)(circleDiam / 10.0), (int)(circleDiam / 10.0));
					}
					
					g.setFont(Fonts.titleFont);
					FontMetrics fm = g.getFontMetrics();
					String textString = "At least one more visitor needed";
					g.drawString(textString, (int)center[0] - fm.stringWidth(textString) / 2, fm.getHeight() * 2);
				}
			}
		}
	}
	
	public Vector getCurrentGroup () {
		Vector members = new Vector();
		Enumeration e = getInstances().elements();
		while (e.hasMoreElements()) {
			Instance instance = (Instance) e.nextElement();
			try {
				members.add(instance.getOwner());
			} catch (InstanceNotOwnedException e1) {
				System.out.println("Exception expected: no visitor coupled");
			} catch (NoSuchVisitorException e1) {
				e1.printStackTrace();
			}
		}
		return members;
	}
	
	public Vector createGroup (Visitor creator) {
		Vector temp = getCurrentGroup();
		Vector friends = new Vector();
		
		Enumeration e = temp.elements();
		while (e.hasMoreElements()) {
			Visitor friend = (Visitor) e.nextElement();
			
			if (!friend.getVisitorHost().equals(creator.getVisitorHost())) {
				ResultSet rs;
				try {
					rs = db.stmt
						.executeQuery("SELECT * FROM friends WHERE hostA=\"" 
								+ creator.getVisitorHost() 
								+ "\" AND hostB=\"" 
								+ friend.getVisitorHost() 
								+ "\" OR hostA=\"" 
								+ friend.getVisitorHost() 
								+ "\" AND hostB=\"" 
								+ creator.getVisitorHost() 
								+ "\"");
				
					if (!rs.next()) {
						friends.add(friend);
						db.stmt.executeUpdate("INSERT INTO friends VALUES (\"" + creator.getVisitorHost() + "\", \"" + friend.getVisitorHost() + "\")");
					}
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
		}
		return friends;
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
