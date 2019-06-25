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

import java.util.regex.Pattern;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import com.sendmail.dao.ProcessingSqlDao;
import com.sendmail.util.DbConnector;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;
import net.sf.jasperreports.export.SimplePdfReportConfiguration;
import com.sendmail.util.Property;

public class CreatePDFService {
	String[] jasperFiles = null;
	List<String> get_jasperDocFile = null;
	List<JasperPrint> listJasper = null;
	ByteArrayOutputStream pdfOutputStream = null;

	// แสดงชื่อ ตัด นามสกุล .jasper ออก
	public String getPDFName(String filePDF) {
		String[] pdfName = filePDF.split(Pattern.quote("."));
		String word = pdfName[0];
		return word;
	}

	// แสดงชื่อไฟล์ jasper จาก data.properties
	public String[] getJasperFile() throws IOException {
		return Property.callDataProperty().getProperty("jasperFiles").split(",");
	}

	// สร้างไฟล์ pdf
	// ส่งค่ากลับเป็นจำนวนไฟล์ที่ต้องทำ size > 0แสดงว่ามีไฟล์ที่ต้องการ merge
	public void createFilePDF(String code_doctor, String emailID, String emailPass, String reciver)
			throws JRException, IOException, SQLException, AddressException, MessagingException {
		String password = "1234";

		// jasperFiles เก็บไฟล์ jasper เป็น string array
		jasperFiles = getJasperFile();
		// เก็บชื่อไฟล์ที่หมอจะต้องทำไว้ใน list
		listJasper = new ArrayList<JasperPrint>();
		pdfOutputStream = new ByteArrayOutputStream();

		// Check
		// get_jasperDocFile = new ArrayList<String>();
		// รับชื่อไฟล์ จาก ไฟล์ jasper ทั้งหมด
		for (String jasperFile : jasperFiles) {

			// เงื่อนไข ตรวจสอบก่อนทุกครั้งว่า ไฟล์เจสเปอนี้ หมอมีขข้อมูลไหม ?
			// ถ้ามี row > 0 ให้ ทำการ put ข้อมูล เพื่อสร้างไฟลฺ pdf
			// ส่ง ชื่อไฟล์และรหัสหมอไปทำ การตรวจสอบ
			// เก็บชื่อไฟล์ที่จะต้องทำไว้ใน list
			if (ProcessingSqlDao.checkJasperReport(jasperFile, code_doctor) > 0) {
				// เก็บ JasperPrint
				listJasper.add(getInJasperFile(jasperFile, code_doctor));
				// Check
				// get_jasperDocFile.add(jasperFile);
			}
		}

		// Check
		/*
		 * System.out.println(); int count = 1; for(String a : get_jasperDocFile) {
		 * System.out.println("\t\t"+count+ ".\t"+a); count++; } System.out.println();
		 */

		if (listJasper.size() > 0) {
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
			// จากนั้นส่งไปให้หมอทีละคน
			// ตามลำดับ //
			SendmailService s = new SendmailService();
			s.sendmail(emailID, emailPass, "springbootrecive@gmail.com", code_doctor, pdfOutputStream);
		}

	}

	// รับไฟล์ jasper
	public JasperPrint getInJasperFile(String jasperFile, String code_doctor)
			throws JRException, IOException, SQLException {

		// 1. รับไฟล์ jasper
		String file = new File(this.getClass().getResource("/jasperReport/" + jasperFile).getFile()).getAbsoluteFile()
				.toString();
		// 1.1. รับค่า parameter
		String from_doctor = code_doctor;
		String to_doctor = code_doctor;
		String doctor = code_doctor;
		String hospitalCode = Property.getHospitalCode();
		String yyyy = Property.getYyyy();
		String to_date = Property.getTo_date();
		String from_date = Property.getFrom_date();
		String mm = Property.getMm();
		String absoluteDiskPath = Property.getabsoluteDiskPath();

		Map<String, Object> params = new HashMap<String, Object>();

		// รับค่า จาก Property

		String[] rows = Property.callDataProperty().getProperty("jasperFiles").split(",");

		for (int j = 0; j < rows.length; j++) {

			// เลือก ว่าจะเข้าอันไหนบ้าง
			if (jasperFile.equals(rows[j])) {
				System.out.print("----------------------------------------------\n" + rows[j] + "\n");
				String[] cols = Property.callDataProperty().getProperty("jasperFiles[" + j + "]").split(",");
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
						System.out.println(cols[i] + "			"
								+ Property.callDataProperty().getProperty("jasperFiles[" + j + "][" + i + "]"));
						params.put(cols[i],
								Property.callDataProperty().getProperty("jasperFiles[" + j + "][" + i + "]"));
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
	System.out.println("");
}
