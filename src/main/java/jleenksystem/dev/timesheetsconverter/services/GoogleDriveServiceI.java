package jleenksystem.dev.timesheetsconverter.services;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;

public interface GoogleDriveServiceI {

	InputStream downloadTimeSheetTemplate(String userId) throws IOException, GeneralSecurityException;
	
	InputStream downloadReportTemplate(String userId) throws IOException, GeneralSecurityException;
	
	void deleteAllTimeSheets(String userId) throws IOException, GeneralSecurityException;
	
	void deleteAllReports(String userId) throws IOException, GeneralSecurityException;

	void createStructure(String userId) throws IOException, GeneralSecurityException;

	void deleteStructure(String userId) throws IOException, GeneralSecurityException;
	
	void generateReports(String userId) throws IOException, GeneralSecurityException;
	
	String getRootFolderId(String userId) throws IOException, GeneralSecurityException;
	
	void uploadTimeSheetTemplate(String userId, InputStream is) throws IOException, GeneralSecurityException;
	
	void uploadTimeSheet(String userId, InputStream is) throws IOException, GeneralSecurityException;
	
	void uploadReportTemplate(String userId, InputStream is) throws IOException, GeneralSecurityException;
	
	String getGenerateReportsStatus(String userId);
	
	boolean isGenerateReportBeasy();
}