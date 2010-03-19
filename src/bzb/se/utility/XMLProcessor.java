package bzb.se.utility;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public abstract class XMLProcessor extends DefaultHandler {

	// There's a name conflict with java.net.ContentHandler
	// so we have to use the fully package qualified name.
	public void parseXML(String fileURL) {
		// Read the response XML document
		XMLReader parser;
		try {
			parser = XMLReaderFactory
					.createXMLReader("org.apache.xerces.parsers.SAXParser");

			parser.setContentHandler(this);

			BufferedReader in = new BufferedReader(new FileReader(fileURL));

			InputSource source = new InputSource(in);
			parser.parse(source);

			in.close();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}
