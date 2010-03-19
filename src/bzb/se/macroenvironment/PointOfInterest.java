package bzb.se.macroenvironment;

import java.sql.ResultSet;
import java.sql.SQLException;

import bzb.se.DB;
import bzb.se.exceptions.NoSuchPointOfInterestException;

public class PointOfInterest {

	private String pointId;
	
	private int[] position;
	
	private String photoURL;
	
	public PointOfInterest (String pointId, int[] position, String photoURL) {
		this.pointId = pointId;
		this.position = position;
		this.photoURL = photoURL;
	}
	
	public void setPosition (int[] position) {
		this.position = position;
	}
	
	public void updatePOIInDB (DB db) {
		System.out.println("Placing point of interest for " + pointId + " with photo " + photoURL + " at ("
				+ position[0] + ", " + position[1] + ")");

		try {
			ResultSet rs = db.stmt
				.executeQuery("SELECT * FROM environmentmap WHERE pointId='"
					+ pointId + "'");
			if (rs.next()) {
				System.out.println("Unexpected: already a POI with this pointId; replacing");
				db.stmt.executeUpdate("DELETE FROM environmentmap WHERE pointId='"
					+ pointId + "'");
			}
			
			db.stmt
					.executeUpdate("INSERT INTO environmentmap (pointId, x, y, photoURL) VALUES ('"
							+ pointId
							+ "', "
							+ position[0]
							+ ", "
							+ position[1]
							+ ", '"
							+ photoURL
							+ "')");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void removeFromDB (DB db) throws NoSuchPointOfInterestException {
		System.out.println("Removing point of interest for " + pointId);

		try {
			ResultSet rs = db.stmt
				.executeQuery("SELECT * FROM environmentmap WHERE pointId='"
					+ pointId + "'");
			if (rs.next()) {
				db.stmt.executeUpdate("DELETE FROM environmentmap WHERE pointId='"
					+ pointId + "'");
			} else {
				System.out.println("Unexpected exception: no such point of interest found; ignoring");
				throw (new NoSuchPointOfInterestException());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public String getPointId () {
		return pointId;
	}
	
	public String getPhotoURL () {
		return photoURL;
	}
	
	public int[] getPosition () {
		return position;
	}
	
}
