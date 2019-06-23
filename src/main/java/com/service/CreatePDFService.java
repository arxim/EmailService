//pdfService
package com.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import com.dao.ExpenseDetailDAO;
import com.dao.PaymentVoucherDAO;
import com.dao.SummaryDFUnpaidByDetailAsOfDateDAO;
import com.dao.SummaryDFUnpaidSubreportDAO;
import com.dao.SummaryRevenueByDetailDAO;
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

public class CreatePDFService {

	ByteArrayOutputStream pdfOutputStream = null;

	// สร้างไฟล์ pdf
	// ส่งค่ากลับเป็นจำนวนไฟล์ที่ต้องทำ size > 0แสดงว่ามีไฟล์ที่ต้องการ merge
	public ByteArrayOutputStream createFilePDF(List<JasperPrint> listJasper)
			throws JRException, IOException, SQLException, AddressException, MessagingException {
		String password = "1234";

		pdfOutputStream = new ByteArrayOutputStream();

		// construct exports report to pdf
		JRPdfExporter exporter = new JRPdfExporter();
		// นำชื่อไฟล์ส่งไป ยังฟังก์ชัน getInJasperFile() เพื่อ map parameter
		exporter.setExporterInput(SimpleExporterInput.getInstance(listJasper));

		exporter.setExporterOutput(// เก็บไฟล์ที่ ?
				new SimpleOutputStreamExporterOutput(pdfOutputStream));
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

		exporter.exportReport();

		System.out.println("Export file PDF success...!!");

		return pdfOutputStream;

	}

	// รับไฟล์ jasper
	public JasperPrint getInJasperFile(String jasperFile, String code_doctor)
			throws JRException, IOException, SQLException {

		// 1. รับไฟล์ jasper เพื่อ put ค่า
		String file = new File(this.getClass().getResource("/jasperReport/" + jasperFile).getFile()).getAbsoluteFile()
				.toString();
		// 1.1. รับค่า parameter
		String from_doctor = code_doctor;
		String to_doctor = code_doctor;
		String doctor = code_doctor;
		String hospitalCode = Property.getCenterProperty("/property/application.properties")
				.getProperty("hospitalCode");
		String yyyy = Property.getCenterProperty("/property/application.properties").getProperty("yyyy");
		String to_date = Property.getCenterProperty("/property/application.properties").getProperty("to_date");
		String from_date = Property.getCenterProperty("/property/application.properties").getProperty("from_date");
		String mm = Property.getCenterProperty("/property/application.properties").getProperty("mm");
		String absoluteDiskPath = new File(Property.class.getClassLoader()
				.getResource(
						Property.getCenterProperty("/property/application.properties").getProperty("absoluteDiskPath"))
				.getFile()).getAbsoluteFile().toString();

		Map<String, Object> params = new HashMap<String, Object>();

		// รับค่า จาก Property

		String[] rows = Property.getCenterProperty("/property/application.properties").getProperty("jasperFiles")
				.split(",");

		for (int j = 0; j < rows.length; j++) {

			// เลือก ว่าจะเข้าอันไหนบ้าง
			if (jasperFile.equals(rows[j])) {
				System.out.print("----------------------------------------------\n" + rows[j] + "\n");
				String[] cols = Property.getCenterProperty("/property/application.properties")
						.getProperty("jasperFiles[" + j + "]").split(",");
				System.out.println(cols.length);

				for (int i = 0; i < cols.length; i++) {

					// คอลัมที่เท่าไหร่ก็ตามที่เจอคำนี้ ให้ ทำการ save แบบนี้
					if (cols[i].equals("from_doctor")) {

						System.out.println(cols[i] + "			" + from_doctor);
						params.put(cols[i], from_doctor);
						// ทำเสร็จไป column ถัดไป
						continue;

					}
					if (cols[i].equals("to_doctor")) {

						System.out.println(cols[i] + "			" + to_doctor);
						params.put(cols[i], to_doctor);
						continue;

					}
					if (cols[i].equals("doctor")) {

						System.out.println(cols[i] + "			" + doctor);
						params.put(cols[i], doctor);
						continue;

					}
					if (cols[i].equals("from_date")) {
						System.out.println(cols[i] + "			" + from_date);
						params.put(cols[i], from_date);
						continue;
					}
					if (cols[i].equals("to_date")) {
						System.out.println(cols[i] + "			" + to_date);
						params.put(cols[i], to_date);
						continue;
					}
					if (cols[i].equals("hospital_code")) {
						System.out.println(cols[i] + "			" + hospitalCode);
						params.put(cols[i], hospitalCode);
						continue;

					}
					if (cols[i].equals("month")) {
						System.out.println(cols[i] + "			" + mm);
						params.put(cols[i], mm);
						continue;

					}
					if (cols[i].equals("year")) {
						System.out.println(cols[i] + "			" + yyyy);
						params.put(cols[i], yyyy);
						continue;

					}
					if (cols[i].equals("SUBREPORT_DIR")) {
						System.out.println(cols[i] + "			" + absoluteDiskPath);
						params.put(cols[i], absoluteDiskPath);
						continue;
					} else {
						System.out.println(
								cols[i] + "			" + Property.getCenterProperty("/property/application.properties")
										.getProperty("jasperFiles[" + j + "][" + i + "]"));
						params.put(cols[i], Property.getCenterProperty("/property/application.properties")
								.getProperty("jasperFiles[" + j + "][" + i + "]"));
					}

				}
				// add column เสร็จให้ ไป fillReport
				break;
			}

		}

		// creatReport
		JasperPrint jasperPrint = JasperFillManager.fillReport(file, params, DbConnector.getDBConnection());
		return jasperPrint;
	}

	// check ว่าหมอมีข้อมูลใน ไฟล์นั้นๆไหม
	public static int getNRowReport(String jasperFile, String code_doctor) throws SQLException, IOException {

		int n_row = 0;
		switch (jasperFile) {

		case "ExpenseDetail.jasper":
			n_row = ExpenseDetailDAO.getNExpenseDetail(code_doctor);
			break;
		case "PaymentVoucher.jasper":
			n_row = PaymentVoucherDAO.getNPaymentVoucher(code_doctor);
			break;
		case "SummaryDFUnpaidByDetailAsOfDate.jasper":
			n_row = SummaryDFUnpaidByDetailAsOfDateDAO.getNSummaryDFUnpaidByDetailAsOfDate(code_doctor);
			break;
		case "SummaryDFUnpaidSubreport.jasper":
			n_row = SummaryDFUnpaidSubreportDAO.getNSummaryDFUnpaidByDetailAsOfDate(code_doctor);
			break;
		case "SummaryRevenueByDetail.jasper":
			n_row = SummaryRevenueByDetailDAO.getNSummaryDFUnpaidByDetailAsOfDate(code_doctor);
			break;
		}

		return n_row;

	}

}
