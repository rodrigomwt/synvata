package com.synvata.interview.model;

import java.time.LocalDate;

import com.synvata.interview.model.enums.FormType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Xbrl {
	String cik;
	String companyName;
	FormType formType;
	LocalDate dateField;
	String fileName;
}
