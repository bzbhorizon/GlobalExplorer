package bzb.se.messaging;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

import bzb.se.exceptions.MessageNotSentException;

public class Message {
	
	private String host;
	
	private int port;
	
	private int messageCode;

	private Vector messageParts;
	
	public Message (DatagramPacket receivedDatagram) {
		messageParts = new Vector();
		host = receivedDatagram.getAddress().getHostAddress();
		port = receivedDatagram.getPort();
		
		byte[] payload = receivedDatagram.getData();
		String payloadStr = new String();
		for (int i = 0; i < payload.length; i++) {
			if (payload[i] != 0) {
				payloadStr += (char) payload[i];
			}
		}
		StringTokenizer t = new StringTokenizer(payloadStr, "/", false);
		try {
			messageCode = Integer.parseInt(t.nextToken());
			while (t.hasMoreTokens()) {
				messageParts.addElement(t.nextToken());
			}
		} catch (NumberFormatException nfe) {
			System.out.println("Received badly formed message: " + payloadStr + "; ignoring");
		}
	}
	
	public Message (int messageCode, String recipientHost, int recipientPort) {
		this.messageCode = messageCode;
		this.host = recipientHost;
		this.port = recipientPort;
		this.messageParts = new Vector();
	}
	
	public void addMessagePart (String messagePart) {
		this.messageParts.addElement(messagePart);
	}
	
	public String getHost () {
		return host;
	}
	
	public int getPort () {
		return port;
	}
	
	public int getMessageCode () {
		return messageCode;
	}
	
	public Vector getMessageParts () {
		return messageParts;
	}
	
	public String getCompleteMessageString () {
		if (getMessageString() != null) {
			return messageCode + "/" + getMessageString();
		} else {
			return String.valueOf(messageCode);
		}
	}
	
	public String getMessageString () {
		String messageString = null;
		if (messageParts != null) {
			Enumeration e = messageParts.elements();
			while (e.hasMoreElements()) {
				if (messageString == null) {
					messageString = (String) e.nextElement() + "/";
				} else {
					messageString += (String) e.nextElement() + "/";
				}
			}
			if (messageString != null && messageString.endsWith("/")) {
				messageString = messageString.substring(0, messageString.length() - 1);
			}
		}
		return messageString;
	}
	
	public DatagramPacket getMessageDatagram () {
		byte[] payload = getCompleteMessageString().getBytes();
		DatagramPacket dg = null;
		try {
			dg = new DatagramPacket(payload, 0, payload.length, InetAddress.getByName(host), port);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return dg;
	}
	
	public void send(DatagramSocket ds) throws MessageNotSentException {
		if (ds != null) {
			try {
				ds.send(getMessageDatagram());
			} catch (IOException e) {
				throw (new MessageNotSentException());
			}
			System.out.println("Sent \"" + getCompleteMessageString() + "\" to " + host
					+ ":" + port);
		} else {
			throw (new MessageNotSentException());
		}
	}

}
