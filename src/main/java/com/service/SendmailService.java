//mailService
package com.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
import com.util.Property;

public class SendmailService {

	static Message msg = null;

	public static Properties getConfigSMTP() throws IOException {

		String auth = Property.getCenterProperty("/property/application.properties").getProperty("mail.smtp.auth");
		String enable = Property.getCenterProperty("/property/application.properties")
				.getProperty("mail.smtp.starttls.enable");
		String host = Property.getCenterProperty("/property/application.properties").getProperty("mail.smtp.host");
		String port = Property.getCenterProperty("/property/application.properties").getProperty("mail.smtp.port");

		// config gmail SMTP
		Properties props = new Properties();
		props.put("mail.smtp.auth", auth);
		props.put("mail.smtp.starttls.enable", enable);
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.port", port);
		return props;
	}

	public static Session getSession(String userSenderMail, String passwordSenderMail) throws IOException {

		// รับชื่อผู้ใช้ อีเมล์,รหัสผ่านอีเมล์ เข้าถึงเมล์ผู้ใช้
		Session session = Session.getInstance(getConfigSMTP(), new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(userSenderMail, passwordSenderMail);
			}
		});

		return session;
	}

	public void sendmail(String userSenderMail, String passwordSenderMail, List<String> userReciverMail,
			ByteArrayOutputStream get_jasperDocFile) throws AddressException, MessagingException, IOException {

		msg = new MimeMessage(getSession(userSenderMail, passwordSenderMail));

		// send to Multiple recipients .-

		InternetAddress sentFrom = new InternetAddress(userSenderMail);
		msg.setFrom(sentFrom); // Set the sender address

		List<String> list = userReciverMail;

		InternetAddress[] sendTo = new InternetAddress[list.size()];
		for (int i = 0; i < list.size(); i++) {
			System.out.println("Send to:" + list.get(i));
			sendTo[i] = new InternetAddress(list.get(i));
		}

		msg.setRecipients(javax.mail.internet.MimeMessage.RecipientType.TO, sendTo);

		// end .-

		// 2.ตั่งค่าชื่อผู้รับ
		// msg.setFrom(new InternetAddress(userSenderMail, false));
		// 3.ตั่งค่าชื่อผู้รับ
		// msg.setRecipients(Message.RecipientType.TO,
		// InternetAddress.parse(userReciverMail));
		// 4.ตั่งค่าหัวข้อจดหมาย
		msg.setSubject("SpringBootSendMail");
		// 5. ตั้งค่าเวลาส่งmsg.setSentDate(new Date());

		// 7. นำข้อความและfile pdf ใส่ลงใน Multipart
		Multipart multipart = new MimeMultipart();

		// PLAIN TEXT
		BodyPart messageBodyPart = new MimeBodyPart();
		messageBodyPart.setText("Here is your plain text message");
		multipart.addBodyPart(messageBodyPart);

		// HTML TEXT
		messageBodyPart = new MimeBodyPart();
		String htmlText = "<h1 style=\"color:blue;\">This is a Blue Heading</h1>";
		messageBodyPart.setContent(htmlText, "text/html;  charset=\"utf-8\"");
		multipart.addBodyPart(messageBodyPart);

		// PDF Part
		// 6.2. ใส่ file pdf
		messageBodyPart = new MimeBodyPart();
		DataSource aAttachment = new ByteArrayDataSource(get_jasperDocFile.toByteArray(), "application/pdf");
		messageBodyPart.setDataHandler(new DataHandler(aAttachment));
		messageBodyPart.setFileName("DF_Payment_Report.pdf");
		multipart.addBodyPart(messageBodyPart);

		// 8.รวมใส่ msg
		msg.setContent(multipart);
		// 9. ส่ง
		Transport.send(msg);
		System.out.println("Send mail success...!!");

	}

}