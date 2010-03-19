package bzb.se.installations.thoughts;

import java.awt.image.BufferedImage;

public class Thought {
	
	private String authorHost = null;
	
	private BufferedImage photo = null;
	
	private int timeTaken = -1;
	
	public Thought (String authorHost, BufferedImage photo, int timeTaken) {
		this.authorHost = authorHost;
		this.photo = photo;
		this.timeTaken = timeTaken;
	}
	
	public String getAuthorHost () {
		return authorHost;
	}
	
	public BufferedImage getPhoto () {
		return photo;
	}
	
	public int getTimetaken() {
		return timeTaken;
	}
}
