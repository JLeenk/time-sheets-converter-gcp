package jleenksystem.dev.timesheetsconverter.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import jleenksystem.dev.timesheetsconverter.models.reports.Report;
import jleenksystem.dev.timesheetsconverter.models.reports.TimeSheet;
import jleenksystem.dev.timesheetsconverter.utils.GoogleDriveMimeType;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GoogleDriveService implements GoogleDriveServiceI {

	private static final String ROOT_FOLDER = "Time Sheets Converter";
	private static final String TIME_SHEETS_FOLDER = "Time sheets";
	private static final String REPORTS_FOLDER = "Reports";
	private static final String TEMPLATES_FOLDER = "Templates";
	private static final String TIME_SHEET_TEMPLATE_NAME = "Time Sheet Template";
	private static final String REPORT_TEMPLATE_NAME = "Report Template";
	private static boolean isServerBusy = false;

	private final OAuthServiceI authServiceI;
	private final XlsxFileContentProcessorServiceI contentProcessorService;
	
	private static final ConcurrentMap<String, String> generateReportStatus = new ConcurrentHashMap<>();

	@Override
	public void createStructure(String userId) throws IOException, GeneralSecurityException {
		Drive driveService = authServiceI.getDriverService(userId);

		String rootFolderId = createFolder(driveService, ROOT_FOLDER, null);
		createFolder(driveService, TIME_SHEETS_FOLDER, rootFolderId);
		createFolder(driveService, REPORTS_FOLDER, rootFolderId);
		createFolder(driveService, TEMPLATES_FOLDER, rootFolderId);
	}

	@Override
	public void deleteStructure(String userId) throws IOException, GeneralSecurityException {
		Drive driveService = authServiceI.getDriverService(userId);

		String folderId = searchFolderByName(driveService, ROOT_FOLDER);

		if (folderId != null) {
			driveService.files().delete(folderId).execute();
		}
	}

	// Create a folder in Google Drive
	private static String createFolder(Drive driveService, String folderName, String parentFolderId)
			throws IOException {
		File fileMetadata = new File();
		fileMetadata.setName(folderName);
		fileMetadata.setMimeType("application/vnd.google-apps.folder");

		// Set the parent folder if provided
		if (parentFolderId != null) {
			fileMetadata.setParents(Collections.singletonList(parentFolderId));
		}

		// Create the folder
		File folder = driveService.files().create(fileMetadata).setFields("id").execute();

		return folder.getId();
	}

	// Method to search for a folder by its name
	public static String searchFolderByName(Drive driveService, String folderName) throws IOException {
		// Build the query to search for a folder with the given name
		String query = String.format("mimeType='%s' and name='%s' and trashed=false",
				GoogleDriveMimeType.GOOGLE_FOLDER.getMimeType(), folderName);

		// Execute the query
		FileList result = driveService.files().list().setQ(query).setFields("files(id, name)").execute();

		// Get the list of files (folders) from the result
		List<File> files = result.getFiles();

		// If the folder is found, return its ID
		if (files != null && !files.isEmpty()) {
			return files.get(0).getId(); // Return the ID of the first matching folder
		}

		return null; // Folder not found
	}

	// Method to find a folder by its name under a parent folder ID
	private static String findFolderIdByName(Drive service, String folderName, String parentId) throws IOException {
		String query = String.format("mimeType='%s' and name='%s' and '%s' in parents and trashed=false",
				GoogleDriveMimeType.GOOGLE_FOLDER.getMimeType(), folderName, parentId);
		FileList result = service.files().list().setQ(query).setSpaces("drive").setFields("files(id, name)").execute();

		List<File> folders = result.getFiles();
		if (folders == null || folders.isEmpty()) {
			throw new IOException("Folder not found: " + folderName);
		}
		return folders.get(0).getId(); // Return the first matching folder's ID
	}

	private static String findRootFolderId(Drive driveService) throws IOException {
		return findFolderIdByName(driveService, ROOT_FOLDER, "root");
	}

	// Method to find a file by its name under a specific folder
	private static File findFileByName(Drive service, String fileName, String parentId) throws IOException {
		String query = String.format("name='%s' and '%s' in parents and trashed=false", fileName, parentId);
		FileList result = service.files().list().setQ(query).setSpaces("drive").setFields("files(id, name)").execute();

		List<File> files = result.getFiles();
		if (files == null || files.isEmpty()) {
			throw new IOException("File not found: " + fileName);
		}
		return files.get(0); // Return the first matching file
	}

	@Override
	public InputStream downloadTimeSheetTemplate(String userId) throws IOException, GeneralSecurityException {
		Drive driveService = authServiceI.getDriverService(userId);
		String templatesFolderId = findFolderIdByName(driveService, TEMPLATES_FOLDER, findRootFolderId(driveService));

		String fileId = findFileByName(driveService, TIME_SHEET_TEMPLATE_NAME, templatesFolderId).getId();

		if (fileId == null) {
			return null;
		}

		return driveService.files().get(fileId).executeMedia().getContent();
	}

	@Override
	public InputStream downloadReportTemplate(String userId) throws IOException, GeneralSecurityException {
		Drive driveService = authServiceI.getDriverService(userId);
		String templatesFolderId = findFolderIdByName(driveService, TEMPLATES_FOLDER, findRootFolderId(driveService));

		String fileId = findFileByName(driveService, REPORT_TEMPLATE_NAME, templatesFolderId).getId();

		if (fileId == null) {
			return null;
		}

		return driveService.files().get(fileId).executeMedia().getContent();
	}

	public static void uploadFileToDrive(Drive service, GoogleDriveMimeType mimeType, String fileName, String folderId,
			ByteArrayOutputStream os) throws IOException, GeneralSecurityException {

		try (ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray())) {
			// File's metadata.
			File fileMetadata = new File();
			fileMetadata.setName(fileName);
			fileMetadata.setParents(Collections.singletonList(folderId)); // Specify the folder ID

			InputStreamContent mediaContent = new InputStreamContent(mimeType.getMimeType(), is);

			service.files().create(fileMetadata, mediaContent).setFields("id, parents").execute();
		}
	}

	public static void uploadFileToDrive(Drive service, GoogleDriveMimeType mimeType, String fileName, String folderId,
			InputStream is) throws IOException, GeneralSecurityException {

		// File's metadata.
		File fileMetadata = new File();
		fileMetadata.setName(fileName);
		fileMetadata.setParents(Collections.singletonList(folderId)); // Specify the folder ID

		InputStreamContent mediaContent = new InputStreamContent(mimeType.getMimeType(), is);

		service.files().create(fileMetadata, mediaContent).setFields("id, parents").execute();
	}

	private static String getTemplatesFolderId(Drive service) throws IOException {
		return findFolderIdByName(service, TEMPLATES_FOLDER, findRootFolderId(service));
	}

	private static String getTimeSheetsFolderId(Drive service) throws IOException {
		return findFolderIdByName(service, TIME_SHEETS_FOLDER, findRootFolderId(service));
	}

	private static String getReportsFolderId(Drive service) throws IOException {
		return findFolderIdByName(service, REPORTS_FOLDER, findRootFolderId(service));
	}

	@Override
	public void deleteAllTimeSheets(String userId) throws IOException, GeneralSecurityException {
		Drive service = authServiceI.getDriverService(userId);
		deleteAllfilesInFolder(service, getTimeSheetsFolderId(service));
	}

	@Override
	public void deleteAllReports(String userId) throws IOException, GeneralSecurityException {
		Drive service = authServiceI.getDriverService(userId);
		deleteAllfilesInFolder(service, getReportsFolderId(service));
	}

	private static FileList getAllFilesInFolder(Drive service, String folderID) throws IOException {
		String query = String.format("'%s' in parents and mimeType != '%s'", folderID,
				GoogleDriveMimeType.GOOGLE_FOLDER.getMimeType());

		return service.files().list().setQ(query).setSpaces("drive").setFields("files(id, name)").execute();

	}

	private static void deleteAllfilesInFolder(Drive service, String folderID) throws IOException {

		FileList fileList = getAllFilesInFolder(service, folderID);

		if (fileList.getFiles().isEmpty()) {
			return;
		}

		// Batch callback for handling the response
		JsonBatchCallback<Void> callback = new JsonBatchCallback<Void>() {
			@Override
			public void onSuccess(Void content, HttpHeaders responseHeaders) {
//				System.out.println("File deleted successfully.");
			}

			@Override
			public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders) {
//				System.err.println("Error deleting file: " + e.getMessage());
			}
		};

		// Create a batch request
		BatchRequest batch = service.batch();

		// Iterate through the file list and queue each delete request in the batch
		for (File file : fileList.getFiles()) {
//			System.out.printf("Deleting file: %s (%s)%n", file.getName(), file.getId());

			service.files().delete(file.getId()).queue(batch, callback); // Queue the delete request in the batch
		}

		// Execute the batch request
		batch.execute();

	}

	@Async
	@Override
	public void generateReports(String userId) throws IOException, GeneralSecurityException {
		isServerBusy = true;
		generateReportStatus.put(userId, "InProcess");
		
		Drive service;
		String timeSheetsFolderId;
		FileList fileList; 
		
		try {
			service = authServiceI.getDriverService(userId);
			
			timeSheetsFolderId = getTimeSheetsFolderId(service);

			fileList = getAllFilesInFolder(service, timeSheetsFolderId);
			
		} catch (IOException | GeneralSecurityException e) {
			isServerBusy = false;
			generateReportStatus.put(userId, "Finished with error");
			throw e;
		}
		
		

		Map<Integer, Report> clients = new HashMap<Integer, Report>();

		for (File file : fileList.getFiles()) {

			try {
				TimeSheet ts = getTimeSheet(service.files().get(file.getId()).executeMedia().getContent());

				for (TimeSheet.Client client : ts.getClients()) {

					int clientId = client.getId();
					String clientName = client.getName();
					if (clientId > 0 && clientName != null && !clientName.isBlank()) {
						if (clients.containsKey(clientId)) {
							updateReport(clients.get(clientId), client, ts);
						} else {
							clients.put(clientId, fillNewReport(client, ts));
						}
					}
				}

			} catch (InvalidFormatException | IOException e) {
				e.printStackTrace();
			}
		}
		
		try {
			deleteAllReports(userId);
		} catch (IOException | GeneralSecurityException e) {
			isServerBusy = false;
			generateReportStatus.put(userId, "Finished with error");
			throw e;
		}


		try (InputStream templateIS = downloadReportTemplate(userId);
				OPCPackage pkg = OPCPackage.open(templateIS, true);
				XSSFWorkbook wb = new XSSFWorkbook(pkg);
				ByteArrayOutputStream baos = new ByteArrayOutputStream(65536);) {
			
			String reportsFolderId = getReportsFolderId(service);

			for (Entry<Integer, Report> reportEntry : clients.entrySet()) {
				Report report = reportEntry.getValue();
				if (report.getEmployees().isEmpty()) {
					continue;
				}
				contentProcessorService.fillReport(report, wb, baos);

				uploadFileToDrive(service, GoogleDriveMimeType.MS_EXCEL, report.getFullClientName(), reportsFolderId,
						baos);
				baos.reset();
				contentProcessorService.cleanReport(report.getEmployees().size(), wb);
			}

		} catch (InvalidFormatException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			generateReportStatus.put(userId, "Finished with error");
			isServerBusy = false;
			return;
		}
		
		generateReportStatus.put(userId, "Finished successfully");
		isServerBusy = false;
	}

	private TimeSheet getTimeSheet(InputStream is) throws InvalidFormatException, IOException {
		XSSFSheet timeSheet = contentProcessorService.getTimeSheetContent(is);
		return contentProcessorService.getEmployeeData(timeSheet);
	}

	private Report fillNewReport(TimeSheet.Client client, TimeSheet timeSheet) {

		String fullClientName = String.format("%s %s", client.getName(), client.getAddress());

		Report report = Report.builder().id(client.getId()).fullClientName(fullClientName).build();

		if (client.getHours() > 0) {
			Report.Employee reportEmployee = report.new Employee();
			reportEmployee.setId(timeSheet.getId());
			reportEmployee.setFullName(timeSheet.getFullName());
			reportEmployee.setHours(client.getHours());
			reportEmployee.setPeriod(timeSheet.getPeriod());

			report.addEmployee(reportEmployee);
		}

		return report;
	}

	private Report updateReport(Report report, TimeSheet.Client client, TimeSheet timeSheet) {
		Report.Employee reportEmployee = report.new Employee();
		reportEmployee.setId(timeSheet.getId());
		reportEmployee.setFullName(timeSheet.getFullName());
		reportEmployee.setHours(client.getHours());
		reportEmployee.setPeriod(timeSheet.getPeriod());

		report.addEmployee(reportEmployee);

		return report;
	}

	@Override
	public String getRootFolderId(String userId) throws GeneralSecurityException {

		try {
			return findRootFolderId(authServiceI.getDriverService(userId));
		} catch (IOException e) {
			return "";
		}
	}

	@Override
	public void uploadTimeSheetTemplate(String userId, InputStream is) throws GeneralSecurityException, IOException {
		Drive service = authServiceI.getDriverService(userId);

		String folderId = getTemplatesFolderId(service);
		
		try {
			File template = findFileByName(service, TIME_SHEET_TEMPLATE_NAME, folderId);
			service.files().delete(template.getId()).execute();
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("File not found continue update Report Template");
		}

		uploadFileToDrive(service, GoogleDriveMimeType.MS_EXCEL, TIME_SHEET_TEMPLATE_NAME, folderId, is);

	}

	@Override
	public void uploadTimeSheet(String userId, InputStream is) throws GeneralSecurityException, IOException {
		Drive service = authServiceI.getDriverService(userId);

		try (ByteArrayInputStream bais = new ByteArrayInputStream(is.readAllBytes())){
			String folderId = getTimeSheetsFolderId(service);
			XSSFSheet sheet = contentProcessorService.getTimeSheetContent(bais);
			bais.reset();
			String employeeName = contentProcessorService.getEmployeeName(sheet);
			int employeeId = contentProcessorService.getEmployeeId(sheet);
			String fileName = String.format("%s %s", employeeName, employeeId);
			uploadFileToDrive(service, GoogleDriveMimeType.MS_EXCEL, fileName, folderId, bais);
			
		} catch (IOException | InvalidFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

	@Override
	public void uploadReportTemplate(String userId, InputStream is) throws GeneralSecurityException, IOException {

		Drive service = authServiceI.getDriverService(userId);

		String folderId = getTemplatesFolderId(service);

		try {
			File template = findFileByName(service, REPORT_TEMPLATE_NAME, folderId);
			service.files().delete(template.getId()).execute();
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("File not found continue update Report Template");
		}
		
		uploadFileToDrive(service, GoogleDriveMimeType.MS_EXCEL, REPORT_TEMPLATE_NAME, folderId, is);

	}

	@Override
	public String getGenerateReportsStatus(String userId) {
		return generateReportStatus.get(userId);
	}

	@Override
	public boolean isGenerateReportBeasy() {
		return isServerBusy;
	}

}