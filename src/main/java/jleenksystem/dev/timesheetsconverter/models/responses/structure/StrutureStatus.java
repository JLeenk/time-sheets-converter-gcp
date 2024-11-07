package jleenksystem.dev.timesheetsconverter.models.responses.structure;

import com.fasterxml.jackson.annotation.JsonValue;

public enum StrutureStatus {
	CREATED("Created"), DELETED("Deleted"), ERROR("Error");

	private final String status;

	private StrutureStatus(String status) {
		this.status = status;
	}

	@JsonValue
	public String getStructureStatus() {
		return status;
	}
}
