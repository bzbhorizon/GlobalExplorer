package bzb.se.macroenvironment;

import java.sql.ResultSet;
import java.sql.SQLException;

import bzb.se.DB;
import bzb.se.exceptions.NoSuchPointOfInterestException;

public class NonInstallation extends PointOfInterest {

	public NonInstallation(String pointId, int[] position, String photoURL) {
		super(pointId, position, photoURL);
	}
	
	public void updatePOIInDB (DB db) {
		super.updatePOIInDB(db);
		
		System.out.println("Adding " + getPointId() + " to nonInstallation list");
		try {
			ResultSet rs = db.stmt
				.executeQuery("SELECT * FROM noninstallations WHERE poiId='"
					+ getPointId() + "'");
			if (rs.next()) {
				System.out.println("Unexpected: already a nonInstallation with this poiId; replacing");
				db.stmt.executeUpdate("DELETE FROM noninstallations WHERE poiId='"
					+ getPointId() + "'");
			}
			
			db.stmt
					.executeUpdate("INSERT INTO noninstallations (poiId) VALUES ('"
							+ getPointId()
							+ "')");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void removeFromDB (DB db) throws NoSuchPointOfInterestException {
		super.removeFromDB(db);
		
		System.out.println("Removing nonInstallation " + getPointId());

		try {
			ResultSet rs = db.stmt
				.executeQuery("SELECT * FROM noninstallations WHERE poiId='"
					+ getPointId() + "'");
			if (rs.next()) {
				db.stmt.executeUpdate("DELETE FROM noninstallations WHERE poiId='"
					+ getPointId() + "'");
			} else {
				System.out.println("Unexpected exception: no such nonInstallation found; ignoring");
				throw (new NoSuchPointOfInterestException());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
