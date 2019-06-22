//mailService
package com.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import org.springframework.stereotype.Component;
import com.sendmail.dao.ProcessingSqlDao;
import com.sendmail.util.DbConnector;
import com.sendmail.util.Property;
import net.sf.jasperreports.engine.JRException;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;

@Component
public class SendmailService {

	DbConnector cnn = null;
	static Message msg = null;
	Property prop = null;
	CreatePDFService create = null;

	/*
	 * @Autowired private SchedulerFactoryBean schedulerFactoryBean;
	 * 
	 * public void rescheduleCronJob() throws SchedulerException, ParseException {
	 * 
	 * String newCronExpression = "0/5 * * * * *"; // the desired cron expression
	 * 
	 * Scheduler scheduler = schedulerFactoryBean.getScheduler(); TriggerKey
	 * triggerKey = new TriggerKey("timeSyncTrigger"); CronTriggerImpl trigger =
	 * (CronTriggerImpl) scheduler.getTrigger(triggerKey);
	 * trigger.setCronExpression(newCronExpression);
	 * scheduler.rescheduleJob(triggerKey, trigger);
	 * 
	 * }
	 */

	public static void main(String[] args)
			throws JRException, InvalidPasswordException, IOException, MessagingException, SQLException {
		/*
		 * ArrayList<HashMap<String,String>> listDoctor = sm.getNRecive(); for (int i =
		 * 0; i < listDoctor.size(); i++) { String name =
		 * listDoctor.get(i).get("NAME_THAI"); }
		 */

		System.out.println("Send mail....\n");

		/*
		 * SendmailService send = new SendmailService(); send.loopSend();
		 */
	}

	// แสดงอีเมล์ผู้ส่งทั้งหมด
	public String[] getSender() throws IOException {
		return Property.getSenderAccount();
	}

	// แสดงจำนวนผู้รับทั้งหมด
	public int getNReciver() throws SQLException {
		return new ProcessingSqlDao().nReciver();
	}

	// แสดงจำนวนสูงสุดในการส่งแต่ละคน จากไฟล์ data.properties จาก key-word "Nsend"
	public int getNSend() throws NumberFormatException, IOException {
		return Integer.parseInt(getPropData().getProperty("Nsend"));
	}

	// แสดงข้อมูล จากการเลือก แถว และ คอลัมภ์ จากการ query จากฐานข้อมูลรายชื่อหมอ
	public String getDataReciver(int row, String column) throws SQLException {

		return Property.getListEmail(row, column);
	}

	// ให้ไปหาไฟล์ data.properties เพื่อเรียกใช้ key word ต่างๆ
	public static Properties getPropData() throws IOException {
		// ไปเรียกไฟล์ data. property
		Properties config = Property.callDataProperty();
		return config;

	}

	public static Properties getConfigSMTP() throws IOException {

		// config gmail SMTP
		Properties props = new Properties();
		props.put("mail.smtp.auth", getPropData().getProperty("mail.smtp.auth"));
		props.put("mail.smtp.starttls.enable", getPropData().getProperty("mail.smtp.starttls.enable"));
		props.put("mail.smtp.host", getPropData().getProperty("mail.smtp.host"));
		props.put("mail.smtp.port", getPropData().getProperty("mail.smtp.port"));
		return props;
	}

	public static Session getSession(String email, String passMail) throws IOException {

		// รับชื่อผู้ใช้ อีเมล์,รหัสผ่านอีเมล์ เข้าถึงเมล์ผู้ใช้
		Session session = Session.getInstance(getConfigSMTP(), new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(email, passMail);
			}
		});

		return session;
	}

	// อังกอริทึมสำหรับวนลูปส่งเมล์ทีละ ตามจำนวนสูงสุดของผู้ส่ง
	public int loopSend() throws JRException, InvalidPasswordException, IOException, MessagingException, SQLException {
		// ประกาศ s สำหรับแปลงไฟล์ jasper เป็น pdf
		create = new CreatePDFService();
		// account เก็บ อีเมล์ ผู้ส่งจาก data.properties
		String[] account = getSender();
		// reciver มีชนิดเป็น int เก็บจำนวนหมอที่ยังไม่ถูกส่งจดหมาย
		int n_reciver = getNReciver();

		// จำนวนการส่งสูงสุดของ Sender
		int sent = getNSend();
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
						String mail_doctor = getDataReciver(n_sent, "EMAIL");
						// code_doctor เก็บโค้ดหมอจากฐานข้อมูลแต่ละคน
						String code_doctor = getDataReciver(n_sent, "DOCTOR_PROFILE_CODE");

						System.out.print(code_doctor + "\t" + mail_doctor + "\t:\n\t\t");

						// สร้างไฟล์ pdf > พร้อมส่ง
						create.createFilePDF(code_doctor, email[0], email[1], "springbootrecive@gmail.com");

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
		System.out.println("\nจำนวนผู้รับทั้งหมด 	:	" + getNReciver() + "	คน\n");
		System.out.println("จำนวนที่ส่งไปแล้ววันนี้	:	" + n_sent + "	คน\n");
		System.out.println("จำนวนที่จะต้องส่งในวันพรุ่งนี้	:	" + n_reciver + "	คน \n");
		return n_reciver;
	}

	public void sendmail(String mail, String pass, String reciver, String code_doctor,
			ByteArrayOutputStream get_jasperDocFile) throws AddressException, MessagingException, IOException {

		msg = new MimeMessage(getSession(mail, pass));
		// 2.ตั่งค่าชื่อผู้รับ
		msg.setFrom(new InternetAddress(reciver, false));
		// 3.ตั่งค่าชื่อผู้รับ
		msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(reciver));
		// 4.ตั่งค่าหัวข้อจดหมาย
		msg.setSubject("HeaderEmail");
		// 5. ตั้งค่าเวลาส่งmsg.setSentDate(new Date());

		// 6. ประกาศตัวแปร messageBodyPart เพื่อใส่ข้อความ
		BodyPart messageBodyPart = new MimeBodyPart();
		// 6.1. ใส่ข้อความ Content
		messageBodyPart.setContent("getContent", "text/html");
		// 6.2. ใส่ file pdf

		DataSource aAttachment = new ByteArrayDataSource(get_jasperDocFile.toByteArray(), "application/pdf");

		// 6.3. อ้างที่อยู่ file pdf
		messageBodyPart.setDataHandler(new DataHandler(aAttachment));
		messageBodyPart.setFileName("DF_Payment_Report.pdf");

		// 7. นำข้อความและfile pdf ใส่ลงใน Multipart
		Multipart multipart = new MimeMultipart();
		// 7.1 ใส่สิ่งของที่ต้องการลงใน addBodyPart
		multipart.addBodyPart(messageBodyPart);
		// 8.รวมใส่ msg
		msg.setContent(multipart);
		// 9. ส่ง
		Transport.send(msg);
		System.out.println("Send mail success...!!");

	}

}