package bzb.se.installations;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.*;

import org.apache.http.client.*;
import java.io.*;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.*;
import org.apache.http.*;
import org.apache.http.conn.params.*;


import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import bzb.se.DB;
import bzb.se.exceptions.InstanceNotOwnedException;
import bzb.se.exceptions.NoSuchVisitorException;
import bzb.se.ge.ClassFactory;
import bzb.se.ge.IApplicationGE;
import bzb.se.ge.AltitudeModeGE;
import bzb.se.messaging.Message;
import bzb.se.meta.Installations;
import bzb.se.meta.Packages;
import bzb.se.meta.Requirements;
import bzb.se.utility.XMLProcessor;
import bzb.se.visitors.Visitor;

import java.net.URI;
import java.net.URISyntaxException;
import com.chilkatsoft.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException; 
import org.w3c.dom.Document;
import org.w3c.dom.*;

import java.awt.*;

import net.roarsoftware.lastfm.*;
import java.net.*;

public class GoogleEarth extends Multi {

	static {
    	try {
        	System.loadLibrary("chilkat");
    	} catch (UnsatisfiedLinkError e) {
      		System.err.println("Native code library failed to load.\n" + e);
      		System.exit(1);
    	}
  	}
	
	public GoogleEarth() {
		super(Installations.BALL_ID,
				1,
				Requirements.NOT_REQUIRED,
				Requirements.NOT_REQUIRED,
				Requirements.NOT_REQUIRED,
				//new String[] {Installations.WORLDMAP_ID},
				null,
				Installations.BALL_DESC);
		
		new Thread(new Runnable() {
			public void run () {
				ge = ClassFactory.createApplicationGE();
				ge.openKmlFile("C:/demo/build/res/controls.kmz", 0);
				ge.openKmlFile("C:/demo/build/res/crosshairs.kmz", 0);
				resetGoogleEarth(true);

				explorer = Desktop.getDesktop();
				player = new GeoMusicPlayer();
			}
		}).start();
	}
	
	private IApplicationGE ge;
	
	private final double gyNott = 52.953469;
	private final double gxNott = -1.187330;
	private final double altNott = 4000100.0;
	
	private double gy = gyNott; //180 -> -180
	private double gx = gxNott; //90 -> -90
	private double alt = altNott;
	
	private final double baseSpeed = 0.0050;
	private final double maxAlt = 20000000.0;
	
	private long lastUpdate = 0;

	private GeoMusicPlayer player;
	private Desktop explorer;
	
	public void updatePosition (Vector accelData) {
		double dx = Double.parseDouble((String) accelData.get(0)) / 60;
		double dy = Double.parseDouble((String) accelData.get(1)) / 60 * -1;

		double delay = 0;
		if (lastUpdate == 0) {
			delay = 100;
		} else {
			delay = System.currentTimeMillis() - lastUpdate;
		}
		if (delay > 2000) {
			delay = 2000;
		}
		lastUpdate = System.currentTimeMillis();
		
		if (!(dx < 0.15 && dx > -0.15) ||
				!(dy < 0.15 && dy > -0.15)) {
			dx = delay * baseSpeed * alt/altNott * dx;
			dy = delay * baseSpeed * alt/altNott * dy;
			
			gx += dx;
			if (gx > 180) {
				gx -= 360;
			} else if (gx < -180) {
				gx += 360;
			}
			
			gy += dy;
			if (gy > 90) {
				gy = 180 - gy;
			} else if (gy < -90) {
				gy = -180 - gy;
			}
		}
	}
	
	public void zoomIn () {
		alt /= 1.5;
		if (alt < 2) {
			alt = 2;
		} else {
			updateGoogleEarth();
		}
	}
	
	public void zoomOut () {
		alt *= 1.5;
		if (alt > maxAlt) {
			alt = maxAlt;
		} else {
			updateGoogleEarth();
		}
	}

	public void moveLeft () {
		Vector coords = new Vector();
		coords.add("-60");
		coords.add("0");
		updatePosition(coords);
		updateGoogleEarth();
	}

	public void moveRight () {
		Vector coords = new Vector();
		coords.add("60");
		coords.add("0");
		updatePosition(coords);
		updateGoogleEarth();
	}

	public void moveUp () {
		Vector coords = new Vector();
		coords.add("0");
		coords.add("-60");
		updatePosition(coords);
		updateGoogleEarth();
	}

	public void moveDown () {
		Vector coords = new Vector();
		coords.add("0");
		coords.add("60");
		updatePosition(coords);
		updateGoogleEarth();
	}
	
	public void updateGoogleEarth () {
		if (ge != null) {
			try {
				ge.setCameraParams(gy, gx, 300.0, AltitudeModeGE.RelativeToGroundAltitudeGE, alt, 60.0, 0, 5);
			} catch (Exception e) {
				//System.out.println("update " + gy + " " + gx + " " + alt);
			}
		}
	}
	
	private String kmlFile = "C:/demo/build/output/ge.kml";

	public void resetGoogleEarth (boolean clearKeys) {
		gy = gyNott;
		gx = gxNott;
		alt = altNott;
		
		try {
			ge.setCameraParams(gy, gx, 5000.0, AltitudeModeGE.RelativeToGroundAltitudeGE, alt, 60.0, 0, 0.2);
		} catch (Exception e) {
			//System.out.println("reset " + gy + " " + gx + " " + alt);
		}
		
		if (ge != null) {
			if (clearKeys) {
				String kml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<kml xmlns=\"http://www.opengis.net/kml/2.2\"></kml>";
				
				try {
					Writer output = null;
				    File file = new File(kmlFile);
				    output = new BufferedWriter(new FileWriter(file));
				    output.write(kml);
				    output.close();
					
					ge.openKmlFile(kmlFile, 0);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}

		if (explorer != null) {
			//showHTML("<p style=\"font-family: arial; font-size: 100pt; font-weight: bold;\">Earth reset</p>");
			//explorer.stop();
		}
	}
	
	private RepositoryXMLProcessor parser;
	private Hashtable keys;
	
	private static final String THUMB_FILENAME = "/thumb.png";
	
	public void displayKeys (Visitor visitor, Vector foundKeys) {
		keys = new Hashtable();
		parser = new RepositoryXMLProcessor(db);
		
		String kml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
		"<kml xmlns=\"http://www.opengis.net/kml/2.2\"><Document>";

		//if (foundKeys.size() > 0) {
			kml += "<Folder>" +
		    "<name>Keys already found</name>" +
		    "<description>Flags of keys found by visitor, overlaid on relevant locations</description>";

			if (ge != null && foundKeys != null && foundKeys.size() > 0) {
				Enumeration e = foundKeys.elements();
				while (e.hasMoreElements()) {
					Integer key = (Integer) e.nextElement();
					int currentCode = key.intValue();

					Hashtable foundKey = (Hashtable) keys.get(key);
					double countryX = ((Double) foundKey.get("countryX")).doubleValue();
					double countryY = ((Double) foundKey.get("countryY")).doubleValue();

					String iconURL = Packages.contentDir + currentCode + THUMB_FILENAME;

					kml += "<GroundOverlay>" +
					      "<name>" + foundKey.get("countryName") + "</name>" +
					      "<Icon>" +
					        "<href>C:/demo/" + iconURL + "</href>" +
					      "</Icon>" +
					      "<LatLonBox>" +
					        "<north>" + (countryY + 3) + "</north>" +
					        "<south>" + (countryY - 3) + "</south>" +
					        "<east>" + (countryX + 3) + "</east>" +
					        "<west>" + (countryX - 3) + "</west>" +
					        "<rotation>0</rotation>" +
					      "</LatLonBox>" +
					    "</GroundOverlay>";
					keys.remove(key);
				}

			}

			kml += "</Folder>";
		//}
		
		if (ge != null && keys != null && keys.size() > 0) {	
			kml += "<Folder>" +
		    "<name>Keys not yet found</name>" +
		    "<description>Locations of keys not found by visitor</description>";
		
			Enumeration e = keys.keys();
			while (e.hasMoreElements()) {
				Integer key = (Integer) e.nextElement();
				int currentCode = key.intValue();
				
				Hashtable unfoundKey = (Hashtable) keys.get(key);
				double countryX = ((Double) unfoundKey.get("countryX")).doubleValue();
				double countryY = ((Double) unfoundKey.get("countryY")).doubleValue();
				
				String iconURL = Packages.contentDir + currentCode + THUMB_FILENAME;
				
				kml += "<GroundOverlay>" +
				      "<name>" + unfoundKey.get("countryName") + "</name>" +
				      "<Icon>" +
				        "<href>C:/demo/build/res/notfound.png</href>" +
				      "</Icon>" +
				      "<LatLonBox>" +
				        "<north>" + (countryY + 3) + "</north>" +
				        "<south>" + (countryY - 3) + "</south>" +
				        "<east>" + (countryX + 3) + "</east>" +
				        "<west>" + (countryX - 3) + "</west>" +
				        "<rotation>0</rotation>" +
				      "</LatLonBox>" +
				    "</GroundOverlay>";
				
				keys.remove(key);
			}
			
			kml += "</Folder>";
		}
		kml += "</Document></kml>";
		
		try {
			Writer output = null;
		    File file = new File(kmlFile);
		    output = new BufferedWriter(new FileWriter(file));
		    output.write(kml);
		    output.close();
		    
			ge.openKmlFile(kmlFile, 0);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		parser = null;
	}

	public void showHTML (String html) {

		html = "<html><head><style>html, body {height: 100%; margin: 0; padding: 0;}"
			+ "body {display: table; height: 100%; width: 100%; text-align: center;}"
			+ "#wrapper {display: table-cell; vertical-align: middle;}"
			+ "#centered {width: 30em; margin: 0 auto;}"
			+ "/*]]>*/"
			+ "</style>"
			+ "<!--[if lte IE 7]>"
			+ "<style type=\"text/css\" media=\"all\">"
			+ "div#centered {margin-top: expression(((document.documentElement.offsetHeight/2)"
			+ "-(parseInt(offsetHeight)/2) -2) <0 ? \"0\" : "
			+ "(document.documentElement.offsetHeight/2)"
			+ "-(parseInt(offsetHeight)/2) -2 +'px') ;}"
			+ "</style><body><div id=\"wrapper\"><div id=\"centered\"><div id=\"middle\">"
			+ html + "</div></div></div></body></html>";


				try {
					Writer output = null;
				    File file = new File("C:/demo/build/output/temp.html");
				    output = new BufferedWriter(new FileWriter(file));
				    output.write(html);
				    output.close();
					
					explorer.browse(new URI("file:///C:/demo/build/output/temp.html"));
				} catch (Exception e1) {
					e1.printStackTrace();
				}
	}
	
	public void startRadio () {
		player.play();     	
	
		//showHTML("<p style=\"font-family: arial; font-size: 60pt;\">lat: " + ((int)Math.round(gy)) + "&deg;<br />long: " + ((int)Math.round(gx)) + "&deg;<br />alt: " + ((int)Math.round(alt)) + "m</p>");
	}
	
	public boolean handleMessage (Message receivedMessage) {
		boolean handled = super.handleMessage(receivedMessage);
		
		if (!handled) {
			handled = true;
			switch (receivedMessage.getMessageCode()) {
			// case 0: by multi
			// case 1: by multi
			default:
				handled  = false;
				break;
			}
		}
		return handled;
	}
	
	public class RepositoryXMLProcessor extends XMLProcessor {

		public RepositoryXMLProcessor(DB db) {
			parseXML();
		}
		
		public void parseXML () {
			super.parseXML(Packages.repositoryDataURL);
		}
		
		private boolean inRepository = false;

		private boolean inContent = false;
		
		private boolean inDesiredContent = false;
		
		private Integer thisCode;
		private Hashtable thisKey;
		
		public void startElement(String namespaceURI, String localName,
				String qualifiedName, Attributes atts) throws SAXException {
			if (localName.equals("repository")) {
				inRepository = true;
			} else if (localName.equals("content")) {
				inContent = true;
		
				if (atts.getValue("isSeed") != null && atts.getValue("isSeed").equals("true")) {
					inDesiredContent = true;
					
					thisKey = new Hashtable();
					
					if (atts.getValue("code") != null) {
						thisCode = Integer.valueOf(atts.getValue("code"));
					}
					
					if (atts.getValue("name") != null) {
						thisKey.put("countryName", atts.getValue("name"));
					}
					if (atts.getValue("x") != null) {
						thisKey.put("countryX", Double.valueOf(atts.getValue("x")));
					}
					if (atts.getValue("y") != null) {
						thisKey.put("countryY", Double.valueOf(atts.getValue("y")));
					}
					System.out.println(thisCode + " " + thisKey.get("countryName"));
					keys.put(thisCode, thisKey);
				}
			}
		}

		public void endElement(String namespaceURI, String localName,
				String qualifiedName) throws SAXException {
			if (localName.equals("repository")) {
				inRepository = false;
			} else if (localName.equals("content")) {
				inContent = false;
				
				if (inDesiredContent) {
					inDesiredContent = false;
				}
			}
		}

		public void characters(char[] ch, int start, int length)
				throws SAXException {}
		
	}

	public class GeoMusicPlayer {
	
	private String[] boringArtists;
	
	public GeoMusicPlayer () {
		
		lookupBoringArtists();
	}
	
	public void play () {
		new Thread(new Runnable () {
			public void run () {
			try {
				explorer.browse(new URI("file:///C:/demo/build/res/loading.html"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		String countryName = lookupCountryName(buildCountryNameRequest(gy, gx));
		if (countryName != null) {
			String artistName = "http://www.last.fm/music/" + chooseArtist(countryName) + "?autostart";
			if (artistName != null) {
				try {
					explorer.browse(new URI(artistName));
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				try {
					explorer.browse(new URI("file:///C:/demo/build/res/netproblem.html"));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else {
			try {
				explorer.browse(new URI("file:///C:/demo/build/res/netproblem.html"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

			}
		}).start();
	}
	
	public void stop () {
		if (explorer != null) {
			//explorer.stop();
			//explorer.quit();
	      		//explorer.release();
		}
	}
	
	private String buildCountryNameRequest (double lat, double lon) {
		return "//ws.geonames.org/countryCode?lat=" + lat + "&lng=" + lon + "&type=xml";
	}
	
	private String lookupCountryName (String countryNameRequest) {
		String html = webGet(countryNameRequest);
	
		CkXml xml = new CkXml();
    	boolean success = xml.LoadXml(html);
    	if (success != true) {
       		System.out.println(xml.lastErrorText());
       		return null;
    	} else {
    		xml.FirstChild2();
			return xml.getChildContent("countryName");
    	}
	}
		
	private void lookupBoringArtists () {
		String key = "01403f9714b074d60b300f1a6782a624";
		String user = "Growlingfish";
		Caller.getInstance().setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("128.243.20.248", 3128)));
		Caller.getInstance().setUserAgent("tst");
		Collection<Artist> artists = Geo.getTopArtists("United Kingdom", key);
		boringArtists = new String[artists.size()];
		int x = 0;
		for (Artist artist : artists) {
			boringArtists[x] = artist.getName();
			x++;
		}
		System.out.println("Boring artists built");
	}
	
	private String chooseArtist (String country) {
		String artistName = null;
		String key = "01403f9714b074d60b300f1a6782a624";
		String user = "Growlingfish";
		Caller.getInstance().setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("128.243.20.248", 3128)));
		Caller.getInstance().setUserAgent("tst");
		Collection<Artist> artists = Geo.getTopArtists(country, key);
		for (Artist artist : artists) {
			artistName = artist.getName();
			boolean found = false;
			for (int i = 0; i < boringArtists.length; i++) {
				if (artist.getName().equals(boringArtists[i])) {
					found = true;
					break;
				}
			}
			if (!found) {
				artistName = artist.getName();
				break;
			}
		}
    		System.out.println(artistName);
    		return URLEncoder.encode(artistName);
	}

	}

	public String webGet (String request) {

		String text = "";
		
		HttpClient httpclient = new DefaultHttpClient();
		httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, new HttpHost("128.243.20.248", 3128, "http"));




 
 // Prepare a request object
 HttpGet httpget = new HttpGet("http:" + request); 
 try {
 // Execute the request
 HttpResponse response = httpclient.execute(httpget);
 
 // Examine the response status
 System.out.println(response.getStatusLine());
 
 // Get hold of the response entity
 HttpEntity entity = response.getEntity();
 
 // If the response does not enclose an entity, there is no need
 // to worry about connection release
 if (entity != null) {
     InputStream instream = entity.getContent();
     try {
         
         BufferedReader reader = new BufferedReader(
                 new InputStreamReader(instream));
         // do something useful with the response
         String line = null;
            
            while ((line = reader.readLine()) != null) {
                //Process the data, here we just print it out
                System.out.println(line);
		text += line;
            }

         
     } catch (IOException ex) {
 
         // In case of an IOException the connection will be released
         // back to the connection manager automatically
         throw ex;
         
     } catch (RuntimeException ex) {
 
         // In case of an unexpected exception you may want to abort
         // the HTTP request in order to shut down the underlying 
         // connection and release it back to the connection manager.
         httpget.abort();
         throw ex;
         
     } finally {
 
         // Closing the input stream will trigger connection release
         instream.close();
         
     }
 }
} catch (Exception e) {
	e.printStackTrace();
}
		System.out.println(text);
		return text;
	}



}
