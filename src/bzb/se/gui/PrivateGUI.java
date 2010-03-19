package bzb.se.gui;

import java.awt.Component;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import bzb.se.exceptions.InstanceNotOwnedException;
import bzb.se.exceptions.NoSuchVisitorException;
import bzb.se.installations.instances.Instance;
import bzb.se.meta.ui.Dimensions;
import bzb.se.meta.ui.Fonts;
import bzb.se.visitors.Visitor;

public class PrivateGUI extends GUI {

	private Instance instance;
	
	public PrivateGUI (Instance instance) {
		super(instance.getMulti());
		this.instance = instance;
	}
	
	// generic
	protected void showCouplingConfirmPanel() {
		JPanel contentPane = getFreshContentPane();

		String waitingMessage = "Welcome";
		JLabel photo = null;
		Visitor visitor;
		try {
			visitor = instance.getOwner();
			if (visitor.getVisitorName() != null && !visitor.getVisitorName().equals("null")) {
				waitingMessage += " " + visitor.getVisitorName();
			}
			if (visitor.getVisitorPhoto() != null) {
				photo = new JLabel(new ImageIcon(visitor.getVisitorPhoto()));
			}
		} catch (NoSuchVisitorException e) {
			e.printStackTrace();
		} catch (InstanceNotOwnedException e) {
			e.printStackTrace();
		}
		
		TextLabel title = new TextLabel(waitingMessage, Fonts.titleFont);

		contentPane.add(Box.createVerticalGlue());
		contentPane.add(title);
		title.setAlignmentX(Component.CENTER_ALIGNMENT);
		contentPane.add(Box.createRigidArea(Dimensions.verticalSpacer));
		if (photo != null) {
			contentPane.add(photo);
			photo.setAlignmentX(Component.CENTER_ALIGNMENT);
		}
		contentPane.add(Box.createVerticalGlue());

		updateContentPane(contentPane);
	}
	
	protected void showDecouplingConfirmPanel() {
		JPanel contentPane = getFreshContentPane();

		String waitingMessage = "Good-bye";
		JLabel photo = null;
		Visitor visitor;
		try {
			visitor = instance.getOwner();
			if (visitor.getVisitorName() != null && !visitor.getVisitorName().equals("null")) {
				waitingMessage += " " + visitor.getVisitorName();
			}
			if (visitor.getVisitorPhoto() != null) {
				photo = new JLabel(new ImageIcon(visitor.getVisitorPhoto()));
			}
		} catch (NoSuchVisitorException e) {
			e.printStackTrace();
		} catch (InstanceNotOwnedException e) {
			e.printStackTrace();
		}
		
		TextLabel title = new TextLabel(waitingMessage, Fonts.titleFont);

		contentPane.add(Box.createVerticalGlue());
		contentPane.add(title);
		title.setAlignmentX(Component.CENTER_ALIGNMENT);
		contentPane.add(Box.createRigidArea(Dimensions.verticalSpacer));
		if (photo != null) {
			contentPane.add(photo);
			photo.setAlignmentX(Component.CENTER_ALIGNMENT);
		}
		contentPane.add(Box.createVerticalGlue());

		updateContentPane(contentPane);
	}
	
	// installation-specific
	protected void showCouplingInstructionPanel() {
		JPanel contentPane = getFreshContentPane();

		String waitingMessage = "Instructions (should be defined!)";
		
		TextLabel title = new TextLabel(waitingMessage, Fonts.titleFont);
		
		contentPane.add(Box.createVerticalGlue());
		contentPane.add(title);
		title.setAlignmentX(Component.CENTER_ALIGNMENT);
		contentPane.add(Box.createVerticalGlue());

		updateContentPane(contentPane);
	}

}
