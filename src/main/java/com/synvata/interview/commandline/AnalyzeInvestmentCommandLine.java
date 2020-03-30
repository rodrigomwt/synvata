package com.synvata.interview.commandline;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.synvata.interview.model.DataResponse;
import com.synvata.interview.model.UsGaap;
import com.synvata.interview.model.Xbrl;
import com.synvata.interview.model.enums.FormType;

/**
 * 
 * @author Rodrigo Xavier
 *
 */

@ShellComponent
@ShellCommandGroup("Analyze Investment")
public class AnalyzeInvestmentCommandLine {

	private static final String BASE_URL = "https://www.sec.gov/Archives/";

	/**
	 * Search
	 * @param cik
	 * @param quarterIndex
	 * @param fields
	 * @return
	 */
	@ShellMethod("Search for CIK, Quarter Index, Fields. Ex: search 1000045 2019-02-14 Assets,Deposits,NoninterestIncome")
	public String search(@ShellOption(help = "CIK Number") String cik, 
			@ShellOption(help = "Quarter Index") String quarterIndex, 
			@ShellOption(help = "Fields") List<String> fields) {

		try {
			List<Xbrl> xbrlList = getXbrlList(cik, LocalDate.parse(quarterIndex));
			if(xbrlList.isEmpty()) {
				System.out.println("No results found");
			}

			List<DataResponse> dataResponse = new ArrayList<>();

			for(Xbrl xbrl : xbrlList) {
				List<UsGaap> usGaapList = getData(xbrl.getFileName(), fields);

				if(!usGaapList.isEmpty()) {
					dataResponse.add(new DataResponse(xbrl.getFormType(), xbrl.getDateField(), BASE_URL + xbrl.getFileName(), usGaapList));
				}
			}

			ObjectMapper mapper = new ObjectMapper();
			return mapper.writeValueAsString(dataResponse);

		} catch(Exception ex){
			System.out.println("Error - " + ex.getMessage());
		}

		return null;

	}

	/**
	 * 
	 * @param urn
	 * @param fields
	 * @return
	 * @throws IOException
	 */
	private List<UsGaap> getData(String urn, List<String> fields) throws IOException {

		URL url = new URL(BASE_URL + urn);
		InputStream inputStream = url.openStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

		String str;
		String xml = "";
		boolean isXml = false;
		boolean read = true;

		while(read){
			str = reader.readLine();

			if(!isXml && str.contains("<xbrli:xbrl")) {
				isXml = true;
			}

			if(isXml) {
				xml += str.trim();
			}

			if(str.contains("</xbrli:xbrl>")) {
				read = false;
			}
		}

		Document xmlDocument = convertStringToXMLDocument(xml);
		String documentPeriodEndDate = xmlDocument.getElementsByTagName("dei:DocumentPeriodEndDate").item(0).getTextContent();
		List<UsGaap> usGaap = new ArrayList<>();

		NodeList nodesUsGaap = null;
		for(String field : fields) {
			nodesUsGaap = xmlDocument.getElementsByTagName("us-gaap:" + field);


			NodeList nodesContext = xmlDocument.getElementsByTagName("xbrli:context");

			for (int i = 0; i < nodesUsGaap.getLength(); i++) {
				Element nodeUsGaap = (Element) nodesUsGaap.item(i);

				for (int a = 0; a < nodesContext.getLength(); a++) {
					Element nodeContext = (Element) nodesContext.item(a);

					if(nodeContext.getAttribute("id").equals(nodeUsGaap.getAttribute("contextRef")) 
							&& nodeContext.getElementsByTagName("xbrli:instant").item(0).getTextContent().equals(documentPeriodEndDate)) {

						usGaap.add(new UsGaap(nodeUsGaap.getTagName(), nodeUsGaap.getAttribute("id"), nodeUsGaap.getAttribute("contextRef"), nodeUsGaap.getAttribute("decimals"), 
								nodeUsGaap.getAttribute("unitRef"), documentPeriodEndDate));

					}
				}
			}
		}

		reader.close();
		return usGaap;

	}

	/**
	 * Get xbrl list
	 * @param cik
	 * @param date
	 * @return
	 * @throws IOException
	 */
	private List<Xbrl> getXbrlList(String cik, LocalDate date) throws IOException {

		URL url = new URL(BASE_URL + "edgar/full-index/" + date.getYear() + "/"+ getQtr(date) +"/xbrl.idx");
		InputStream inputStream = url.openStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

		List<Xbrl> xbrl = new ArrayList<>();
		String str;
		int line = 0;

		while((str = reader.readLine())!= null){
			if(line > 9) {
				String[] ln = str.split("\\|");

				if(FormType.fromType(ln[2]) != null 
						&& ln[0].equals(cik)
						&& (LocalDate.parse(ln[3]).equals(date) || LocalDate.parse(ln[3]).isAfter(date))) {
					xbrl.add(new Xbrl(ln[0], ln[1], FormType.fromType(ln[2]), LocalDate.parse(ln[3]), ln[4]));
				}
			}
			line ++;
		}

		reader.close();
		return xbrl;	

	}

	/**
	 * Convert String To XML Document
	 * @param xmlString
	 * @return
	 */
	private static Document convertStringToXMLDocument(String xmlString) {

		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new InputSource(new StringReader(xmlString)));

			return doc;
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}

		return null;

	}

	/**
	 * Get quarter index
	 * @param date
	 * @return
	 */
	private String getQtr(LocalDate date) {
		String qtr = "QTR";
		switch(date.getMonth()) {
		case JANUARY:
		case FEBRUARY:
		case MARCH:
			qtr += 1; 
			break;
		case APRIL:
		case MAY:
		case JUNE:
			qtr += 2; 
			break;
		case JULY:
		case AUGUST:
		case SEPTEMBER:
			qtr += 3; 
			break;
		case OCTOBER:
		case NOVEMBER:
		case DECEMBER:
			qtr += 4; 
			break;
		}

		return qtr;
	}
}
