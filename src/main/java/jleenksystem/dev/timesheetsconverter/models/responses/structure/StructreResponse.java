package jleenksystem.dev.timesheetsconverter.models.responses.structure;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StructreResponse {

	private StrutureStatus status;
	private String message;
}
