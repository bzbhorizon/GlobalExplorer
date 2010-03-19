package bzb.se.adverts;

import java.sql.ResultSet;
import java.sql.SQLException;

import bzb.se.DB;

public class POIAdvert extends Advert {

	public POIAdvert(String poiId, DB db) {
		super(poiId, false, true, getVisited(poiId, db), false,
				true, "");
	}
	
	public int getPriority () {
		if (hasAlreadyBeenVisited()) {
			return Advert.Priorities.VISITED_NONINSTALLATION;
		} else {
			return Advert.Priorities.NONINSTALLATION;
		}
	}
	
	private static boolean getVisited (String poiId, DB db) {
		ResultSet rs;
		try {
			rs = db.stmt
				.executeQuery("SELECT visitorHost FROM completeexperiences WHERE instanceName='"
					+ poiId + "'");
		
			if (rs.next()) {
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

}
