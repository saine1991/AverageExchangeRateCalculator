package pl.parser.nbp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MainClass {

	public static void main(String[] args) throws Exception{
		String currencyCode = args[0];
		String beginDate = args[1];
		String endDate = args[2];
		
		List<String> listOfXmlsToParse = getListOfXmlBetweenDates(beginDate, endDate);
		List<Double> data = getExchangeRateFromXml(currencyCode, listOfXmlsToParse);
		
		double averageOfBuyingRate = data.get(0) / data.get(2);
		double averageOfSellingRate = data.get(1) / data.get(2);
		double standardDeviation = 0;
		double subtract = 0;
		double sumOfSquaredSubtract = 0;
		
		for (int i = 6 ; i < data.size(); i++){
			subtract = data.get(i) - averageOfSellingRate;
			sumOfSquaredSubtract += subtract*subtract;
		}
		standardDeviation = Math.sqrt(sumOfSquaredSubtract/data.get(2));
		
		System.out.format("%.4f%n", averageOfBuyingRate);
		System.out.format("%.4f%n", standardDeviation);
	}

	public static List<String> getListOfXmlBetweenDates(String dateBegin, String dateEnd) throws Exception {
		String refactoredDateBegin = dateBegin.substring(2).replaceAll("-", "");
		String refactoredDateEnd = dateEnd.substring(2).replaceAll("-", "");
		String year = refactoredDateBegin.substring(0, 2);
		List<String> listaXml = new ArrayList<>();
		URL url;

		if (!year.equals("17"))
			url = new URL("http://nbp.pl/kursy/xml/dir" + "20" + year + ".txt");
		else
			url = new URL("http://nbp.pl/kursy/xml/dir.txt");

		BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));

		String nextLine;
		boolean done = true;
		while ((nextLine = br.readLine()) != null && done) {
			if (nextLine.contains(refactoredDateBegin)) {
				while (done) {
					if (nextLine.contains("c")) {
						listaXml.add(nextLine);
					}
					nextLine = br.readLine();
					if (nextLine.contains(refactoredDateEnd)) {
						done = false;
						listaXml.add(nextLine);
					}
				}

			}
		}
		return listaXml;
	}

	public static List<Double> getExchangeRateFromXml(String currencyCode, List<String> listOfXmls)
			throws Exception {
		
		List<Double> lista = new ArrayList<>();
		double counter = 0;
		double purchasePrice = 0;
		double sellPrice = 0;
		double nextSellPrice = 0;
		lista.add(0, 0.0);
		lista.add(1, 0.0);
		lista.add(2, 0.0);
		for (String s : listOfXmls) {
			URL directory = new URL("http://www.nbp.pl/kursy/xml/" + s + ".xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(directory.openStream());
			doc.getDocumentElement().normalize();

			NodeList nList = doc.getElementsByTagName("kod_waluty");

			for (int i = 0; i < nList.getLength(); i++) {
				Node node = nList.item(i);
				if (node.getTextContent().equals(currencyCode)) {
					Node sibling = node.getNextSibling();
					while (!(sibling instanceof Element) && sibling != null) {
						sibling = sibling.getNextSibling();

						purchasePrice += Double.parseDouble(sibling.getTextContent().replaceAll(",", "."));

						Node secondSibling = sibling.getNextSibling();
						boolean nextSiblingFound = true;
						while (nextSiblingFound && sibling != null) {
							sibling = secondSibling.getNextSibling();

							sellPrice += Double.parseDouble(sibling.getTextContent().replaceAll(",", "."));
							nextSellPrice = Double.parseDouble(sibling.getTextContent().replaceAll(",", "."));

							counter++;
							lista.add(3, nextSellPrice);
							nextSiblingFound = false;
						}

					}

				}

			}
		}
		lista.add(0, purchasePrice);
		lista.add(1, sellPrice);
		lista.add(2, counter);

		return lista;
	}
}
