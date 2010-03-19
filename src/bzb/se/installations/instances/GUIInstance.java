package bzb.se.installations.instances;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import bzb.se.exceptions.InstanceNotOwnedException;
import bzb.se.exceptions.NoSuchVisitorException;
import bzb.se.gui.TextLabel;
import bzb.se.messaging.Message;
import bzb.se.meta.Device;
import bzb.se.meta.MessageCodes;
import bzb.se.meta.ui.Dimensions;
import bzb.se.meta.ui.Fonts;
import bzb.se.meta.Packages;
import bzb.se.visitors.Visitor;

public class GUIInstance extends Instance {

	//	 reusable stuff
	protected DemoFrame demoFrame;

	protected ImageIcon logo;
	
	protected ImageIcon skey1Icon;

	protected ImageIcon skey2Icon;

	protected ImageIcon joypadIcon;

	protected ImageIcon leftArrowIcon;

	protected ImageIcon rightArrowIcon;
	
	public GUIInstance () {
		super();
	}
	
	public boolean setPortAndStart (int desiredPort) {
		boolean portSet = super.setPortAndStart(desiredPort);
		if (portSet) {
			setupGUI();
		}
		return portSet;
	}
	
	private void setupGUI () {
		JFrame.setDefaultLookAndFeelDecorated(true);

		demoFrame = new DemoFrame();

		demoFrame.setLocationRelativeTo(null);
		
		// make reusable images
		try {
			logo = new ImageIcon(ImageIO.read(new File(Packages.resourceDir + "logo.png")));
			skey1Icon = new ImageIcon(ImageIO.read(new File(Packages.resourceDir + "skey1.png")));
			skey2Icon = new ImageIcon(ImageIO.read(new File(Packages.resourceDir + "skey2.png")));
			joypadIcon = new ImageIcon(ImageIO.read(new File(Packages.resourceDir + "joypad.png")));
			rightArrowIcon = new ImageIcon(ImageIO.read(new File(Packages.resourceDir + 
					"right_arrow.png")));
			leftArrowIcon = new ImageIcon(ImageIO.read(new File(Packages.resourceDir + 
					"left_arrow.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		showSplashPanel();

		demoFrame.pack();
		demoFrame.setVisible(true);
	}
	
	public void close (boolean freeInstance) {
		super.close(freeInstance);
		
		if (demoFrame != null) {
			demoFrame.dispose();
		}
	} 
		
	protected class DemoFrame extends JFrame {
		
		public DemoFrame() {
			super();

			this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
			
			this.addWindowListener( new WindowAdapter() {
				public void windowClosing(WindowEvent evt) {
					getMulti().close();
            	}
			});

			this.addComponentListener(new java.awt.event.ComponentAdapter() {
				public void componentResized(ComponentEvent e) {
					JFrame tmp = (JFrame) e.getSource();
					tmp.setSize(Device.INSTALLATION_RES[0], Device.INSTALLATION_RES[1]);
				}
			});
		}

	}
	
	protected DemoFrame getDemoFrame () {
		return demoFrame;
	}
	
	protected JPanel getFreshContentPane() {
		JPanel contentPane = (JPanel) demoFrame.getContentPane();

		contentPane.removeAll();

		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
		contentPane.setOpaque(true);
		contentPane.setBackground(Color.BLACK);
		contentPane.setBorder(BorderFactory.createLineBorder(Color.WHITE));

		return contentPane;
	}

	protected void updateContentPane(JPanel contentPane) {
		contentPane.doLayout();
		demoFrame.setContentPane(contentPane);
	}
	
	// screens
	
	protected void outOfFocus () {
		JPanel contentPane = getFreshContentPane();

		TextLabel instructions = new TextLabel("Look at your mobile", Fonts.smallTitleFont);
		instructions.setForeground(Color.DARK_GRAY);

		contentPane.add(Box.createVerticalGlue());
		contentPane.add(instructions);
		instructions.setAlignmentX(Component.CENTER_ALIGNMENT);
		contentPane.add(Box.createVerticalGlue());

		updateContentPane(contentPane);
	}
	
	protected void inFocus () {
		int flashes = 0;
		while (flashes  < 3) {
			getDemoFrame().getContentPane().setBackground(Color.WHITE);
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			getDemoFrame().getContentPane().setBackground(Color.BLACK);
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			flashes++;
		}
	}
	
	protected void showBlankPanel () {
		JPanel contentPane = getFreshContentPane();

		updateContentPane(contentPane);
	}
	
	protected void showSplashPanel() {
		JPanel contentPane = getFreshContentPane();

		JLabel logoLabel = new JLabel(logo);

		contentPane.add(Box.createVerticalGlue());
		contentPane.add(logoLabel);
		logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		logoLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
		contentPane.add(Box.createVerticalGlue());

		updateContentPane(contentPane);

	}
	
	protected void showWaitingPanel() {
		JPanel contentPane = getFreshContentPane();

		TextLabel instructions = new TextLabel("Please wait ...", Fonts.titleFont);

		contentPane.add(Box.createVerticalGlue());
		contentPane.add(instructions);
		instructions.setAlignmentX(Component.CENTER_ALIGNMENT);
		contentPane.add(Box.createVerticalGlue());

		updateContentPane(contentPane);
	}
	
	// generic
	protected void showCouplingConfirmPanel() {
		JPanel contentPane = getFreshContentPane();

		String waitingMessage = "Welcome";
		JLabel photo = null;
		Visitor visitor;
		try {
			visitor = getOwner();
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
			visitor = getOwner();
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

		String waitingMessage = getName() + " instructions (should be defined!)";
		
		TextLabel title = new TextLabel(waitingMessage, Fonts.titleFont);
		
		contentPane.add(Box.createVerticalGlue());
		contentPane.add(title);
		title.setAlignmentX(Component.CENTER_ALIGNMENT);
		contentPane.add(Box.createVerticalGlue());

		updateContentPane(contentPane);
	}
	
	private void coupling () {
		// display coupling confirmation for a few seconds
		showCouplingConfirmPanel();
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// then show instructions
		showCouplingInstructionPanel();
	}
	
	private void decoupling () {
		// display decoupling confirmation for a few seconds
		showDecouplingConfirmPanel();
	}
	
	public void freeInstance () {
		super.freeInstance();
		
		// redisplay splash screen
		showSplashPanel();
	}
	
	public boolean handleMessage (Message receivedMessage) {
		boolean handled = super.handleMessage(receivedMessage);
		
		// instances should only handle messages from their owner?
		try {
			if (receivedMessage.getHost().equals(getOwner().getVisitorHost())) {
				switch (receivedMessage.getMessageCode()) {
				case MessageCodes.In.Instance.ARRIVED:
					coupling();
					break;
				case MessageCodes.In.Instance.DECOUPLING:
					decoupling();
					break;
				default:
					System.out.println("No additional handling by GUIInstance");
					break;
				}
			}
		} catch (InstanceNotOwnedException e) {
			System.out.println("Exception unexpected (instance has no owner); ignoring");
		} catch (NoSuchVisitorException e) {
			System.out.println("Exception unexpected (instance has no owner); ignoring");
		}
		return handled;
	}
	
}
