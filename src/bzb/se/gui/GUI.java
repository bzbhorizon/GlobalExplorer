package bzb.se.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
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

import bzb.se.installations.Multi;
import bzb.se.meta.Device;
import bzb.se.meta.ui.Fonts;
import bzb.se.meta.Packages;

public class GUI {

	private Multi multi;
	
//	 reusable stuff
	protected DemoFrame demoFrame;

	protected ImageIcon logo;
	
	protected ImageIcon skey1Icon;

	protected ImageIcon skey2Icon;

	protected ImageIcon joypadIcon;

	protected ImageIcon leftArrowIcon;

	protected ImageIcon rightArrowIcon;
	
	public GUI (Multi multi) {
		this.multi = multi;
	}
	
	public void setupGUI () {
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
	
	protected class DemoFrame extends JFrame {
		
		public DemoFrame() {
			super();

			this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
			
			this.addWindowListener( new WindowAdapter() {
				public void windowClosing(WindowEvent evt) {
					multi.close();
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
	
	public JPanel getFreshContentPane() {
		JPanel contentPane = (JPanel) demoFrame.getContentPane();

		contentPane.removeAll();

		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
		contentPane.setOpaque(true);
		contentPane.setBackground(Color.BLACK);
		contentPane.setBorder(BorderFactory.createLineBorder(Color.WHITE));

		return contentPane;
	}

	public void updateContentPane(JPanel contentPane) {
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
	
	protected DemoFrame getDemoFrame () {
		return demoFrame;
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
		
		TextLabel title = new TextLabel("Welcome", Fonts.titleFont);

		contentPane.add(Box.createVerticalGlue());
		contentPane.add(title);
		title.setAlignmentX(Component.CENTER_ALIGNMENT);
		contentPane.add(Box.createVerticalGlue());

		updateContentPane(contentPane);
	}
	
	protected void showDecouplingConfirmPanel() {
		JPanel contentPane = getFreshContentPane();
		
		TextLabel title = new TextLabel("Good-bye", Fonts.titleFont);

		contentPane.add(Box.createVerticalGlue());
		contentPane.add(title);
		title.setAlignmentX(Component.CENTER_ALIGNMENT);
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
	
	protected class TextLabel extends JLabel {

		public TextLabel(String title, Font f) {
			super(title, null, JLabel.CENTER);
			
			this.setForeground(Color.WHITE);
			this.setFont(f);
			this.setHorizontalTextPosition(JLabel.CENTER);
			this.setMaximumSize(new Dimension(800, 600));
		}

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
	
	public void freeGUI () {
		// redisplay splash screen
		showSplashPanel();
	}
	
}
