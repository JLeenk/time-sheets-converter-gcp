package jleenksystem.dev.timesheetsconverter.models.responses.structure;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StructreFolderIdResponse {
	
	private StrutureStatus status;
	private String rootFolderId;
}
