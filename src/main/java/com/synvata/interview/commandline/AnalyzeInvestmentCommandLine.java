package com.synvata.interview.commandline;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import com.synvata.interview.model.DataResponse;
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
	@ShellMethod("Search for CIK, Quarter Index, Fields. Ex: search 1000045 2019-02-14 Assets,Deposits")
	public DataResponse search(@ShellOption(help = "CIK Number") String cik, 
			@ShellOption(help = "Quarter Index") String quarterIndex, 
			@ShellOption(help = "Fields") List<String> fields) {

		try {
			List<Xbrl> xbrlList = getXbrlList(cik, LocalDate.parse(quarterIndex));
			if(xbrlList.isEmpty()) {
				return new DataResponse("No results found");
			}

			for(Xbrl xbrl : xbrlList) {
				getData(xbrl.getFileName(), fields);
				//TODO get data and mount return object
			}

			return new DataResponse();
		} catch(Exception ex){
			return new DataResponse("Error - " + ex.getMessage());
		}

	}

	/**
	 * 
	 * @param urn
	 * @param fields
	 * @return
	 * @throws IOException
	 */
	private void getData(String urn, List<String> fields) throws IOException {

		URL url = new URL(BASE_URL + urn);
		InputStream inputStream = url.openStream();
		InputStreamReader isReader = new InputStreamReader(inputStream);

		BufferedReader reader = new BufferedReader(isReader);

		//TODO get xml file
		//TODO filter data points

		reader.close();

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
		InputStreamReader isReader = new InputStreamReader(inputStream);

		BufferedReader reader = new BufferedReader(isReader);

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
