package jleenksystem.dev.timesheetsconverter.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import jleenksystem.dev.timesheetsconverter.models.reports.Report;
import jleenksystem.dev.timesheetsconverter.models.reports.TimeSheet;

@Service
public class XlsxFileContentProcessorService implements XlsxFileContentProcessorServiceI {

	private static final int EMPLOYEE_NAME_ROW = 0;

	private static final int EMPLOYEE_ID_CELL = 6;
	private static final int EMPLOYEE_NAME_CELL = 5;

	private static final int START_DATE_ROW = 42;
	private static final int START_DATE_CELL = 7;

	private static final int CLIENT_FIRST_ROW = 2;
	private static final int CLIENT_ID_CELL = 0;
	private static final int CLIENT_NAME_CELL = 3;
	private static final int CLIENT_ADDRESS_CELL = 4;
	private static final int EMPLOYEE_HOURS_CELL = 8;

	private static final int TOTAL_CLIENTS_AMOUNT = 28;

	private static final int TITLE_ROW = 0;
	private static final int FIRST_EMPLOYEE_REPORT_ROW = 5;
	private static final int EMPLOYEE_REPORT_NAME_CELL = 0;
	private static final int EMPLOYEE_REPORT_PERIOD_CELL = 1;
	private static final int EMPLOYEE_REPORT_HOURS_CELL = 2;

	@Override
	public XSSFSheet getTimeSheetContent(InputStream is) throws IOException, InvalidFormatException {
		try (OPCPackage pkg = OPCPackage.open(is, true); XSSFWorkbook wb = new XSSFWorkbook(pkg);) {
			return wb.getSheetAt(0);
		}

	}


	private Row getEmployeeRow(XSSFSheet sheet) {
		return sheet.getRow(EMPLOYEE_NAME_ROW);
	}

	@Override
	public String getEmployeeName(XSSFSheet sheet) {

		return getEmployeeRow(sheet).getCell(EMPLOYEE_NAME_CELL).getStringCellValue();

	}

	@Override
	public int getEmployeeId(XSSFSheet sheet) {

		return (int) getEmployeeRow(sheet).getCell(EMPLOYEE_ID_CELL).getNumericCellValue();

	}

	@Override
	public TimeSheet getEmployeeData(XSSFSheet sheet) {
		TimeSheet timeSheet = TimeSheet.builder().id(getEmployeeId(sheet)).fullName(getEmployeeName(sheet))
				.period(getPeriod(sheet)).build();

		for (int row = CLIENT_FIRST_ROW; row < TOTAL_CLIENTS_AMOUNT; row++) {
			if (isTimeSheetClientRowExist(sheet, row)) {
				TimeSheet.Client client = timeSheet.new Client();
				client.setId(getClientId(sheet, row));
				client.setName(getClientName(sheet, row));
				client.setAddress(getClientAddress(sheet, row));
				client.setHours(getEmployeeHours(sheet, row));

				timeSheet.addClient(client);
			} else {
				break;
			}
		}

		return timeSheet;
	}

	@Override
	public String getPeriod(XSSFSheet sheet) {
		Date startDate = getStartDateRow(sheet).getCell(START_DATE_CELL).getDateCellValue();

		// Create a Calendar object and set it to the current date
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(startDate);

		// Add 14 days to the calendar
		calendar.add(Calendar.DAY_OF_MONTH, 13);

		Date endDate = calendar.getTime();

		SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd");

		return String.format("%s-%s", dateFormat.format(startDate).toUpperCase(),
				dateFormat.format(endDate).toUpperCase());
	}

	@Override
	public void fillReport(Report report, XSSFWorkbook wb, OutputStream destinotion) throws IOException, InvalidFormatException {

				XSSFSheet reportSheet = wb.getSheetAt(0);

				setReportTitle(reportSheet, report);

				int employeeRow = FIRST_EMPLOYEE_REPORT_ROW;

				for (Report.Employee emplyee : report.getEmployees()) {
					setEmployeeReportRow(reportSheet, emplyee, employeeRow);
					employeeRow++;
				}
				wb.write(destinotion);
	}
	
	@Override
	public void cleanReport(int  employeeCount, XSSFWorkbook wb) {
		XSSFSheet reportSheet = wb.getSheetAt(0);

		cleanReportTitle(reportSheet);
		
		for (int i = FIRST_EMPLOYEE_REPORT_ROW; i < FIRST_EMPLOYEE_REPORT_ROW + employeeCount; i++) {
			cleanEmployeeReportRow(reportSheet, i);
		}
	}

	private void setEmployeeReportRow(XSSFSheet reportSheet, Report.Employee emplyee, int row) {
		Row employeeRow = reportSheet.getRow(row);
		employeeRow.getCell(EMPLOYEE_REPORT_NAME_CELL).setCellValue(emplyee.getFullName());
		employeeRow.getCell(EMPLOYEE_REPORT_PERIOD_CELL).setCellValue(emplyee.getPeriod());

		DecimalFormat df = new DecimalFormat("#.##");
		employeeRow.getCell(EMPLOYEE_REPORT_HOURS_CELL).setCellValue(df.format(emplyee.getHours()));

	}

	private void cleanEmployeeReportRow(XSSFSheet reportSheet, int row) {
		Row employeeRow = reportSheet.getRow(row);
		employeeRow.getCell(EMPLOYEE_REPORT_NAME_CELL).setBlank();
		employeeRow.getCell(EMPLOYEE_REPORT_PERIOD_CELL).setBlank();

		employeeRow.getCell(EMPLOYEE_REPORT_HOURS_CELL).setBlank();
	}

	
	private void setReportTitle(XSSFSheet reportSheet, Report report) {
		reportSheet.getRow(TITLE_ROW).getCell(0).setCellValue(report.getFullClientName());
	}
	
	private void cleanReportTitle(XSSFSheet reportSheet) {
		reportSheet.getRow(TITLE_ROW).getCell(0).setBlank();
	}

	private Row getStartDateRow(XSSFSheet sheet) {
		return sheet.getRow(START_DATE_ROW);
	}

	private String getClientName(XSSFSheet sheet, int row) {
		return sheet.getRow(row).getCell(CLIENT_NAME_CELL).getStringCellValue();
	}

	private String getClientAddress(XSSFSheet sheet, int row) {
		return sheet.getRow(row).getCell(CLIENT_ADDRESS_CELL).getStringCellValue();
	}

	private double getEmployeeHours(XSSFSheet sheet, int row) {
		return sheet.getRow(row).getCell(EMPLOYEE_HOURS_CELL).getNumericCellValue();
	}

	private int getClientId(XSSFSheet sheet, int row) {
		return (int) sheet.getRow(row).getCell(CLIENT_ID_CELL).getNumericCellValue();
	}

	private boolean isTimeSheetClientRowExist(XSSFSheet sheet, int row) {
		try {
			return (Double) sheet.getRow(row).getCell(CLIENT_ID_CELL).getNumericCellValue() != null;
		} catch (IllegalStateException e) {
			return false;
		}
	}

	public static ByteArrayInputStream copyInputStream(InputStream input) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length;
		while ((length = input.read(buffer)) != -1) {
			baos.write(buffer, 0, length);
		}
		return new ByteArrayInputStream(baos.toByteArray()); // Return the copied data in a ByteArrayInputStream
	}
}
