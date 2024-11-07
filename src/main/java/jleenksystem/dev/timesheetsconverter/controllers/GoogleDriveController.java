package jleenksystem.dev.timesheetsconverter.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jleenksystem.dev.timesheetsconverter.models.responses.structure.StructreFolderIdResponse;
import jleenksystem.dev.timesheetsconverter.models.responses.structure.StructreResponse;
import jleenksystem.dev.timesheetsconverter.models.responses.structure.StrutureStatus;
import jleenksystem.dev.timesheetsconverter.services.GoogleDriveServiceI;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class GoogleDriveController {

	private final GoogleDriveServiceI googleDriveService;

	@GetMapping("getTimeSheetTemplate")
	public ResponseEntity<Resource> getTimeSheetTemplate(String userId) {

		try {
			InputStream is = googleDriveService.downloadTimeSheetTemplate(userId);
			if (is == null) {
				return ResponseEntity.notFound().build();
			}

			Resource resource = new InputStreamResource(is);
			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Time Sheet Template\"")
					.body(resource);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PostMapping("/uploadTimeSheetTemplate")
	public ResponseEntity<String> uploadTimeSheetTemplate(MultipartFile file, String userId) {
		try (InputStream is = file.getInputStream()){

			if (file == null || (userId != null && userId.isBlank())) {
				return ResponseEntity.badRequest().build();
			}

			googleDriveService.uploadTimeSheetTemplate(userId, is);

			return ResponseEntity.ok().build();
		} catch (IOException | GeneralSecurityException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("File upload failed: " + e.getMessage());
		}
	}

	@PostMapping("/uploadTimeSheetFile")
	public ResponseEntity<String> uploadTimeSheetFile(MultipartFile file, String userId) {
		try {

			if (file == null || (userId != null && userId.isBlank())) {
				return ResponseEntity.badRequest().build();
			}

			googleDriveService.uploadTimeSheet(userId, file.getInputStream());

			return ResponseEntity.ok().build();
		} catch (IOException | GeneralSecurityException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("File upload failed: " + e.getMessage());
		}
	}

	@DeleteMapping("deleteAllTimeSheetFiles")
	public ResponseEntity<String> deleteAllTimeSheetFiles(String userId) {
		if (userId != null && userId.isBlank()) {
			return ResponseEntity.badRequest().build();
		}
		try {
			googleDriveService.deleteAllTimeSheets(userId);
			return ResponseEntity.ok().build();
		} catch (IOException | GeneralSecurityException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Files delete failed: " + e.getMessage());
		}
	}

	@PostMapping("createRootFolder")
	public ResponseEntity<StructreResponse> createRootFolder(String userId) {
		if (userId == null || userId != null && userId.isBlank()) {
			return ResponseEntity.badRequest().body(new StructreResponse(StrutureStatus.ERROR, ""));
		}
		
		try {
			googleDriveService.createStructure(userId);
			String rootFolderId = googleDriveService.getRootFolderId(userId);
			return ResponseEntity.ok(new StructreResponse(StrutureStatus.CREATED, rootFolderId));
		} catch (IOException | GeneralSecurityException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new StructreResponse(StrutureStatus.ERROR, ""));
		}
		
	}

	@DeleteMapping("deleteRootFolder")
	public ResponseEntity<StructreResponse> deleteRootFolder(String userId) {
		if (userId != null && userId.isBlank()) {
			return ResponseEntity.badRequest().body(new StructreResponse(StrutureStatus.ERROR, "Bad request"));
		}

		try {
			googleDriveService.deleteStructure(userId);
			return ResponseEntity.ok().body(new StructreResponse(StrutureStatus.ERROR, "Root folder deleted"));
		} catch (IOException | GeneralSecurityException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new StructreResponse(StrutureStatus.ERROR, "Folder delete failed: "));
		}
	}

	@PostMapping("uploadRepotTemplate")
	public ResponseEntity<String> uploadRepotTemplate(MultipartFile file, String userId) {
		try {

			if (file == null || userId == null || userId.isBlank()) {
				return ResponseEntity.badRequest().build();
			}

			googleDriveService.uploadReportTemplate(userId, file.getInputStream());
			
			return ResponseEntity.ok().build();
		} catch (IOException | GeneralSecurityException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("File upload failed: " + e.getMessage());
		}
	}
	
	@GetMapping("getReportTemplate")
	public ResponseEntity<Resource> getReportTemplate(String userId) {

		try {
			InputStream is = googleDriveService.downloadReportTemplate(userId);
			if (is == null) {
				return ResponseEntity.notFound().build();
			}

			Resource resource = new InputStreamResource(is);
			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Time Sheet Template\"")
					.body(resource);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	@PostMapping("generateReports")
	public ResponseEntity<String> generateReports(String userId) {
		try {

			if (userId == null || userId.isBlank()) {
				return ResponseEntity.badRequest().build();
			}
			
			if(googleDriveService.isGenerateReportBeasy()) {
				return ResponseEntity.status(HttpStatusCode.valueOf(503)).build();
			}
			
			googleDriveService.generateReports(userId);

			return ResponseEntity.ok().build();
		} catch (IOException | GeneralSecurityException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("File upload failed: " + e.getMessage());
		}
	}
	
	@GetMapping("getRootFolderId")
	public ResponseEntity<StructreFolderIdResponse> getRootFolderId(String userId) {
		if (userId != null && userId.isBlank()) {
			return ResponseEntity.badRequest().build();
		}
		
		String rootFolderId;
		try {
			rootFolderId = googleDriveService.getRootFolderId(userId);
			if(rootFolderId.isBlank()) {
				return ResponseEntity.ok(new StructreFolderIdResponse(StrutureStatus.DELETED, rootFolderId));
			}
			return ResponseEntity.ok(new StructreFolderIdResponse(StrutureStatus.CREATED, rootFolderId));
		} catch (IOException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new StructreFolderIdResponse(StrutureStatus.ERROR, null));
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(new StructreFolderIdResponse(StrutureStatus.ERROR, null));
		}
	}
	
	@GetMapping("checkReportsStatus")
	public ResponseEntity<String> checkStatus(String userId) {
		if (userId != null && userId.isBlank()) {
			return ResponseEntity.badRequest().build();
		}
		
		String status = googleDriveService.getGenerateReportsStatus(userId);
		
		if(status == null || status.isBlank()) {
			ResponseEntity.notFound();
		}
		
	    return ResponseEntity.ok(status);
	}
	
	@DeleteMapping("/deleteAllReportFiles")
	public ResponseEntity<String> deleteAllReportFiles(String userId) {
		if (userId != null && userId.isBlank()) {
			return ResponseEntity.badRequest().build();
		}

		try {
			googleDriveService.deleteAllReports(userId);
			return ResponseEntity.ok().build();
		} catch (IOException | GeneralSecurityException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Files delete failed: " + e.getMessage());
		}
	}
}
