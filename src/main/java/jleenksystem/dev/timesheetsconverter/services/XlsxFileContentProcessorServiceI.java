package jleenksystem.dev.timesheetsconverter.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import jleenksystem.dev.timesheetsconverter.models.reports.Report;
import jleenksystem.dev.timesheetsconverter.models.reports.TimeSheet;

public interface XlsxFileContentProcessorServiceI {

	XSSFSheet getTimeSheetContent(InputStream is) throws IOException, InvalidFormatException;

	String getEmployeeName(XSSFSheet sheet);

	int getEmployeeId(XSSFSheet sheet);

	TimeSheet getEmployeeData(XSSFSheet sheet);

	void fillReport(Report report, XSSFWorkbook wb, OutputStream destinotion) throws IOException, InvalidFormatException;
	
	void cleanReport(int  employeeCount, XSSFWorkbook wb);
	
	String getPeriod(XSSFSheet sheet);
	
}
