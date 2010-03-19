package bzb.se.visitors;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.TreeSet;

import javax.imageio.ImageIO;

import bzb.se.DB;
import bzb.se.exceptions.NoSuchVisitorException;
import bzb.se.meta.Installations;
import bzb.se.meta.Packages;

public class Visitor {

	private String visitorHost = null;

	private String visitorName = null;

	private boolean visitorHasBT = false;

	private boolean visitorHasMainCamera = false;

	private boolean visitorHasFaceCamera = false;

	private int visitorAge = 0;

	private String visitorLastKnownPosition = Installations.DECOUPLED_ID;

	private BufferedImage visitorPhoto = null;

	private TreeSet completeexperiences = new TreeSet();

	private int[] visitorScreenDimensions = new int[2];

	public Visitor(String visitorHost, String visitorName,
			boolean visitorHasBT, boolean visitorHasMainCamera,
			boolean visitorHasFaceCamera, int visitorAge,
			String visitorLastKnownPosition, BufferedImage visitorPhoto,
			TreeSet completeexperiences, int[] visitorScreenDimensions) {
		this.visitorHost = visitorHost;
		this.visitorName = visitorName;
		this.visitorHasBT = visitorHasBT;
		this.visitorHasMainCamera = visitorHasMainCamera;
		this.visitorHasFaceCamera = visitorHasFaceCamera;
		this.visitorAge = visitorAge;
		this.visitorLastKnownPosition = visitorLastKnownPosition;
		this.visitorPhoto = visitorPhoto;
		this.completeexperiences = completeexperiences;
		this.visitorScreenDimensions = visitorScreenDimensions;
	}

	public Visitor(String visitorHost, DB db) throws NoSuchVisitorException {
		ResultSet rs;

		System.out.println("Checking for visitors registered with this host");

		try {
			rs = db.stmt.executeQuery("SELECT * FROM visitors WHERE host='"
					+ visitorHost + "'");
			if (rs.next()) {
				System.out.println("Visitor found with this host: "
						+ rs.getString("name"));

				this.visitorHost = rs.getString("host");
				visitorName = rs.getString("name");
				visitorHasBT = rs.getBoolean("hasBT");
				visitorHasMainCamera = rs.getBoolean("hasMainCamera");
				visitorHasFaceCamera = rs.getBoolean("hasFaceCamera");
				visitorAge = rs.getInt("age");
				visitorLastKnownPosition = rs.getString("lastKnownPosition");
				visitorScreenDimensions[0] = rs.getInt("screenWidth");
				visitorScreenDimensions[1] = rs.getInt("screenHeight");

				byte[] visitorPhotoBytes = rs.getBytes("photoData");
				if (visitorPhotoBytes != null) {
					InputStream in = new ByteArrayInputStream(visitorPhotoBytes);
					try {
						visitorPhoto = ImageIO.read(in);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				completeexperiences = getCompleteExperiencesFromDB(db);
			} else {
				throw (new NoSuchVisitorException());
			}
		} catch (SQLException e) {
			throw (new NoSuchVisitorException());
		}
	}

	public void setVisitorHost(String visitorHost) {
		this.visitorHost = visitorHost;
	}

	public void setVisitorName(String visitorName) {
		this.visitorName = visitorName;
	}

	public void setVisitorHasBT(boolean visitorHasBT) {
		this.visitorHasBT = visitorHasBT;
	}

	public void setVisitorHasMainCamera(boolean visitorHasMainCamera) {
		this.visitorHasMainCamera = visitorHasMainCamera;
	}

	public void setVisitorHasFaceCamera(boolean visitorHasFaceCamera) {
		this.visitorHasFaceCamera = visitorHasFaceCamera;
	}

	public void setVisitorAge(int visitorAge) {
		this.visitorAge = visitorAge;
	}

	public void setVisitorLastKnownPosition(String visitorLastKnownPosition) {
		this.visitorLastKnownPosition = visitorLastKnownPosition;
	}

	public void setVisitorPhoto(BufferedImage visitorPhoto) {
		this.visitorPhoto = visitorPhoto;
	}

	public void setCompleteExperiences(TreeSet completeexperiences) {
		this.completeexperiences = completeexperiences;
	}

	public void setVisitorScreenDimensions(int[] visitorScreenDimensions) {
		this.visitorScreenDimensions = visitorScreenDimensions;
	}

	public void addCompleteExperience(String instanceName) {
		completeexperiences.add(instanceName);
	}

	public String getVisitorHost() {
		return visitorHost;
	}

	public String getVisitorName() {
		return visitorName;
	}

	public boolean getVisitorHasBT() {
		return visitorHasBT;
	}

	public boolean getVisitorHasMainCamera() {
		return visitorHasMainCamera;
	}

	public boolean getVisitorHasFaceCamera() {
		return visitorHasFaceCamera;
	}

	public int getVisitorAge() {
		return visitorAge;
	}

	public String getVisitorLastKnownPosition() {
		return visitorLastKnownPosition;
	}

	public BufferedImage getVisitorPhoto() {
		return visitorPhoto;
	}

	public TreeSet getCompleteExperiences() {
		return completeexperiences;
	}

	public TreeSet getCompleteExperiencesFromDB(DB db) {
		TreeSet completeexperiences = new TreeSet();

		ResultSet rs;

		try {
			rs = db.stmt
					.executeQuery("SELECT * FROM completeexperiences WHERE visitorHost='"
							+ visitorHost + "'");
			while (rs.next()) {
				System.out.println("Complete experience at " + rs.getString("instanceName") + " found for this host");

				completeexperiences.add(rs.getString("instanceName"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return completeexperiences;
	}

	public int[] getVisitorScreenDimensions() {
		return visitorScreenDimensions;
	}

	public void unregisterFromDB(DB db) {
		// remove instances from DB
		try {
			db.stmt.executeUpdate("DELETE FROM visitors WHERE host='"
					+ visitorHost + "'");

			db.stmt
					.executeUpdate("DELETE FROM completeexperiences WHERE visitorHost='"
							+ visitorHost + "'");

			db.stmt
			.executeUpdate("DELETE FROM friends WHERE hostA='"
					+ visitorHost + "' OR hostB='" + visitorHost + "'");

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void registerInDB(DB db) {
		unregisterFromDB(db);

		System.out.println("Registering a visitor: " + visitorName + " (host: "
				+ visitorHost + ", visitorHasBT: " + visitorHasBT
				+ ", visitorHasMainCamera: " + visitorHasMainCamera
				+ ", visitorHasFaceCamera: " + visitorHasFaceCamera + ", age: "
				+ visitorAge + ", last known position: "
				+ visitorLastKnownPosition + ", screen width: "
				+ visitorScreenDimensions[0] + ", screen height: "
				+ visitorScreenDimensions[1] + ")");

		try {
			db.stmt
					.executeUpdate("INSERT INTO visitors (host, name, hasBT, hasFaceCamera, hasMainCamera, age, lastKnownPosition, screenWidth, screenHeight) VALUES ('"
							+ visitorHost
							+ "', '"
							+ visitorName
							+ "', "
							+ visitorHasBT
							+ ", "
							+ visitorHasFaceCamera
							+ ", "
							+ visitorHasMainCamera
							+ ", "
							+ visitorAge
							+ ", '"
							+ visitorLastKnownPosition
							+ "', "
							+ visitorScreenDimensions[0]
							+ ", "
							+ visitorScreenDimensions[1] + ")");

			try {
				if (visitorPhoto != null) {
					String query = ("UPDATE visitors SET photoData=? WHERE host='"
							+ visitorHost + "'");
					PreparedStatement pstmt = db.con.prepareStatement(query);

					String url = Packages.outputDir + "photos/" + System.currentTimeMillis() + ".png";

					ImageIO.write(visitorPhoto, "PNG", new File(url));

					File file = new File(url);
					FileInputStream fis = new FileInputStream(file);
					int len = (int) file.length();

					pstmt.setBinaryStream(1, fis, len);
					pstmt.executeUpdate();
					
					file.delete();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void updateVisitorInDB(DB db) {
		System.out.println("Updating visitor: " + visitorHost + " (name: "
				+ visitorName + ", visitorHasBT: " + visitorHasBT
				+ ", visitorHasMainCamera: " + visitorHasMainCamera
				+ ", visitorHasFaceCamera: " + visitorHasFaceCamera + ", age "
				+ visitorAge + ", last known position: "
				+ visitorLastKnownPosition + ", screen width: "
				+ visitorScreenDimensions[0] + ", screen height: "
				+ visitorScreenDimensions[1] + ")");

		try {

			db.stmt.executeUpdate("UPDATE visitors SET name='" + visitorName
					+ "', hasBT=" + visitorHasBT + ", hasFaceCamera="
					+ visitorHasFaceCamera + ", hasMainCamera="
					+ visitorHasMainCamera + ", age=" + visitorAge
					+ ", lastKnownPosition='" + visitorLastKnownPosition
					+ "', screenWidth=" + visitorScreenDimensions[0] + ", screenHeight="
					+ visitorScreenDimensions[1] + " WHERE host='"
					+ visitorHost + "'");

			try {
				if (visitorPhoto != null) {
					String query = ("UPDATE visitors SET photoData=? WHERE host='"
							+ visitorHost + "'");
					PreparedStatement pstmt = db.con.prepareStatement(query);

					String url = System.currentTimeMillis() + ".png";

					ImageIO.write(visitorPhoto, "PNG", new File(url));

					File file = new File(url);
					FileInputStream fis = new FileInputStream(file);
					int len = (int) file.length();

					pstmt.setBinaryStream(1, fis, len);
					pstmt.executeUpdate();
					
					file.delete();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			db.stmt
					.executeUpdate("DELETE FROM completeexperiences WHERE visitorHost='"
							+ visitorHost + "'");

			if (completeexperiences != null) {
				Iterator i = completeexperiences.iterator();
				while (i.hasNext()) {
					String instanceName = (String) i.next();
					db.stmt
							.executeUpdate("INSERT INTO completeexperiences (visitorHost, instanceName) VALUES ('"
									+ visitorHost + "', '" + instanceName + "')");
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
