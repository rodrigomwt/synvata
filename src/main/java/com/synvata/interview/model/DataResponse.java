package com.synvata.interview.model;

import java.time.LocalDate;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.synvata.interview.model.enums.FormType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class DataResponse {

	FormType formType;
	LocalDate dateField;
	String fileUrl;
	String reportingQuarter;
	String errorReason;
	//TODO dataPoints

	public DataResponse(String error) {
		this.errorReason = error;
	}

}
