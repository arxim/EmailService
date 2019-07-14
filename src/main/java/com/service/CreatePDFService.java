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
import com.dao.SummaryRevenueByDetailDAO;
import com.util.DbConnector;
import com.util.JDate;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;
import net.sf.jasperreports.export.SimplePdfReportConfiguration;
import com.util.Property;
import com.dao.BatchDao;

public class CreatePDFService {

	ByteArrayOutputStream pdfOutputStream = null;

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
			throws Exception {

		// 1. รับไฟล์ jasper เพื่อ put ค่า
		String file = new File(this.getClass().getResource("/jasperReport/" + jasperFile).getFile()).getAbsoluteFile()
				.toString();
		// 1.1. รับค่า parameter
		String from_doctor = code_doctor;
		String to_doctor = code_doctor;
		String doctor = code_doctor;
		String hospitalCode = Property.getCenterProperty("/application.properties").getProperty("hospitalCode");
		//String yyyy = Property.getCenterProperty("/application.properties").getProperty("yyyy");
		//String mm = Property.getCenterProperty("/application.properties").getProperty("mm");
		//String to_date = Property.getCenterProperty("/application.properties").getProperty("to_date");
		String from_date = Property.getCenterProperty("/application.properties").getProperty("from_date");
		String absoluteDiskPath = new File(CreatePDFService.class.getClass().getResource("/jasperReport").getFile())
				.getPath().toString();

		String mm = BatchDao.getMonth(hospitalCode);
		String yyyy = BatchDao.getYear(hospitalCode);
		String to_date = JDate.getLastDayOfMonth(Integer.parseInt(yyyy),Integer.parseInt(mm));
		
		
		

		Map<String, Object> params = new HashMap<String, Object>();
		// รับค่า จาก Property

		// --------------------------------------------------------------------------
		String[] jasperFiles = Property.getCenterProperty("/application.properties").getProperty("jasperFiles")
				.split(",");

		for (String jasFile : jasperFiles) {
			// เลือก ว่าจะเข้าอันไหนบ้าง
			if (jasperFile.equals(jasFile)) {
				System.out.print("-------------------------------------------------------------------------------\n"
						+ jasFile + "\n");

				// หา คอลัมใน เจสเปอนั้นๆ
				String[] getParaJas = Property.getCenterProperty("/application.properties")
						.getProperty("jasperFiles[" + jasFile + "]").split(",");
				System.out.println(getParaJas.length);

				for (String parameter : getParaJas) {
					// คอลัมที่เท่าไหร่ก็ตามที่เจอคำนี้ ให้ ทำการ save แบบนี้
					if (parameter.equals("from_doctor")) {

						System.out.println(parameter + "\t\t\t" + from_doctor);
						params.put(parameter, from_doctor);
						// ทำเสร็จไป column ถัดไป
						continue;

					}
					if (parameter.equals("to_doctor")) {

						System.out.println(parameter + "\t\t\t" + to_doctor);
						params.put(parameter, to_doctor);
						continue;

					}
					if (parameter.equals("doctor")) {

						System.out.println(parameter + "\t\t\t" + doctor);
						params.put(parameter, doctor);
						continue;

					}
					if (parameter.equals("from_date")) {
						System.out.println(parameter + "\t\t\t" + from_date);
						params.put(parameter, from_date);
						continue;
					}
					if (parameter.equals("to_date")) {
						System.out.println(parameter + "\t\t\t" + to_date);
						params.put(parameter, to_date);
						continue;
					}
					if (parameter.equals("hospital_code")) {
						System.out.println(parameter + "\t\t\t" + hospitalCode);
						params.put(parameter, hospitalCode);
						continue;

					}
					if (parameter.equals("month")) {
						System.out.println(parameter + "\t\t\t" + mm);
						params.put(parameter, mm);
						continue;

					}
					if (parameter.equals("year")) {
						System.out.println(parameter + "\t\t\t" + yyyy);
						params.put(parameter, yyyy);
						continue;

					}
					if (parameter.equals("SUBREPORT_DIR")) {
						System.out.println(parameter + "\t\t\t" + absoluteDiskPath);
						params.put(parameter, absoluteDiskPath);
						continue;
					} else {
						System.out.println(parameter + "\t\t\t" + Property.getCenterProperty("/application.properties")
								.getProperty("jasperFiles[" + jasFile + "][" + parameter + "]"));
						params.put(parameter, Property.getCenterProperty("/application.properties")
								.getProperty("jasperFiles[" + jasFile + "][" + parameter + "]"));
					}

				}

				System.out.println("put data in method getInJasperFile Success");
				// add column เสร็จให้ ไป fillReport
				break;
			}
		}

		// creatReport
		JasperPrint jasperPrint = JasperFillManager.fillReport(file, params, DbConnector.getDBConnection());
		System.out.println("success fill report in method getInJasperFile()...");
		return jasperPrint;
	}

	// check ว่าหมอมีข้อมูลใน ไฟล์นั้นๆไหม
	public static int getNRowReport(String jasperFile, String code_doctor) throws Exception {

		int n_row = 0;
		switch (jasperFile) {

		case "ExpenseDetail.jasper":
			try {
				n_row = ExpenseDetailDAO.getNExpenseDetail(code_doctor);
			} catch (SQLException | IOException e) {
				System.out.println(
						"fail get number of row jasper 'ExpenseDetail.jasper' file from method getNRowReport() !");
			}
			break;

		case "PaymentVoucher.jasper":
			try {
				n_row = PaymentVoucherDAO.getNPaymentVoucher(code_doctor);
			} catch (SQLException | IOException e) {
				System.out.println(
						"fail get number of row jasper 'PaymentVoucher.jasper' file from method getNRowReport() !");
			}
			break;

		case "SummaryDFUnpaidByDetailAsOfDate.jasper":
			try {
				n_row = SummaryDFUnpaidByDetailAsOfDateDAO.getNSummaryDFUnpaidByDetailAsOfDate(code_doctor);
			} catch (SQLException | IOException e) {
				System.out.println(
						"fail get number of row jasper 'SummaryDFUnpaidByDetailAsOfDate.jasper' file from method getNRowReport() !");
			}
			break;

		case "SummaryRevenueByDetail.jasper":
			try {
				n_row = SummaryRevenueByDetailDAO.getNSummaryDFUnpaidByDetailAsOfDate(code_doctor);
			} catch (SQLException | IOException e) {
				System.out.println(
						"fail get number of row jasper 'SummaryRevenueByDetail.jasper' file from method getNRowReport() !");
			}
			break;
		}
		System.out.println("\ncheck row in method getNRowReport Success");

		return n_row;

	}

}
