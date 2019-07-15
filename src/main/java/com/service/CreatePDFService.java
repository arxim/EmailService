//pdfService
package com.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import com.util.DbConnector;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;
import net.sf.jasperreports.export.SimplePdfReportConfiguration;
import com.util.Property;
import com.dao.DoctorDAO;

public class CreatePDFService {

	private ByteArrayOutputStream pdfOutputStream = null;

	// สร้างไฟล์ pdf
	public ByteArrayOutputStream createFilePDF(List<JasperPrint> listJasper, String passwords) {

		// String password = "1234";
		String password = passwords;

		pdfOutputStream = new ByteArrayOutputStream();

		// construct exports report to pdf
		JRPdfExporter exporter = new JRPdfExporter();
		// ไฟล์ขาเข้า
		exporter.setExporterInput(SimpleExporterInput.getInstance(listJasper));
		// ไฟล์ขาออก ที่ต้อง return กลับ เป็น ByteArrayOutputStream ไม่มีการวางไฟล์ไว้ใน
		// โฟลเดอ
		exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(pdfOutputStream));
		// ตั่งค่า pdf file
		SimplePdfReportConfiguration reportConfig = new SimplePdfReportConfiguration();
		reportConfig.setSizePageToContent(true);
		reportConfig.setForceLineBreakPolicy(false);

		SimplePdfExporterConfiguration exportConfig = new SimplePdfExporterConfiguration();

		exportConfig.set128BitKey(true);
		// ใส่รหัสผ่าน ให้ pdf file
		exportConfig.setUserPassword(password);
		exportConfig.setOwnerPassword(password);
		exportConfig.setMetadataAuthor("baeldung");
		exportConfig.setEncrypted(true);
		exportConfig.setAllowedPermissionsHint("PRINTING");

		exporter.setConfiguration(reportConfig);
		exporter.setConfiguration(exportConfig);

		try {
			exporter.exportReport();
			System.out.println("success export roport jasper file from method createFilePDF() !");
		} catch (JRException e) {
			System.out.println("fail export roport jasper file from method createFilePDF() !");
		}

		return pdfOutputStream;

	}

	// ฟังก์ชันการ map ค่า parameter ดึงค่ามาจาก application.propertise
	public JasperPrint getInJasperFile(String jasperFile, String code_doctor)
			throws JRException, IOException, SQLException {

		// 1. รับไฟล์ jasper เพื่อ put ค่า
		String file = new File(this.getClass().getResource("/jasperReport/" + jasperFile + ".jasper").getFile())
				.getAbsoluteFile().toString();
		// 1.1. รับค่า parameter
		String from_doctor = code_doctor;
		String to_doctor = code_doctor;
		String doctor = code_doctor;
		String hospitalCode = Property.getCenterProperty("/application.properties").getProperty("hospitalCode");
		String yyyy = Property.getCenterProperty("/application.properties").getProperty("yyyy");
		String to_date = Property.getCenterProperty("/application.properties").getProperty("to_date");
		String from_date = Property.getCenterProperty("/application.properties").getProperty("from_date");
		String mm = Property.getCenterProperty("/application.properties").getProperty("mm");
		String absoluteDiskPath = new File(CreatePDFService.class.getClass().getResource("/jasperReport").getFile())
				.getPath().toString();

		Map<String, Object> params = new HashMap<String, Object>();
		// รับค่า จาก Property

		Properties propJasper = Property.getProp(jasperFile);

		for (java.util.Iterator<Object> it = propJasper.keySet().iterator(); it.hasNext();) {
			String key = (String) it.next();
			String value = propJasper.getProperty(key);
			// System.out.println(" Key : " + key + " value : " + value);
			// continue เป็นการบอกให้ไปทำ column ถัดไป ในที่นี้มอง column เป็น key ถัดไป
			if (key.equals(jasperFile + ".hospital_code")) {
				params.put(key.substring(jasperFile.length() + 1), hospitalCode);
				System.out.println(key.substring(jasperFile.length() + 1) + "\t\t\t" + hospitalCode);
				continue;

			}
			if (key.equals(jasperFile + ".from_doctor")) {
				params.put(key.substring(jasperFile.length() + 1), from_doctor);
				System.out.println(key.substring(jasperFile.length() + 1) + "\t\t\t" + from_doctor);
				continue;

			}
			if (key.equals(jasperFile + ".to_doctor")) {
				params.put(key.substring(jasperFile.length() + 1), to_doctor);
				System.out.println(key.substring(jasperFile.length() + 1) + "\t\t\t" + to_doctor);
				continue;

			}
			if (key.equals(jasperFile + ".month")) {
				params.put(key.substring(jasperFile.length() + 1), mm);
				System.out.println(key.substring(jasperFile.length() + 1) + "\t\t\t" + mm);
				continue;

			}
			if (key.equals(jasperFile + ".year")) {
				params.put(key.substring(jasperFile.length() + 1), yyyy);
				System.out.println(key.substring(jasperFile.length() + 1) + "\t\t\t" + yyyy);
				continue;

			}
			if (key.equals(jasperFile + ".SUBREPORT_DIR")) {
				params.put(key.substring(jasperFile.length() + 1), absoluteDiskPath);
				System.out.println(key.substring(jasperFile.length() + 1) + "\t\t\t" + absoluteDiskPath);
				continue;

			}
			if (key.equals(jasperFile + ".from_date")) {
				params.put(key.substring(jasperFile.length() + 1), from_date);
				System.out.println(key.substring(jasperFile.length() + 1) + "\t\t\t" + from_date);
				continue;

			}
			if (key.equals(jasperFile + ".to_date")) {
				params.put(key.substring(jasperFile.length() + 1), to_date);
				System.out.println(key.substring(jasperFile.length() + 1) + "\t\t\t" + to_date);
				continue;

			}
			if (key.equals(jasperFile + ".doctor")) {
				params.put(key.substring(jasperFile.length() + 1), doctor);
				System.out.println(key.substring(jasperFile.length() + 1) + "\t\t\t" + doctor);
				continue;

			} else {
				params.put(key.substring(jasperFile.length() + 1), value);
				System.out.println(key.substring(jasperFile.length() + 1) + "\t\t\t" + value);

			}
		}

		// creatReport
		JasperPrint jasperPrint = JasperFillManager.fillReport(file, params, DbConnector.getDBConnection());
		System.out.println("success fill report in method getInJasperFile()...");
		return jasperPrint;

	}

	// check ว่าหมอมีข้อมูลใน ไฟล์นั้นๆไหม
	public static String getNRowReport(String jasperFile, int n_sent,ArrayList<HashMap<String, String>> list) throws IOException, SQLException {

		ArrayList<HashMap<String, String>> lists = list;
		
		String n_row = "";
		
		switch (jasperFile) {

		case "ExpenseDetail.jasper":
			n_row = list.get(n_sent).get("EXPENSE").toString();
			System.out.println(n_row);
			break;

		case "PaymentVoucher.jasper":
			n_row = list.get(n_sent).get("VOUCHER").toString();
			System.out.println(n_row);
			break;

		case "SummaryDFUnpaidByDetailAsOfDate.jasper":
			n_row = list.get(n_sent).get("UNPAID").toString();
			System.out.println(n_row);
			break;

		case "SummaryRevenueByDetail.jasper":
			n_row = list.get(n_sent).get("REVENUE_DETAIL").toString();
			System.out.println(n_row);
			break;
		}
		System.out.println("\ncheck row in method getNRowReport Success");

		return n_row;

	}

}
