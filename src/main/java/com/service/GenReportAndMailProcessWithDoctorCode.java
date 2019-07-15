package com.service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.mail.MessagingException;
import com.dao.DoctorDAO;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;

public class GenReportAndMailProcessWithDoctorCode {

	public static void sendByCodeDoctor(String mail_doc, String code_doc, String password_doc, String jasperFileName) {
		// mail_doctor เก็บเมล์หมอแต่ละคน
		String mail_doctor = mail_doc;
		// code_doctor เก็บโค้ดหมอจากฐานข้อมูลแต่ละคน
		String code_doctor = code_doc;
		// pass_encrytDoctor เก็บรหัสผ่านไฟล์ pdf
		// password_doctor = list.get(n_sent).get("PASS_ENCRYPT");
		String password_doctor = password_doc;

		System.out.println("\nEmail:\n\t" + mail_doctor + "\nDoctor code:\n\t" + code_doctor + "\nPassword doctor:\n\t"
				+ password_doctor);

		// ประกาศ listJasper เก็บไฟล์ jasper ที่มีการ put parameter เรียบร้อยแล้ว
		ArrayList<JasperPrint> listJasper = new ArrayList<JasperPrint>();
		CreatePDFService c = new CreatePDFService();

		try {
			try {
				listJasper
						.add(c.getInJasperFile(jasperFileName.substring(0, jasperFileName.length() - 7), code_doctor));
			} catch (IOException e) {

				e.printStackTrace();
			}
		} catch (JRException | SQLException e) {

			e.printStackTrace();
		}

		// construct > นำไฟล์ใน list ไปสร้าง pdf ไฟล์ > ส่งเมล์
		SendmailService sendmailService = null;
		String pass = "";
		// ต้องมีไฟล์ ถึงจะส่ง
		if (listJasper.size() > 0) {
			sendmailService = new SendmailService();

			// Mutireciver
			List<String> userReciverMail = new ArrayList<String>();
			// userReciverMail.add(mail_doctor);
			userReciverMail.add("springbootrecive@gmail.com");
			userReciverMail.add("wintazaza@gmail.com");
			userReciverMail.add("spittayakorn@gmail.com");

			try {

				// multi reciver
				sendmailService.sendmail("springbootrecive@gmail.com", "=2r549z5c", userReciverMail,
						c.createFilePDF(listJasper, password_doctor));

			} catch (MessagingException | IOException e1) {
				pass = "false";
				System.out.println("fail send mail from method loopSend() !");
			}

			try {

				if (pass.equals("true")) {
					DoctorDAO.SendMailPaymentSuccess(code_doctor);
					System.out.println("success stam reciver from method loopSend()...Doctor code is " + code_doctor);
				}

			} catch (SQLException | IOException e) {

				System.out.println("fail sql exception from method loopSend()");
			}
		}
	}

}
