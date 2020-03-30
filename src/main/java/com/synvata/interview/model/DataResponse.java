package com.synvata.interview.model;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.synvata.interview.model.enums.FormType;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DataResponse {

	FormType formType;
	String dateField;
	String fileUrl;
	String reportingQuarter;
	Map<String, Integer> dataPoints = new HashMap<String, Integer>();

	public DataResponse(FormType formType, LocalDate dateField, String fileUrl, List<UsGaap> usGaapList) {
		this.formType = formType;
		this.dateField = dateField.toString();
		this.fileUrl = fileUrl;
		this.reportingQuarter = usGaapList.get(0).getReportingQuarter();

		for(UsGaap usGaap : usGaapList) {

			Map<String, Integer> insertedDataPoints = this.dataPoints.entrySet().stream().filter(x -> x.getKey().equals(usGaap.getDataPoint())).collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));

			if(insertedDataPoints.isEmpty()) {
				insertedDataPoints.put(usGaap.getDataPoint(), 1);
			} else {
				insertedDataPoints.entrySet().stream().forEach(x -> {
					if(x.getKey().equals(usGaap.getDataPoint())) {
						int val = x.getValue() + 1;
						x.setValue(val);
					}
				});
			}

			this.dataPoints = insertedDataPoints;
		}
	}

}
