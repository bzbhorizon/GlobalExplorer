package bzb.se.macroenvironment;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.imageio.ImageIO;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import bzb.se.DB;
import bzb.se.exceptions.NoSuchPointOfInterestException;
import bzb.se.macroenvironment.dijk.DenseRoutesMap;
import bzb.se.macroenvironment.dijk.DijkstraEngine;
import bzb.se.meta.Packages;
import bzb.se.utility.XMLProcessor;


public class EnvironmentMap {

	private Hashtable pois;
	
	private Vector nonInstallations;

	private Vector routes;

	public EnvironmentMap(DB db) {
		pois = new Hashtable();
		nonInstallations = new Vector();
		routes = new Vector();
		
		new EnvironmentXMLProcessor(db);

		System.out.println("Loaded " + pois.size() + " photo identifiers and "
				+ routes.size() + " routes");
	}

	public Hashtable getPOIsTable() {
		return pois;
	}

	public Vector getRoutes() {
		return routes;
	}

	// using Dijkstra shortest path algorithm from renaud.waldura.com

	public Vector getDirections(String currentId, String destinationId, DB db)
			throws NoSuchPointOfInterestException {
		System.out.println("Map POIs to useful indexes for Dijkstra");
		Hashtable dijkstraMapping = new Hashtable();
		Enumeration e = getPOIsTable().keys();
		int i = 0;
		while (e.hasMoreElements()) {
			String installationName = (String) e.nextElement();
			dijkstraMapping.put(installationName, new Integer(i));
			i++;
		}
		
		System.out.println("Create route map");
		DenseRoutesMap r = new DenseRoutesMap(dijkstraMapping);

		System.out.println("Fill route map");
		Iterator i1 = getRoutes().iterator();
		while (i1.hasNext()) {
			String[] route = (String[]) i1.next();
			String toPOIId = route[0];
			String fromPOIId = route[1];

			PointOfInterest toPOI = (PointOfInterest) getPOIsTable().get(
					toPOIId);
			PointOfInterest fromPOI = (PointOfInterest) getPOIsTable().get(
					fromPOIId);

			int[] toPosition = toPOI.getPosition();
			int[] fromPosition = fromPOI.getPosition();
			int distance = (int) Math.sqrt(Math.pow(
					(toPosition[0] - fromPosition[0]), 2)
					+ Math.pow((toPosition[1] - fromPosition[1]), 2));

			//r.addDirectRoute(toPOIId, fromPOIId, distance);
			r.addDirectRoute(fromPOIId, toPOIId, distance);
		}
		
		System.out.println("Start engine");
		DijkstraEngine d = new DijkstraEngine(r);

		System.out.println("Plot route from " + currentId + " to " + destinationId);
		d.execute(currentId, destinationId);
		System.out.println(d.getShortestDistance(destinationId));

		System.out.println("Get photo steps");
		Vector steps = new Vector();
		for (String poiId = destinationId; poiId != null; poiId = d
				.getPredecessor(poiId)) {
			PointOfInterest stepPOI = (PointOfInterest) pois.get(poiId);
			try {
				Vector step = new Vector();
				step.addElement(poiId);
				step.addElement(ImageIO.read(new File(Packages.resourceDir + stepPOI
						.getPhotoURL())));
				steps.add(step);
				System.out.println(poiId + " step added");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

		System.out.println("Reversing steps");
		Collections.reverse(steps);

		System.out.println("Steps done");
		return steps;
	}

	public static PointOfInterest getPointOfInterestByPointId(String pointId,
			DB db) throws NoSuchPointOfInterestException {
		PointOfInterest foundPOI = null;
		try {
			ResultSet rs = db.stmt
					.executeQuery("SELECT * FROM environmentmap WHERE pointId='"
							+ pointId + "'");

			if (rs.next()) {
				foundPOI = new PointOfInterest(rs.getString("pointId"),
						new int[] { rs.getInt("x"), rs.getInt("y") }, rs
								.getString("photoURL"));
			} else {
				throw (new NoSuchPointOfInterestException());
			}
		} catch (SQLException e) {
			System.out.println("SQL failed (" + e.getMessage()
					+ "); so no such point of interest found");
			throw (new NoSuchPointOfInterestException());
		}
		return foundPOI;
	}

	public static PointOfInterest getPointOfInterestByPhotoURL(String photoURL,
			DB db) throws NoSuchPointOfInterestException {
		PointOfInterest foundPOI = null;
		try {
			ResultSet rs = db.stmt
					.executeQuery("SELECT * FROM environmentmap WHERE photoURL='"
							+ photoURL + "'");

			if (rs.next()) {
				foundPOI = new PointOfInterest(rs.getString("pointId"),
						new int[] { rs.getInt("x"), rs.getInt("y") }, rs
								.getString("photoURL"));
			} else {
				throw (new NoSuchPointOfInterestException());
			}
		} catch (SQLException e) {
			System.out.println("SQL failed (" + e.getMessage()
					+ "); so no such point of interest found");
			throw (new NoSuchPointOfInterestException());
		}
		return foundPOI;
	}

	public static PointOfInterest getPointOfInterestByPosition(int[] position,
			DB db) throws NoSuchPointOfInterestException {
		PointOfInterest foundPOI = null;
		try {
			ResultSet rs = db.stmt
					.executeQuery("SELECT * FROM environmentmap WHERE x="
							+ position[0] + " AND y=" + position[1]);

			if (rs.next()) {
				foundPOI = new PointOfInterest(rs.getString("pointId"),
						new int[] { rs.getInt("x"), rs.getInt("y") }, rs
								.getString("photoURL"));
			} else {
				throw (new NoSuchPointOfInterestException());
			}
		} catch (SQLException e) {
			System.out.println("SQL failed (" + e.getMessage()
					+ "); so no such point of interest found");
			throw (new NoSuchPointOfInterestException());
		}
		return foundPOI;
	}

	public static void removePOIFromDB(String pointId, DB db)
			throws NoSuchPointOfInterestException {
		System.out.println("Removing point of interest for " + pointId);

		try {
			ResultSet rs = db.stmt
					.executeQuery("SELECT * FROM environmentmap WHERE pointId='"
							+ pointId + "'");
			if (rs.next()) {
				db.stmt
						.executeUpdate("DELETE FROM environmentmap WHERE pointId='"
								+ pointId + "'");
			} else {
				System.out
						.println("Unexpected exception: no such point of interest found; ignoring");
				throw (new NoSuchPointOfInterestException());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public class EnvironmentXMLProcessor extends XMLProcessor {

		public EnvironmentXMLProcessor(DB db) {
			parseXML();

			updateInDB(db);
		}

		private void updateInDB(DB db) {
			try {
				db.stmt.executeUpdate("DELETE FROM environmentmap");
				Enumeration e = pois.elements();
				while (e.hasMoreElements()) {
					PointOfInterest p = (PointOfInterest) e.nextElement();
					p.updatePOIInDB(db);
				}
				
				db.stmt.executeUpdate("DELETE FROM noninstallations");
				Iterator i = nonInstallations.iterator();
				while (i.hasNext()) {
					NonInstallation n = (NonInstallation) i.next();
					n.updatePOIInDB(db);
				}

				db.stmt.executeUpdate("DELETE FROM routes");
				i = routes.iterator();
				while (i.hasNext()) {
					String[] route = (String[]) i.next();
					db.stmt.executeUpdate("INSERT INTO routes (a, b) VALUES ('"
							+ route[0] + "', '" + route[1] + "')");
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}

		}

		public void parseXML() {
			super.parseXML(Packages.pathwaysDataURL);
		}
		
		private static final String POITYPE_ENTRANCE = "entrance";
		
		private static final String POITYPE_INSTALLATION = "installation";
		
		private static final String POITYPE_NONINSTALLATION = "nonInstallation";

		private boolean inPoi = false;

		private boolean inId = false;

		private boolean inPosition = false;

		private boolean inX = false;

		private boolean inY = false;

		private boolean inPhoto = false;

		private boolean inRoute = false;

		private boolean inA = false;

		private boolean inB = false;

		private String pointId = null;

		private String photoURL = null;

		private int[] directionMatrixCoords = null;

		private String from = null;

		private String to = null;

		private boolean inPathways = false;
		
		private boolean inNonInstallation = false;
		
		private boolean isBi = false;

		public void startElement(String namespaceURI, String localName,
				String qualifiedName, Attributes atts) throws SAXException {
			if (localName.equals("poi")) {
				inPoi = true;
				
				if (atts.getValue("type") != null && atts.getValue("type").equals(EnvironmentXMLProcessor.POITYPE_NONINSTALLATION)) {
					inNonInstallation = true;
				}
			} else if (localName.equals("id")) {
				pointId = new String();
				inId = true;
			} else if (localName.equals("position")) {
				directionMatrixCoords = new int[2];
				inPosition = true;
			} else if (localName.equals("x")) {
				inX = true;
			} else if (localName.equals("y")) {
				inY = true;
			} else if (localName.equals("photo")) {
				photoURL = new String();
				inPhoto = true;
			} else if (localName.equals("route")) {
				inRoute = true;
				
				if (atts.getValue("bi") != null && atts.getValue("bi").equals("true")) {
					isBi = true;
				}
			} else if (localName.equals("a")) {
				from = new String();
				inA = true;
			} else if (localName.equals("b")) {
				to = new String();
				inB = true;
			} else if (localName.equals("pathways")) {
				inPathways = true;
			}
		}

		public void endElement(String namespaceURI, String localName,
				String qualifiedName) throws SAXException {
			if (localName.equals("poi")) {
				inPoi = false;

				if (inPathways) {
					pois.put(pointId, new Installation(pointId, directionMatrixCoords, photoURL));
					
					if (inNonInstallation) {
						nonInstallations.addElement(new NonInstallation(pointId, directionMatrixCoords, photoURL));
						inNonInstallation = false;
					}
				}

				photoURL = null;
				directionMatrixCoords = null;
			} else if (localName.equals("id")) {
				inId = false;
			} else if (localName.equals("position")) {
				inPosition = false;
			} else if (localName.equals("x")) {
				inX = false;
			} else if (localName.equals("y")) {
				inY = false;
			} else if (localName.equals("photo")) {
				inPhoto = false;
			} else if (localName.equals("route")) {
				inRoute = false;

				if (isBi) {
					routes.add(new String[] { to, from });
					isBi = false;
				}
				routes.add(new String[] { from, to });

				from = null;
				to = null;
			} else if (localName.equals("a")) {
				inA = false;
			} else if (localName.equals("b")) {
				inB = false;
			} else if (localName.equals("pathways")) {
				inPathways = false;
			}
		}

		public void characters(char[] ch, int start, int length)
				throws SAXException {
			if (inPathways) {
				if (inPoi) {
					if (inPosition) {
						if (inX) {
							String temp = new String();
							for (int i = start; i < start + length; i++) {
								temp += ch[i];
							}
							directionMatrixCoords[0] = Integer.parseInt(temp);
						} else if (inY) {
							String temp = new String();
							for (int i = start; i < start + length; i++) {
								temp += ch[i];
							}
							directionMatrixCoords[1] = Integer.parseInt(temp);
						}
					} else if (inId) {
						for (int i = start; i < start + length; i++) {
							pointId += ch[i];
						}
					} else if (inPhoto) {
						for (int i = start; i < start + length; i++) {
							photoURL += ch[i];
						}
					}
				} else if (inRoute) {
					if (inA) {
						for (int i = start; i < start + length; i++) {
							from += ch[i];
						}
					} else if (inB) {
						for (int i = start; i < start + length; i++) {
							to += ch[i];
						}
					}
				}
			}
		}

	}

}
