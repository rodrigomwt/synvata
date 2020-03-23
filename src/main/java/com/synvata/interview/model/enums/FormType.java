package com.synvata.interview.model.enums;

public enum FormType {
	Q("10-Q"),
	K("10-K");

	private String type;

	FormType(String type){
		this.type = type;
	}

	public String getType(){
		return type;
	}

	public static FormType fromType(String type) {
		if (type != null) {
			for (FormType b : FormType.values()) {
				if (type.equalsIgnoreCase(b.type)) {
					return b;
				}
			}
		}
		return null;
	}
}
