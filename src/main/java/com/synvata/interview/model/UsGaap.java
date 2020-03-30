package com.synvata.interview.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * @author Rodrigo Xavier
 *
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsGaap {
	
	String dataPoint;
	String id;
	String contextRef;
	String decimals;
	String unitRef;
	String reportingQuarter;
	
}
