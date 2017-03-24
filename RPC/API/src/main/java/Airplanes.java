/**
 * 
 */


import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

/**
 * @author blake
 *
 */
public class Airplanes extends ArrayList<Airplane> {
	private static final long serialVersionUID = 1L;

	public boolean addAll (String xmlAirplanes) throws NullPointerException {

		boolean collectionUpdated = false;

		// Load the XML string into a DOM tree for ease of processing
		// then iterate over all nodes adding each airport to our collection
		Document docAirplanes = buildDomDoc (xmlAirplanes);
		NodeList nodesAirplanes = docAirplanes.getElementsByTagName("Airplane");

		for (int i = 0; i < nodesAirplanes.getLength(); i++) {
			Element elementAirplane = (Element) nodesAirplanes.item(i);
			Airplane airplane = buildAirplane (elementAirplane);

			if (airplane.isValid()) {
				this.add(airplane);
				collectionUpdated = true;
			}
		}

		return collectionUpdated;
	}

	/**
	 * Builds a DOM tree form an XML string
	 *
	 * Parses the XML file and returns a DOM tree that can be processed
	 *
	 * @param xmlString XML String containing set of objects
	 * @return DOM tree from parsed XML or null if exception is caught
	 */
	private Document buildDomDoc (String xmlString) {
		/**
		 * load the xml string into a DOM document and return the Document
		 */
		try {
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			InputSource inputSource = new InputSource();
			inputSource.setCharacterStream(new StringReader(xmlString));

			return docBuilder.parse(inputSource);
		}
		catch (ParserConfigurationException e) {
			e.printStackTrace();
			return null;
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		catch (SAXException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Creates an Airplane object form a DOM node
	 *
	 * Processes a DOM Node that describes an Airport and creates an Airport object from the information
	 * @param nodeAirplane is a DOM Node describing an Airplane
	 * @return Airplane object created from the DOM Node representation of the Airport
	 *
	 * @preconditions nodeAirplane is of format specified by CS509 server API
	 */
	private Airplane buildAirplane (Node nodeAirplane) {
		/**
		 * Instantiate an empty Airport object
		 */
		Airplane airplane = new Airplane();

		String manufacturer;
		String model;
		int seatFirstClass;
		int seatCoach;

		// The airport element has attributes of Name and 3 character airport code
		Element elementAirplane = (Element) nodeAirplane;
		manufacturer = elementAirplane.getAttributeNode("Manufacturer").getValue();
		model = elementAirplane.getAttributeNode("Model").getValue();

		// The latitude and longitude are child elements
		Element elementSeat;
		elementSeat = (Element)elementAirplane.getElementsByTagName("FirstClassSeats").item(0);
		seatFirstClass = Integer.parseInt(getCharacterDataFromElement(elementSeat));

		elementSeat = (Element)elementAirplane.getElementsByTagName("CoachSeats").item(0);
		seatCoach = Integer.parseInt(getCharacterDataFromElement(elementSeat));

		/**
		 * Update the Airport object with values from XML node
		 */
		airplane.manufacturer(manufacturer);
		airplane.model(model);
		airplane.firstClassSeats(seatFirstClass);
		airplane.coachSeats(seatCoach);

		return airplane;
	}

	/**
	 * Retrieve character data from an element if it exists
	 *
	 * @param e is the DOM Element to retrieve character data from
	 * @return the character data as String [possibly empty String]
	 */
	private static String getCharacterDataFromElement (Element e) {
		Node child = e.getFirstChild();
		if (child instanceof CharacterData) {
			CharacterData cd = (CharacterData) child;
			return cd.getData();
		}
		return "";
	}

}
