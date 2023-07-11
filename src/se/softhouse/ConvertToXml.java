package se.softhouse;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ConvertToXml {

	public static Element parsePerson(List<String> personData, Document doc) {
		Element person = doc.createElement("person");
		Element family = null;
		Iterator<String> pdI = personData.iterator();
		while (pdI.hasNext()) {
			String data = pdI.next();
			String[] info = data.split("\\|");
			if(data.charAt(0)=='P') {
				String[] names = {"firstname","lastname"};
				for(int i = 1; i<info.length;i++) {
					Element ele = createElement(names[i-1], info[i], doc);
					person.appendChild(ele);
				}
			} else if(data.charAt(0)=='T') {
				Element phone = doc.createElement("phone");
				String[] names = {"mobile","stationary"};
				for(int i = 1; i<info.length;i++) {
					Element ele = createElement(names[i-1], info[i], doc);
					phone.appendChild(ele);
				}
				
				if(family==null) {
					person.appendChild(phone);
				} else {
					family.appendChild(phone);
				}
			} else if(data.charAt(0)=='A') {
				Element address = doc.createElement("address");
				String[] names = {"street","city","postal"};
				for(int i = 1; i<info.length;i++) {
					Element ele = createElement(names[i-1], info[i], doc);
					address.appendChild(ele);
				}
				
				if(family==null) {
					person.appendChild(address);
				} else {
					family.appendChild(address);
				}
			} else if(data.charAt(0)=='F') {
				if(!(family==null)) {
					person.appendChild(family);
				}
				family = doc.createElement("family");
				String[] names = {"name","born"};
				for(int i = 1; i<info.length;i++) {
					Element ele = createElement(names[i-1], info[i], doc);
					family.appendChild(ele);
				}
			}
		}
		if(!(family==null)) {
			person.appendChild(family);
		}
		return person;
	}
	
	private static Element createElement(String name, String data, Document doc) {
		Element ele = doc.createElement(name);
		ele.appendChild(doc.createTextNode(data));
		return ele;
	}

	public static void main(String[] args) {
		try {
			File legacyFile = new File(args[0]);
			Scanner legacyReader = new Scanner(legacyFile);
			List<String> person = new ArrayList<String>();

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.newDocument();

			Element rootElement = doc.createElement("people");
			doc.appendChild(rootElement);

			while (legacyReader.hasNextLine()) {
				String data = legacyReader.nextLine();
				if (data.charAt(0) == 'P') {
					if (!person.isEmpty()) {
						// Parse person into XML
						Element personXML = ConvertToXml.parsePerson(person, doc);
						rootElement.appendChild(personXML);
						person.clear();
					}
				}
				person.add(data);
			}
			if (!person.isEmpty()) {
				// Parse person into XML
				Element personXML = ConvertToXml.parsePerson(person, doc);
				rootElement.appendChild(personXML);
			}
			legacyReader.close();

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);

			// Output to console for testing
			StreamResult consoleResult = new StreamResult(System.out);
			transformer.transform(source, consoleResult);
			
			if(args.length>1) {
				FileWriter writer = new FileWriter(new File(args[1]));
				StreamResult result = new StreamResult(writer);
				transformer.transform(source, result);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
