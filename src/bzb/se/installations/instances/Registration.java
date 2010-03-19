package bzb.se.installations.instances;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.TreeSet;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import bzb.se.exceptions.InstanceNotOwnedException;
import bzb.se.exceptions.MessageNotSentException;
import bzb.se.exceptions.NoSuchVisitorException;
import bzb.se.gui.TextLabel;
import bzb.se.messaging.Message;
import bzb.se.meta.Device;
import bzb.se.meta.MessageCodes;
import bzb.se.meta.Network;
import bzb.se.meta.ui.Dimensions;
import bzb.se.meta.ui.Fonts;
import bzb.se.meta.Packages;
import bzb.se.utility.ImageProcessing;
import bzb.se.visitors.Visitor;

import com.phidgets.InterfaceKitPhidget;
import com.phidgets.PhidgetException;
import com.phidgets.event.InputChangeEvent;
import com.phidgets.event.InputChangeListener;

public class Registration extends GUIInstance {
	
	private SwitchListener switchListener;
	
	public Registration () {
		super();
	}
	
	public boolean setPortAndStart (int desiredPort) {
		boolean portSet = super.setPortAndStart(desiredPort);
		
		if (portSet) {
			// registration specific stuff
			switchListener = new SwitchListener();
			switchListener.begin();
		}
		return portSet;
	}
	
	public void close (boolean freeInstance) {
		super.close(freeInstance);

		if (switchListener != null) {
			switchListener.stop();
			switchListener = null;
		}
	}
	
	protected void showSplashPanel() {
		JPanel contentPane = getFreshContentPane();

		FileFilter pngFilter = new FileFilter() {
			public boolean accept(File file) {
				if (!file.isDirectory()
						&& (file.getName().endsWith("png") || file.getName()
								.endsWith("PNG"))) {
					return true;
				} else {
					return false;
				}
			}
		};

		File photoDir = new File(Packages.outputDir + "photos/");
		File[] photoFiles = photoDir.listFiles(pngFilter);

		int widthLeft = Device.INSTALLATION_RES[0];
		int heightLeft = Device.INSTALLATION_RES[1];

		int rowHeight = 0;
		
		contentPane.add(Box.createVerticalGlue());

		JPanel row = new JPanel();
		row.setOpaque(false);
		row.setLayout(new BoxLayout(row, BoxLayout.LINE_AXIS));

		TreeSet uniqueList = new TreeSet();
		
		int limit = 0;
		
		while (heightLeft >= 0 && photoFiles.length > 0 && limit < 200) {
			try {
				BufferedImage photo = null;
				while (uniqueList.size() < photoFiles.length) {
					Integer i = new Integer(
							(int) (Math.random() * photoFiles.length));
					if (!uniqueList.contains(i)) {
						uniqueList.add(i);
						photo = ImageIO.read(photoFiles[i.intValue()]);
						break;
					}
				}

				if (photo != null && widthLeft - photo.getWidth() >= 0) {
					ImageIcon photoIcon = new ImageIcon(photo);
					JLabel photoLabel = new JLabel(photoIcon);
					row.add(Box.createHorizontalGlue());
					row.add(photoLabel);

					widthLeft -= photo.getWidth();

					if (photo.getHeight() > rowHeight) {
						rowHeight = photo.getHeight();
					}

					if (heightLeft - rowHeight < 0) {
						break;
					}
				} else {
					heightLeft -= rowHeight;

					row.add(Box.createVerticalGlue());
					contentPane.add(row);
					contentPane.add(Box.createVerticalGlue());

					row = new JPanel();
					row.setOpaque(false);
					row.setLayout(new BoxLayout(row, BoxLayout.LINE_AXIS));
					rowHeight = 0;

					widthLeft = Device.INSTALLATION_RES[0];
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			limit++;
		}
		updateContentPane(contentPane);

	}
	
	protected void showCouplingInstructionPanel() {
		JPanel contentPane = getFreshContentPane();

		TextLabel title = new TextLabel("Starting the registration",
				Fonts.titleFont);

		TextLabel instructions = new TextLabel(
				"<html><center><span style=\"font-weight: bold; color: yellow;\">Place your mobile in the holster</span> (as shown in the photo below).</center></html>",
				Fonts.generalFont);

		ImageIcon couplingPhoto;
		JLabel photo = null;
		try {
			couplingPhoto = new ImageIcon(ImageIO.read(new File(Packages.resourceDir + 
					"registration_coupling.png")));
			photo = new JLabel(couplingPhoto);
		} catch (IOException e) {
			e.printStackTrace();
		}

		contentPane.add(Box.createVerticalGlue());
		contentPane.add(title);
		title.setAlignmentX(Component.CENTER_ALIGNMENT);
		contentPane.add(Box.createRigidArea(Dimensions.verticalSpacer));
		contentPane.add(instructions);
		instructions.setAlignmentX(Component.CENTER_ALIGNMENT);
		contentPane.add(Box.createRigidArea(Dimensions.largeVerticalSpacer));
		contentPane.add(photo);
		photo.setAlignmentX(Component.CENTER_ALIGNMENT);
		contentPane.add(Box.createVerticalGlue());

		updateContentPane(contentPane);
	}

	public final class SpecificMessageCodes {
		public final class In {
			public static final int FACE_CAMERA_CAPTURED = 500;
		}
		
		public final class Out {
			public static final int START_FACE_CAMERA = 500;
			public static final int CAPTURE_FACE_CAMERA = 501;
			public static final int TRANSFER_CAPTURED_FACE = 502;
		}
	}
	
	public boolean handleMessage (Message receivedMessage) {
		boolean handled = super.handleMessage(receivedMessage);
		
		if (!handled) {
			handled = true;
			switch (receivedMessage.getMessageCode()) {
			case SpecificMessageCodes.In.FACE_CAMERA_CAPTURED:
				// mobile has taken and displayed a photo of the visitor -
				// ask them if it's ok
				//showConfirmPhotoPanel();
				
				showWaitingPanel();

				// visitor wants to keep this photo - retrieve it
				// from the
				// mobile and register it
				try {
					Message prod;
					try {
						prod = new Message(SpecificMessageCodes.Out.TRANSFER_CAPTURED_FACE, getOwner().getVisitorHost(), Network.mobilePacketPort);
						prod.send(multi.getMessenger());

						Socket s = getInstanceServer().accept();
						s.setReceiveBufferSize(1000);
						InputStream is = s.getInputStream();
	
						BufferedImage photo = ImageIO.read(is);
						
						int scale = 120 / photo.getHeight();
						photo = ImageProcessing.resize(photo, scale * photo.getWidth(), scale * photo.getHeight());
	
						// output locally for fun
						String url = Packages.outputDir + "photos/" + System.currentTimeMillis()
								+ ".png";
	
						ImageIO.write(photo, "PNG", new File(url));

						// upload image to database
						Visitor owner = getOwner();
						owner.setVisitorPhoto(photo);
						owner.updateVisitorInDB(getMulti().db);

						if (is != null) {
							is.close();
						}
						if (s != null) {
							s.close();
						}
					} catch (InstanceNotOwnedException e2) {
						e2.printStackTrace();
					} catch (NoSuchVisitorException e2) {
						e2.printStackTrace();
					} catch (MessageNotSentException e2) {
						e2.printStackTrace();
					}

					// ... and show summary of collected
					// personal
					// info
					showCollectedInfoPanel();

				} catch (IOException e1) {
					e1.printStackTrace();
				}
				
				break;
			default:
				System.out.println("Unrecognised message code (" + receivedMessage.getMessageCode() + "); ignoring");
				handled = false;
				break;
			}
		}
		return handled;
	}
	
	private void showWelcomePanel() {
		JPanel contentPane = getFreshContentPane();

		TextLabel title = new TextLabel("Welcome to the registration session",
				Fonts.titleFont);

		TextLabel instructions = new TextLabel(
				"<html><center>In the next couple of minutes you will enter some personal details using the keyboard and mouse. This information will be used to alter your experiences at the other installations to be more interesting and useful for you.</center></html>",
				Fonts.generalFont);

		final JButton button = new JButton("Begin");
		button.setFont(Fonts.generalFont);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				button.setEnabled(false);
				showWaitingPanel();
				showPersinfoPanel();
			}
		});

		contentPane.add(Box.createVerticalGlue());
		contentPane.add(title);
		title.setAlignmentX(Component.CENTER_ALIGNMENT);
		contentPane.add(Box.createRigidArea(Dimensions.verticalSpacer));
		contentPane.add(instructions);
		instructions.setAlignmentX(Component.CENTER_ALIGNMENT);
		contentPane.add(Box.createRigidArea(Dimensions.largeVerticalSpacer));
		contentPane.add(button);
		button.setAlignmentX(Component.CENTER_ALIGNMENT);
		contentPane.add(Box.createVerticalGlue());

		updateContentPane(contentPane);
	}
	
	private JTextField visitorNameField;

	private JTextField visitorAgeField;
	
	private void showPersinfoPanel() {
		JPanel contentPane = getFreshContentPane();

		TextLabel title = new TextLabel("Your details", Fonts.titleFont);

		TextLabel instructions = new TextLabel(
				"<html><center><span style=\"font-weight: bold; color: yellow;\">Click inside the \"Name\" text-box using the mouse</span> and <span style=\"font-weight: bold; color: yellow;\">enter your details using the keyboard</span>. You will not be able to continue until you have entered something in each box.</center></html>",
				Fonts.generalFont);

		try {
			TextLabel nameLabel = new TextLabel("Name:", Fonts.generalFont);
	
			visitorNameField = new JTextField();
			String name = getOwner().getVisitorName();
			if (name == null || name.equals("null")) {
				name = "Anon.";
			}
			visitorNameField.setText(getOwner().getVisitorName());
			visitorNameField.setFont(Fonts.generalFont);
			Dimension textBoxSize = new Dimension(50,
					Fonts.generalFont.getSize() * 2);
			visitorNameField.setMaximumSize(textBoxSize);
			visitorNameField.setColumns(20);
	
			TextLabel ageLabel = new TextLabel("Age:", Fonts.generalFont);
	
			visitorAgeField = new JTextField();
			visitorAgeField.setText(String.valueOf(getOwner().getVisitorAge()));
			visitorAgeField.setFont(Fonts.generalFont);
			visitorAgeField.setMaximumSize(textBoxSize);
			visitorAgeField.setColumns(5);
		
			JButton button = new JButton();
			button.setText("Continue");
			button.setFont(Fonts.generalFont);
			button.addActionListener(new RegistrationButtonListener());
	
			contentPane.add(Box.createVerticalGlue());
			contentPane.add(title);
			title.setAlignmentX(Component.CENTER_ALIGNMENT);
			contentPane.add(Box.createRigidArea(Dimensions.verticalSpacer));
			contentPane.add(instructions);
			instructions.setAlignmentX(Component.CENTER_ALIGNMENT);
			contentPane.add(Box.createRigidArea(Dimensions.largeVerticalSpacer));
	
			JPanel namePanel = new JPanel();
			namePanel.setOpaque(false);
			namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.LINE_AXIS));
			namePanel.add(Box.createHorizontalGlue());
			namePanel.add(nameLabel);
			namePanel.add(Box.createRigidArea(new Dimension(5, 0)));
			namePanel.add(visitorNameField);
			namePanel.add(Box.createHorizontalGlue());
			namePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
			contentPane.add(namePanel);
	
			contentPane.add(Box.createRigidArea(Dimensions.verticalSpacer));
	
			JPanel agePanel = new JPanel();
			agePanel.setOpaque(false);
			agePanel.setLayout(new BoxLayout(agePanel, BoxLayout.LINE_AXIS));
			agePanel.add(Box.createHorizontalGlue());
			agePanel.add(ageLabel);
			agePanel.add(Box.createRigidArea(new Dimension(5, 0)));
			agePanel.add(visitorAgeField);
			agePanel.add(Box.createHorizontalGlue());
			agePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
			contentPane.add(agePanel);
	
			contentPane.add(Box.createRigidArea(Dimensions.largeVerticalSpacer));
			contentPane.add(button);
			button.setAlignmentX(Component.CENTER_ALIGNMENT);
			contentPane.add(Box.createVerticalGlue());
		} catch (InstanceNotOwnedException e) {
			e.printStackTrace();
		} catch (NoSuchVisitorException e) {
			e.printStackTrace();
		}
				
		updateContentPane(contentPane);
	}
	
	private class RegistrationButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {

			boolean failed = false;
			if (!(visitorNameField.getText().length() > 0)) {
				visitorNameField.setBackground(Color.RED);
				visitorNameField.setForeground(Color.WHITE);
				System.out.println("*** Visitor hasn't filled the name field ***");

				failed = true;
			} else if (visitorNameField.getBackground() == Color.RED) {
				visitorNameField.setBackground(Color.WHITE);
				visitorNameField.setForeground(Color.BLACK);
			}

			if (!(visitorAgeField.getText().length() > 0)
					|| visitorAgeField.getText().equals("0")) {
				visitorAgeField.setBackground(Color.RED);
				visitorAgeField.setForeground(Color.WHITE);
				System.out.println("*** Visitor hasn't filled the age field ***");

				failed = true;
			} else if (visitorAgeField.getBackground() == Color.RED) {
				visitorAgeField.setBackground(Color.WHITE);
				visitorAgeField.setForeground(Color.BLACK);
			}

			try {
				Integer.parseInt(visitorAgeField.getText());
			} catch (NumberFormatException nfe) {
				visitorAgeField.setBackground(Color.RED);
				visitorAgeField.setForeground(Color.WHITE);
				System.out.println("*** Visitor has filled the age field with something stupid ***");

				failed = true;
			}

			if (!failed) {
				showWaitingPanel();

				new Thread(new Runnable() {
					public void run() {
						// register this personal information
						Visitor owner;
						try {
							owner = getOwner();
							owner.setVisitorAge(Integer.parseInt(visitorAgeField
									.getText()));
							owner.setVisitorName(visitorNameField.getText());
							owner.updateVisitorInDB(getMulti().db);
	
							// show the camera instructions form and start
							// camera on mobile if there is one, otherwise skip it
							if (owner.getVisitorHasFaceCamera()) {
								Message prod = new Message(SpecificMessageCodes.Out.START_FACE_CAMERA, owner.getVisitorHost(), Network.mobilePacketPort);
								try {
									prod.send(multi.getMessenger());
	
									showCameraInstructionsPanel();
								} catch (MessageNotSentException e) {
									e.printStackTrace();
								}
							} else {
								System.out.println("*** Visitor's mobile has no face camera - skipping photo step ***");
	
								showCollectedInfoPanel();
							}
						} catch (InstanceNotOwnedException e) {
							e.printStackTrace();
						} catch (NoSuchVisitorException e) {
							e.printStackTrace();
						}
					}
				}).start();
			}
		}
	}
	
	private void showCameraInstructionsPanel() {
		JPanel contentPane = getFreshContentPane();

		TextLabel title = new TextLabel("Your photograph", Fonts.titleFont);

		TextLabel instructions = new TextLabel(
				"<html><center>Take a look at your mobile - it has been converted into a camera. Adjust your position on your seat so that you can see your face clearly on the mobile screen, then <span style=\"font-weight: bold; color: yellow;\">press the space-bar</span> (as shown in the image below) and hold your pose until you hear the camera snap your photograph.</center></html>",
				Fonts.generalFont);

		ImageIcon buttonPhoto;
		JLabel photo = null;
		try {
			buttonPhoto = new ImageIcon(ImageIO.read(new File(Packages.resourceDir + 
					"registration_button.png")));
			photo = new JLabel(buttonPhoto);
		} catch (IOException e) {
			e.printStackTrace();
		}

		contentPane.add(Box.createVerticalGlue());
		contentPane.add(title);
		title.setAlignmentX(Component.CENTER_ALIGNMENT);
		contentPane.add(Box.createRigidArea(Dimensions.verticalSpacer));
		contentPane.add(instructions);
		instructions.setAlignmentX(Component.CENTER_ALIGNMENT);
		contentPane.add(Box.createRigidArea(Dimensions.largeVerticalSpacer));
		contentPane.add(photo);
		photo.setAlignmentX(Component.CENTER_ALIGNMENT);
		contentPane.add(Box.createVerticalGlue());
		
		demoFrame.addKeyListener(new KeyListener() {

			public void keyPressed(KeyEvent ke) {
				if (ke.getKeyCode() == KeyEvent.VK_SPACE) {
					System.out.println("Space-bar pressed");
					
					// camera button is pressed - take picture
					showWaitingPanel();
					Message prod;
					try {
						prod = new Message(SpecificMessageCodes.Out.CAPTURE_FACE_CAMERA, getOwner().getVisitorHost(), Network.mobilePacketPort);
						prod.send(multi.getMessenger());
					
						demoFrame.removeKeyListener(this);
					} catch (MessageNotSentException e) {
						e.printStackTrace();
					} catch (InstanceNotOwnedException e1) {
						e1.printStackTrace();
					} catch (NoSuchVisitorException e1) {
						e1.printStackTrace();
					}

				}
			}

			public void keyReleased(KeyEvent ke) {}

			public void keyTyped(KeyEvent ke) {}
			
		});

		updateContentPane(contentPane);
		
		demoFrame.setFocusable(true);
		demoFrame.requestFocus();
	}
	
	private void showCollectedInfoPanel() {
		completeExperience();

		JPanel contentPane = getFreshContentPane();

		TextLabel title = new TextLabel("You're done!", Fonts.titleFont);

		Visitor owner;
		try {
			owner = getOwner();
		
			String collectedText = "<html><center>You have registered as <span style=\"font-weight: bold; color: green;\">"
					+ owner.getVisitorName() + "</span>, aged <span style=\"font-weight: bold; color: green;\">" + owner.getVisitorAge() + "</span>";
	
			JLabel photo = null;
			if (owner.getVisitorPhoto() != null) {
				collectedText += " and you look like this:</center></html>";
				photo = new JLabel(new ImageIcon(owner.getVisitorPhoto()));
			}
	
			TextLabel collectedDetails = new TextLabel(collectedText,
					Fonts.generalFont);
	
			TextLabel instructions = new TextLabel(
					"<html><center>Now <span style=\"font-weight: bold; color: yellow;\">remove your phone from the holster</span>. By choosing 'Find exhibits' on your phone you will be able to find suitable exhibits to visit.</center></html>",
					Fonts.generalFont);
	
			contentPane.add(Box.createVerticalGlue());
			contentPane.add(title);
			title.setAlignmentX(Component.CENTER_ALIGNMENT);
			contentPane.add(Box.createRigidArea(Dimensions.verticalSpacer));
			contentPane.add(collectedDetails);
			collectedDetails.setAlignmentX(Component.CENTER_ALIGNMENT);
			if (photo != null) {
				contentPane.add(Box.createRigidArea(Dimensions.verticalSpacer));
				contentPane.add(photo);
				photo.setAlignmentX(Component.CENTER_ALIGNMENT);
			}
			contentPane.add(Box.createRigidArea(Dimensions.largeVerticalSpacer));
			contentPane.add(instructions);
			instructions.setAlignmentX(Component.CENTER_ALIGNMENT);
			contentPane.add(Box.createVerticalGlue());
		} catch (InstanceNotOwnedException e) {
			e.printStackTrace();
		} catch (NoSuchVisitorException e) {
			e.printStackTrace();
		}
		
		updateContentPane(contentPane);
	}
	
	/*private void showConfirmPhotoPanel() {
		JPanel contentPane = getFreshContentPane();

		TextLabel title = new TextLabel("Your photograph", Fonts.titleFont);

		TextLabel instructions = new TextLabel(
				"<html><center>Your photo has been taken - check your mobile and <span style=\"font-weight: bold; color: yellow;\">use the touchpad to click on one of the buttons below</span> to indicate whether you want to retake it.</center></html>",
				Fonts.generalFont);

		final JButton OKButton = new JButton("Keep this photo");
		OKButton.setFont(Fonts.generalFont);
		OKButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				OKButton.setEnabled(false);

				showWaitingPanel();

				// visitor wants to keep this photo - retrieve it
				// from the
				// mobile and register it
				try {
					Message prod;
					try {
						prod = new Message(SpecificMessageCodes.Out.TRANSFER_CAPTURED_FACE, getOwner().getVisitorHost(), Network.mobilePacketPort);
						prod.send(multi.getMessenger());

						Socket s = getInstanceServer().accept();
						s.setReceiveBufferSize(1000);
						InputStream is = s.getInputStream();
	
						BufferedImage photo = ImageIO.read(is);
						
						int scale = 120 / photo.getHeight();
						photo = ImageProcessing.resize(photo, scale * photo.getWidth(), scale * photo.getHeight());
	
						// output locally for fun
						String url = Packages.outputDir + "photos/" + System.currentTimeMillis()
								+ ".png";
	
						ImageIO.write(photo, "PNG", new File(url));

						// upload image to database
						Visitor owner = getOwner();
						owner.setVisitorPhoto(photo);
						owner.updateVisitorInDB(getMulti().db);

						if (is != null) {
							is.close();
						}
						if (s != null) {
							s.close();
						}
					} catch (InstanceNotOwnedException e2) {
						e2.printStackTrace();
					} catch (NoSuchVisitorException e2) {
						e2.printStackTrace();
					} catch (MessageNotSentException e2) {
						e2.printStackTrace();
					}

					// ... and show summary of collected
					// personal
					// info
					showCollectedInfoPanel();

				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});

		final JButton retakeButton = new JButton("Let me try again");
		retakeButton.setFont(Fonts.generalFont);
		retakeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				retakeButton.setEnabled(false);
				showWaitingPanel();

				// visitor doesn't want to keep this photo - restart
				// camera
				// on mobile
				Message prod;
				try {
					prod = new Message(SpecificMessageCodes.Out.START_FACE_CAMERA, getOwner().getVisitorHost(), Network.mobilePacketPort);
					prod.send(multi.getMessenger());
					showCameraInstructionsPanel();
				} catch (InstanceNotOwnedException e1) {
					e1.printStackTrace();
				} catch (NoSuchVisitorException e1) {
					e1.printStackTrace();
				} catch (MessageNotSentException e1) {
					e1.printStackTrace();
				}
			}
		});

		contentPane.add(Box.createVerticalGlue());
		contentPane.add(title);
		title.setAlignmentX(Component.CENTER_ALIGNMENT);
		contentPane.add(Box.createRigidArea(Dimensions.verticalSpacer));
		contentPane.add(instructions);
		instructions.setAlignmentX(Component.CENTER_ALIGNMENT);
		contentPane.add(Box.createRigidArea(Dimensions.largeVerticalSpacer));

		JPanel buttonsPanel = new JPanel();
		buttonsPanel
				.setLayout(new BoxLayout(buttonsPanel, BoxLayout.LINE_AXIS));
		buttonsPanel.setOpaque(false);
		buttonsPanel.add(Box.createHorizontalGlue());
		buttonsPanel.add(OKButton);
		buttonsPanel.add(Box.createRigidArea(Dimensions.horizontalSpacer));
		buttonsPanel.add(retakeButton);
		buttonsPanel.add(Box.createHorizontalGlue());

		contentPane.add(buttonsPanel);
		buttonsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		contentPane.add(Box.createVerticalGlue());

		updateContentPane(contentPane);
	}*/
	
//	 Pressure checker (is phone in place?)

	private class SwitchListener implements InputChangeListener {

		private InterfaceKitPhidget phidkit;

		public void begin() {
			try {
				phidkit = new InterfaceKitPhidget();
				phidkit.openAny();
				phidkit.addInputChangeListener(this);
			} catch (PhidgetException e) {
				e.printStackTrace();
			}
		}

		public void stop() {
			try {
				if (phidkit != null) {
					phidkit.close();
				}
			} catch (PhidgetException e1) {
				e1.printStackTrace();
			}
		}
		
		public void inputChanged(InputChangeEvent e) {
			System.out.println(e.getIndex() + " " + e.getState());
			
			new Thread(new Runnable () {
				public void run () {
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					phidkit.addInputChangeListener(SwitchListener.this);
				}
			}).start();
			
			if (e.getIndex() == 0) {
				phidkit.removeInputChangeListener(this);
				try {
					if (getOwner() != null) {
						if (e.getState() == false) {
							System.out.println("holster pressed");

							// mobile is in holster - begin experience
							showWelcomePanel();

							Visitor owner = getOwner();
							Message reply = new Message(MessageCodes.Out.Instance.COUPLED, owner.getVisitorHost(), Network.mobilePacketPort);
							reply.send(multi.getMessenger());
						} else {
							System.out.println("holster unpressed");

							Visitor owner = getOwner();
							Message reply = new Message(MessageCodes.Out.Instance.DECOUPLED, owner.getVisitorHost(), Network.mobilePacketPort);
							reply.send(multi.getMessenger());
							
							freeInstance();
						}
					}
				} catch (InstanceNotOwnedException e1) {
					System.out.println("Exception expected (instance not owned); ignoring");
				} catch (NoSuchVisitorException e1) {
					e1.printStackTrace();
				} catch (MessageNotSentException e1) {
					e1.printStackTrace();
				}
			} else {
				System.out.println("Dampening ...");
			}
		}
	}
	
}
