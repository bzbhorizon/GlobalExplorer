package bzb.se.installations;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Vector;

import bzb.se.DB;
import bzb.se.exceptions.ConnectNotReadyException;
import bzb.se.exceptions.InstanceNotOwnedException;
import bzb.se.exceptions.MessageNotSentException;
import bzb.se.exceptions.NoFreeInstanceException;
import bzb.se.exceptions.NoSuchPointOfInterestException;
import bzb.se.exceptions.NoSuchVisitorException;
import bzb.se.installations.instances.Instance;
import bzb.se.macroenvironment.EnvironmentMap;
import bzb.se.messaging.Message;
import bzb.se.meta.MessageCodes;
import bzb.se.meta.Network;
import bzb.se.meta.Packages;
import bzb.se.meta.Requirements;
import bzb.se.meta.Timings;

public class Multi {

	// profile
	private int capacity = 0;
	
	private int freeCapacity = 0;

	private String instanceName = null;

	private int requiresBT = Requirements.NOT_REQUIRED;

	private int requiresMainCamera = Requirements.NOT_REQUIRED;

	private int requiresFaceCamera = Requirements.NOT_REQUIRED;
	
	private String[] prerequisiteInstances = null;
	
	private String description = null;

	// service objects
	private Vector instances;

	private DatagramSocket multiSock;

	public DB db;
	
	private MessageRouter router;

	public Multi(String instanceName, int desiredCapacity, int requiresBT,
			int requiresMainCamera, int requiresFaceCamera, String[] prerequisiteInstances,
			String description) {
		choosePort();
		setInstanceName(instanceName);
		setRequirements(requiresBT, requiresMainCamera, requiresFaceCamera);
		startInstances(desiredCapacity);
		setPrerequisiteInstances(prerequisiteInstances);
		setDescription(description);
		
		db = new DB();
		registerInDB();
		
		router = new MessageRouter();
		new Thread(router).start();
		
		new Thread(new DatabaseActivity()).start();
	}
	
	public DatagramSocket getMessenger () {
		if (multiSock != null) {
			return multiSock;
		} else {
			return null;
		}
	}

	private void setRequirements(int requiresBT, int requiresMainCamera,
			int requiresFaceCamera) {
		this.requiresBT = requiresBT;
		this.requiresMainCamera = requiresMainCamera;
		this.requiresFaceCamera = requiresFaceCamera;
	}
	
	private void setPrerequisiteInstances (String[] prerequisiteInstances) {
		this.prerequisiteInstances = prerequisiteInstances;
	}

	private void setDescription (String description) {
		this.description = description;
	}
	
	private void choosePort() {
		boolean portChosen = false;
		for (int desiredPort = Network.openPortLow; desiredPort <= Network.openPortHigh
				&& !portChosen; desiredPort++) {
			try {
				multiSock = new DatagramSocket(desiredPort);
				
				portChosen = true;
			} catch (IOException e) {
				System.out.println("Expected exception (port in use): "
						+ e.getMessage());
			}
		}
		if (!portChosen) {
			System.out.println("No port available for multi; exiting");
			close();
		} else {
			System.out.println("Multi takes port: " + multiSock.getLocalPort());
		}
	}

	public InetAddress getHost() {
		if (multiSock != null) {
			try {
				return InetAddress.getLocalHost();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public String getInstanceName () {
		return instanceName;
	}
	
	public int getCapacity () {
		return capacity;
	}

	private void registerInDB() {
		unregisterFromDB();

		System.out.println("Registering an installation: " + instanceName
				+ " (C: " + capacity + ", host: " + getHost().getHostAddress()
				+ " with these requirements: main camera support "
				+ requiresMainCamera + ", face camera support "
				+ requiresFaceCamera + ", BT support " + requiresBT + ", described as:" 
				+ description + ")");

		try {
			db.stmt
					.executeUpdate("INSERT INTO installations (host, port, instanceName, capacity, freeCapacity, requiresMainCamera, requiresFaceCamera, requiresBT, description) VALUES ('"
							+ getHost().getHostAddress()
							+ "', "
							+ multiSock.getLocalPort()
							+ ", '"
							+ instanceName
							+ "', "
							+ capacity
							+ ", "
							+ capacity
							+ ", "
							+ requiresMainCamera
							+ ", "
							+ requiresFaceCamera
							+ ", "
							+ requiresBT
							+ ", '" 
							+ description
							+ "')");
			
			updateFreeCapacityInDB();
			
			if (prerequisiteInstances != null) {
				System.out.println("Registering prerequisites for installation " + instanceName);
	
				for (int i = 0; i < prerequisiteInstances.length; i++) {
					try {
						db.stmt
								.executeUpdate("INSERT INTO storylines (instanceName, prerequisiteInstanceName) VALUES ('"
										+ instanceName
										+ "', '"
										+ prerequisiteInstances[i]
										+ "')");
						System.out.println("Added " + prerequisiteInstances[i] + " as prerequisite to " + instanceName);
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void unregisterFromDB() {
		// remove instances from DB
		try {
			ResultSet rs;

			System.out
					.println("Checking for installations registered with this name|host");

			rs = db.stmt
					.executeQuery("SELECT * FROM installations WHERE host='"
							+ getHost().getHostAddress()
							+ "' AND instanceName='" + instanceName + "'");
			if (rs.next()) {
				System.out
						.println("Installation registered with this name|host: removing");
				
				try {
					EnvironmentMap.removePOIFromDB(getInstanceName(), db);
				} catch (NoSuchPointOfInterestException e) {
					System.out.println("Exception expected: no existing POI found for " + instanceName + "; ignoring");
				}
				
				db.stmt.executeUpdate("DELETE FROM installations WHERE host='"
						+ getHost().getHostAddress() + "' AND instanceName='"
						+ instanceName + "'");
				
				// remove prerequisites
				db.stmt.executeUpdate("DELETE FROM storylines WHERE instanceName='"
						+ instanceName + "'");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void startInstances(int desiredCapacity) {
		int capacity = 0;
		if (desiredCapacity > 0) {
			instances = new Vector();
			for (int desiredPort = Network.openPortLow; desiredPort <= Network.openPortHigh
					&& instances.size() < desiredCapacity; desiredPort++) {
				Instance temp = null;
				try {
					temp = (Instance) (Class
							.forName(Packages.instancePackage + "."
									+ getInstanceName())).newInstance();
					temp.setMulti(this);
					if (temp.setPortAndStart(desiredPort)) {
						instances.add(temp);
					} else {
						temp.close(false);
						System.out.println("Expected exception (port in use); skipping port");
					}
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}

			capacity = instances.size();
			if (desiredCapacity > capacity) {
				System.out.println("Limiting capacity to " + capacity
						+ " (reducing from " + desiredCapacity + ")");
			}
		}
		this.capacity = capacity;
		System.out.println("Set capacity to " + this.capacity);
	}
	
	private void inspectInstances () {
		System.out.println("Inspecting instances at " + instanceName + " multi");
		if (instances != null) {
			Iterator i = instances.iterator();
			while (i.hasNext()) {
				Instance instance = (Instance) i.next();
				try {
					System.out.println(instanceName + " (" + getHost().getHostName() + ":" + instance.getPort() + ") instance owned by " + instance.getOwner().getVisitorHost());
				} catch (InstanceNotOwnedException e) {
					try {
						System.out.println(instanceName + " (" + getHost().getHostName() + ":" + instance.getPort() + ") instance unassigned ");
					} catch (ConnectNotReadyException cnre) {
						cnre.printStackTrace();
					}
				} catch (ConnectNotReadyException e) {
					e.printStackTrace();
				} catch (NoSuchVisitorException e) {
					e.printStackTrace();
				}
			}
		} else {
			System.out.println("No instances");
		}
	}
	
	public Vector getInstances () {
		return instances;
	}
	
	public void updateFreeCapacityInDB () {
		freeCapacity = capacity;
		if (instances != null) {
			Iterator i = instances.iterator();
			while (i.hasNext()) {
				Instance instance = (Instance) i.next();
				try {
					instance.getOwner();
					freeCapacity--;
					System.out.println("Reducing capacity to " + freeCapacity);
				} catch (InstanceNotOwnedException e) {
					System.out.println("Exception expected (instance not owned); ignoring");
				} catch (NoSuchVisitorException e) {
					System.out.println("Exception expected (instance not owned); ignoring");
				}
			}
			
			try {
				System.out.println("Updating free capacity at " + instanceName + " multi from " + capacity + " to " + freeCapacity);
				db.stmt.executeUpdate("UPDATE installations SET freeCapacity=" + freeCapacity + " WHERE host='"
						+ getHost().getHostAddress() + "' AND instanceName='"
						+ getInstanceName() + "'");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	private void setInstanceName(String instanceName) {
		String fullInstanceName = null;
		try {
			fullInstanceName = Packages.instancePackage + "." + instanceName;
			Class.forName(fullInstanceName);
		} catch (ClassNotFoundException e) {
			System.out.println("Expected exception (bad instance name): "
					+ e.getMessage() + "; exiting");
			close();
		} finally {
			this.instanceName = instanceName;
			System.out.println("Set instance name to " + this.instanceName);
		}
	}

	public void close() {
		new Thread(new Runnable() {
			public void run() {
				multiSock.disconnect();
				multiSock.close();
			}
		}).start();
		closeInstances();
		unregisterFromDB();
	}

	private void closeInstances() {
		if (instances != null) {
			Iterator i = instances.iterator();
			while (i.hasNext()) {
				((Instance) i.next()).close(true);
			}
		}
	}
	
	private int assignInstance (String visitorHost) throws NoFreeInstanceException {
		if (instances != null) {			
			Iterator i = instances.iterator();
			while (i.hasNext()) {
				try {
					Instance instance = (Instance) i.next();
					instance.setOwner(visitorHost);
					updateFreeCapacityInDB();
					return instance.getPort();
				} catch (Exception e) {
					System.out.println("Exception expected: instance already assigned");
				}
			}
		}
		throw (new NoFreeInstanceException());
	}
	
	public class DatabaseActivity implements Runnable {
		
		public void run () {
			while (true) {
				System.out
						.println("Keep the database connection alive");
				try {
					db.stmt.executeQuery("SELECT * FROM installations");
				} catch (SQLException e) {
					e.printStackTrace();
				}
				try {
					Thread.sleep(Timings.KeepDatabaseAliveInterval);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	public class MessageRouter implements Runnable {
		
		public void run() {
			while (true) {
				try {
					byte[] payload = new byte[50];
					DatagramPacket receiveDatagram = new DatagramPacket(payload,
							payload.length);
					
					System.out.println(instanceName + " multi waiting");
					multiSock.receive(receiveDatagram);
					
					Message msg = new Message(receiveDatagram);
				
					System.out.println(instanceName + " multi received " + msg.getMessageString() + " (type " + msg.getMessageCode() + ") from " + msg.getHost());
					boolean handled = false;
					handled = handleMessage (msg);
					
					if (!handled) {
						System.out.println("Passing message to instances");
						
						Iterator i = instances.iterator();
						while (i.hasNext()) {
							Instance instance = (Instance) i.next();
							try {
								String owner = instance.ownerHost;
								if (owner.equals(msg.getHost())) {
									handled = instance.handleMessage(msg);
									break;
								}
							} catch (Exception e) {
								System.out.println("Exception expected (instance is unassigned); ignoring");
							}
						}
					}
					
					if (!handled) {
						System.out.println("Message: " + msg.getMessageString() + " (" + msg.getMessageCode() + ") remains unhandled");
					}

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	protected boolean handleMessage (Message receivedMessage) {
		boolean handled = true;
		switch (receivedMessage.getMessageCode()) {
		case MessageCodes.In.Multi.ASSIGN_INSTANCE:
			System.out.println("Multi 0: assign instance");
			Message reply;
			try {
				try {
					String desiredInstanceName = (String) receivedMessage.getMessageParts().elementAt(0);
					if (desiredInstanceName.equals(instanceName)) {
						int instancePort = assignInstance(receivedMessage.getHost());
						reply = new Message(MessageCodes.Out.Multi.INSTANCE_PORT, receivedMessage.getHost(), receivedMessage.getPort());
						reply.addMessagePart(String.valueOf(instancePort));
						reply.addMessagePart(instanceName);
						reply.send(multiSock);
					} else {
						System.out.println("Exception unexpected (no instance of this type): rejecting");
						reply = new Message(MessageCodes.Out.Multi.NO_FREE_INSTANCE, receivedMessage.getHost(), receivedMessage.getPort());
						reply.send(multiSock);
					}
				} catch (NoFreeInstanceException nfie) {
					System.out.println("Exception expected (no free instance): rejecting");
					reply = new Message(MessageCodes.Out.Multi.NO_FREE_INSTANCE, receivedMessage.getHost(), receivedMessage.getPort());
					reply.send(multiSock);
				}
			} catch (MessageNotSentException mnse) {
				System.out.println("Message couldn't be sent; ignoring");
			}
			inspectInstances();
			break;
		default:
			handled = false;
			break;
		}
		return handled;
	}

}
