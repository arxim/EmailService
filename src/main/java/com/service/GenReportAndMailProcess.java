package com.service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;
import org.springframework.stereotype.Component;

import com.dao.DoctorDAO;
import com.util.Property;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;

@Component
public class GenReportAndMailProcess {

	public static void main(String[] args)
			throws JRException, IOException, MessagingException, SQLException {
		/*
		 * ArrayList<HashMap<String,String>> listDoctor = sm.getNRecive(); for (int i =
		 * 0; i < listDoctor.size(); i++) { String name =
		 * listDoctor.get(i).get("NAME_THAI"); }
		 */

		System.out.println("Send mail....\n");
		loopSend();

	}

	// อังกอริทึมสำหรับวนลูปส่งเมล์ทีละ ตามจำนวนสูงสุดของผู้ส่ง
	public static int loopSend()
			throws JRException, IOException, MessagingException, SQLException {

		// account เก็บ อีเมล์ ผู้ส่งจาก data.properties
		String[] account = Property.getCenterProperty("/property/application.properties").getProperty("sendersMail")
				.split(",");
		// reciver มีชนิดเป็น int เก็บจำนวนหมอที่ยังไม่ถูกส่งจดหมาย
		int n_reciver = DoctorDAO.getNReciver();

		// จำนวนการส่งสูงสุดของ Sender
		int sent = Integer
				.parseInt(Property.getCenterProperty("/property/application.properties").getProperty("Nsend"));
		// นับจำนวนที่ถูกส่งไปแล้ว เริ่มต้นยังไม่ถูกส่ง sened = 0
		int n_sent = 0;
		// เริ่มต้นวนแอคเค๊าที่จะใช้ส่ง i เริ่มต้นที่ 0 หมายถึง ใช้ อีเมล์ index ที่ 0
		// ใน account ที่มีชนิดข้อมูลเป็น array string
		for (int i = 0; i < account.length; i++) {
			// กรณีไม่มีผู้รับ ให้หลุดจากลูป
			if (n_reciver != 0) { //
				// แยกอีเมล์กับพาสเวิสออกจากกัน index = 0 เก็บ ชื่อ e-mail , index = 1 เก็บ
				// รหัสผ่านอีเมล์
				String[] email = account[i].split("&");
				System.out.println((i + 1) + ". Sending emails using <" + email[0] + " , " + email[1] + ">");
				// วนส่งเท่ากับ จำนวนสูงสุดที่ใช้ในการส่ง
				for (int j = 0; j < sent; j++) {
					// กรณียังส่งไม่หมดให้ส่งต่อไป จนกว่าจะส่งครบ ให้หลุดจากลูป
					if (n_reciver != 0) {

						System.out.print("	(" + (n_sent + 1) + ") ");

						// --- processing การสร้างไฟล์ให้หมอแต่ละคน
						// mail_doctor เก็บอีเมล์หมอจากฐานข้อมูล แต่ละคน
						String mail_doctor = DoctorDAO.getReciver().get(n_sent).get("EMAIL").toString();
						// code_doctor เก็บโค้ดหมอจากฐานข้อมูลแต่ละคน
						String code_doctor = DoctorDAO.getReciver().get(n_sent).get("DOCTOR_PROFILE_CODE").toString();

						System.out.print(code_doctor + "\t" + mail_doctor + "\t:\n\t\t");

						// Test
						// jasperFiles เก็บไฟล์ jasper เป็น string array
						String[] jasperFiles = Property.getCenterProperty("/property/application.properties")
								.getProperty("jasperFiles").split(",");

						// เก็บชื่อไฟล์ที่หมอจะต้องทำไว้ใน list
						List<JasperPrint> listJasper = new ArrayList<JasperPrint>();
						CreatePDFService c = new CreatePDFService();

						// รับชื่อไฟล์ จาก ไฟล์ jasper ทั้งหมด
						for (String jasperFile : jasperFiles) {

							// เงื่อนไข ตรวจสอบก่อนทุกครั้งว่า ไฟล์เจสเปอนี้ หมอมีขข้อมูลไหม ?
							// ถ้ามี row > 0 ให้ ทำการ put ข้อมูล เพื่อสร้างไฟลฺ pdf
							// ส่ง ชื่อไฟล์และรหัสหมอไปทำ การตรวจสอบ
							// เก็บชื่อไฟล์ที่จะต้องทำไว้ใน list
							if (CreatePDFService.getNRowReport(jasperFile, code_doctor) > 0) {
								// เก็บ JasperPrint ลงใน listJasper จากการส่งไป ยังฟังก์ชัน getInJasperFile()
								// เพื่อ map parameter แล้ว
								listJasper.add(c.getInJasperFile(jasperFile, code_doctor));
							}
						}

						// ต้องมีไฟล์ ถึงจะส่ง
						if (listJasper.size() > 0) {
							SendmailService s = new SendmailService();

							// Mutireciver
							List<String> userReciverMail = new ArrayList<String>();
							userReciverMail.add("springbootrecive@gmail.com");
							userReciverMail.add("wintazaza@gmail.com");
							userReciverMail.add("spittayakorn@gmail.com");

							s.sendmail(email[0], email[1], userReciverMail, c.createFilePDF(listJasper));
						}

						// endTest

						// --- end processing
						n_sent++;
						n_reciver--;
					} else {
						break;
					}

				}
				System.out.print("\n\n");

			} else {
				break;
			}

		}
		System.out.println("\nจำนวนผู้รับทั้งหมด 	:	" + DoctorDAO.getNReciver() + "	คน\n");
		System.out.println("จำนวนที่ส่งไปแล้ววันนี้	:	" + n_sent + "	คน\n");
		System.out.println("จำนวนที่จะต้องส่งในวันพรุ่งนี้	:	" + n_reciver + "	คน \n");
		return n_reciver;
	}

}
