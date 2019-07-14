package com.service;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.mail.MessagingException;

import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;
import org.springframework.stereotype.Component;

import com.ibm.icu.text.SimpleDateFormat;
import com.dao.DoctorDAO;
import com.util.Property;
import com.service.CreatePDFService;
import com.service.SendmailService;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

@Component // ระบุ ต่อจาก@ComponentScan(basePackages = "com")
public class GenReportAndMailProcess {

	private CreatePDFService createPDFService = new CreatePDFService();
	private SendmailService sendmailService = new SendmailService();

	int n_re;
	int day = 15;
	int sec = 10;
	// int day = 1;// Test
	String key = "open";

	// กระตุ้นงานด้วย Scheduled anotation ->method start() ทำงาน -> เกิดJob[1]
	// @Scheduled(cron = "0 0/7 2 15 * ?")//Test
	@Scheduled(cron = "0 0 2 15 * ?")
	public void start() {

		// เก็บจำนวนทั้งหมดตอนเริ่มต้น เพื่อให้ cron ทำงานในวันแรก
		if (key.equals("open")) {
			try {
				System.out.println("\n-----------Spring send mail service...-----------");
				n_re = DoctorDAO.getNReciver();

			} catch (SQLException | IOException e) {
				System.out.println("fail method start() !");
			}

		}

		// change cron
		// กรณีพบว่า ต้องทำงานเพิ่ม เปลี่ยน cron
		if (n_re > 0) {
			// Job[1] คืนค่า > 0 (ยังเหลือผู้รับ) -> doSetTimeScherdule วันถัดไป -> Job[2]
			System.out.println("\n\nTotal reciver	:	" + n_re + "	person");
			System.out.println("\nProgram has beginning....it cooldown '" + sec + "' minute do day '" + day + "'... ");

			try {
				deleteScheduler();
				// setSchedule("0 " + day + " 2 15 * ?");// Test
				setSchedule(sec + " 0 2 " + day + " * ?");

			} catch (ClassNotFoundException | NoSuchMethodException | SchedulerException | ParseException e) {
				// TODO Auto-generated catch block
				System.out.println("fail condition 'n_re > 0' from method start() !");
			}

		}
		if (n_re == 0) {
			// เปลี่ยนกุญแจ เป็นเปิดเมื่อ ทำจนเสร็จ รอวันถัดไป เพื่อรอรค่าหมอทั้งหมด
			System.out.println("\n*****************send mail success this month*****************\n\n");
			try {
				deleteScheduler();

			} catch (SchedulerException e) {

				System.out.println("fail condition 'n_re == 0' from method start() !");
			}
			// เปิด key เมื่อ job ทำงานเสร็จ
			// day = 1;// Test
			day = 15;
			key = "open";
		}

	}

	// method สร้าง job detail
	private static JobDetail createJob(GenReportAndMailProcess worker, String identity) {

		MethodInvokingJobDetailFactoryBean jobDetail = new MethodInvokingJobDetailFactoryBean();

		// set object งานลงไป
		jobDetail.setTargetObject(worker);

		// บอกว่าจะให้ทำที่ method ไหน ของ object นั้น ในที่นี้คือให้ทำที่ method
		// loppSend
		jobDetail.setTargetMethod("loopSend");

		// ระบุคีย์ โดยจะต้องไม่ซ้ำกัน
		jobDetail.setName(identity);
		jobDetail.setConcurrent(false);

		try {
			jobDetail.afterPropertiesSet();
			System.out.println("success createjob method createJob()...");
		} catch (ClassNotFoundException | NoSuchMethodException e) {

			System.out.println("fail createjob method createJob() !");
		}

		return jobDetail.getObject();

	}

	// method สร้าง trigger
	private static Trigger createTrigger(String identity, String cronExp) {
		// ตั้งเวลา จากตัวอย่างคือ ทุกๆ 5 วินาที
		CronExpression cx = null;
		try {
			cx = new CronExpression(cronExp);
		} catch (ParseException e) {
			System.out.println("fail cron expression method createTrigger() !");
		}
		CronScheduleBuilder atHourAndMinuteOnGivenDaysOfWeek = CronScheduleBuilder.cronSchedule(cx);
		System.out.println("success create trigger method createTrigger()...");

		return TriggerBuilder.newTrigger().withSchedule(atHourAndMinuteOnGivenDaysOfWeek) // set เวลา
				.withIdentity(identity) // ระบุคีย์ trigger โดยจะต้องไม่ซ้ำกัน
				.startNow().build();
	}

	public void setSchedule(String cronExp)
			throws SchedulerException, ClassNotFoundException, NoSuchMethodException, ParseException {

		// get schedule
		SchedulerFactory sf = new StdSchedulerFactory();
		Scheduler sched = sf.getScheduler();

		sched = sf.getScheduler();

		// สร้าง object หรือ งานที่จะให้ ทำตาม scheduler
		GenReportAndMailProcess worker = new GenReportAndMailProcess();

		// สร้าง job detail เพื่อบอกรายละเอียดงานที่จะให้ทำ พร้อมระบุ id ของ job นั้น
		// โดยจะต้องมี id ไม่ซ้ำกันด้วย
		JobDetail job = createJob(worker, "workJob1");

		// สร้าง trigger คือตัวตั้งเวลาการทำงาน ว่าจะให้ทำตอนไหน พร้อมระบุ id ของ
		// trigger นั้น
		Trigger trigger = createTrigger("workTrigger1", cronExp);

		// นำ job และ trigger ยัดลงใน schedule
		sched.scheduleJob(job, trigger);

		System.out.println("success schedulejob start...");
		// start งานนั้น ให้เริ่มทำงาน
		sched.start();

	}

	// ฟังชัน ลบงานใน Schdule
	public void deleteScheduler() throws SchedulerException {

		try {
			SchedulerFactory sf = new StdSchedulerFactory();
			Scheduler sched = sf.getScheduler();

			JobKey jobkey = new JobKey("workJob1");
			sched.deleteJob(jobkey); // ลบ job นั้นๆ
			System.out.println("success delete job key...");

		} catch (SchedulerException ex) {
			System.out.println("fail delete job key !");
		}

	}

	// อังกอริทึมสำหรับวนลูปส่งเมล์ทีละ ตามจำนวนสูงสุดของผู้ส่ง
	public void loopSend() {

		// set variable
		int sent = 0;
		int n_reciver = 0;
		int n_sent = 0;// นับจำนวนที่ถูกส่งไปแล้ว เริ่มต้นยังไม่ถูกส่ง sened = 0

		String mail_doctor = "";
		String code_doctor = "";
		String password_doctor = "";
		String strDate = "";
		String pass = "true";

		String[] account = null;
		String[] jasperFiles = null;

		SimpleDateFormat sdf = null;

		Date now = null;

		List<JasperPrint> listJasper = null;

		// process
		sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		now = new Date();
		strDate = sdf.format(now);
		System.out.println("\nJava cron job expression: " + strDate + "\n");

		// account เก็บ อีเมล์ ผู้ส่งจาก data.properties
		try {
			account = Property.getCenterProperty("/application.properties").getProperty("sendersMail").split(",");
			System.out.println("success get account from method loopSend()...");
		} catch (IOException e1) {

			System.out.println("fail get account from method loopSend() !");
		}

		// reciver มีชนิดเป็น int เก็บจำนวนหมอที่ยังไม่ถูกส่งจดหมาย
		try {
			n_reciver = DoctorDAO.getNReciver();
			System.out.println("success get number reciver from method loopSend()...");

		} catch (SQLException | IOException e1) {

			System.out.println("fail get number reciver from method loopSend() !");
		}

		// จำนวนการส่งสูงสุดของ Sender
		try {
			sent = Integer.parseInt(Property.getCenterProperty("/application.properties").getProperty("Nsend"));
			System.out.println("success get max sender from method loopSend()...");
		} catch (NumberFormatException | IOException e1) {

			System.out.println("fail get max sender from method loopSend() !");
		}

		// เริ่มต้นวนแอคเค๊าที่จะใช้ส่ง i เริ่มต้นที่ 0 หมายถึง ใช้ อีเมล์ index ที่ 0
		// ใน account ที่มีชนิดข้อมูลเป็น array string
		for (int i = 0; i < account.length; i++) {
			// กรณีไม่มีผู้รับ ให้หลุดจากลูป
			if (n_reciver != 0) { //
				// แยกอีเมล์กับพาสเวิสออกจากกัน index = 0 เก็บ ชื่อ e-mail , index = 1 เก็บ
				// รหัสผ่านอีเมล์
				String[] email = account[i].split("&");
				System.out.println((i + 1) + ". Sending emails using < '" + email[0] + "' , '" + email[1] + "' >");
				// วนส่งเท่ากับ จำนวนสูงสุดที่ใช้ในการส่ง
				for (int j = 0; j < sent; j++) {
					// กรณียังส่งไม่หมดให้ส่งต่อไป จนกว่าจะส่งครบ ให้หลุดจากลูป
					if (n_reciver != 0) {
						System.out.print("	(" + (n_sent + 1) + ") ");

						try {
							// mail_doctor เก็บเมล์หมอแต่ละคน
							try {
								mail_doctor = DoctorDAO.getReciver().get(n_sent).get("EMAIL").toString();
								System.out.println("success get mail reciver from method loopSend()...");

							} catch (IOException e) {

								System.out.println("fail get mail reciver from method loopSend() !");
							}

							// code_doctor เก็บโค้ดหมอจากฐานข้อมูลแต่ละคน
							try {
								code_doctor = DoctorDAO.getReciver().get(n_sent).get("DOCTOR_CODE").toString();
								System.out.println("success get code reciver from method loopSend()...");
							} catch (IOException e) {

								System.out.println("fail get code reciver from method loopSend() !");
							}

							try {
								password_doctor = DoctorDAO.getPassEncryt(code_doctor).get(0).get("PASS_ENCRYPT")
										.toString();
								System.out.println("success get password reciver from method loopSend()...");
							} catch (IOException e) {
								System.out.println("fail get password reciver from method loopSend() !");
							}

							System.out.println("\nEmail:\n\t" + mail_doctor + "\nDoctor code:\n\t" + code_doctor
									+ "\nPassword doctor:\n\t" + password_doctor);

						} catch (SQLException e) {

							System.out.println("fail sql exception from method loopSend() !");
						}

						// jasperFiles เก็บไฟล์ jasper เป็น string array
						try {
							jasperFiles = Property.getCenterProperty("/application.properties")
									.getProperty("jasperFiles").split(",");
						} catch (IOException e) {

							System.out.println("fail get jasper file from method loopSend() !");
						}

						// เก็บชื่อไฟล์ที่หมอจะต้องทำไว้ใน list
						listJasper = new ArrayList<JasperPrint>();
						createPDFService = new CreatePDFService();

						// รับชื่อไฟล์ จาก ไฟล์ jasper ทั้งหมด
						for (String jasperFile : jasperFiles) {

							// เงื่อนไข ตรวจสอบก่อนทุกครั้งว่า ไฟล์เจสเปอนี้ หมอมีขข้อมูลไหม ?
							// ถ้ามี row > 0 ให้ ทำการ put ข้อมูล เพื่อสร้างไฟลฺ pdf
							// ส่ง ชื่อไฟล์และรหัสหมอไปทำ การตรวจสอบ
							// เก็บชื่อไฟล์ที่จะต้องทำไว้ใน list
							try {
								if (CreatePDFService.getNRowReport(jasperFile, code_doctor) > 0) {
									// เก็บ JasperPrint ลงใน listJasper จากการส่งไป ยังฟังก์ชัน getInJasperFile()
									// เพื่อ map parameter แล้ว
									listJasper.add(createPDFService.getInJasperFile(jasperFile, code_doctor));
								}
							} catch (SQLException | IOException | JRException e) {
								System.out.println(
										"fail for loop 'get number use jasper file or put parameter in jasper file' from method loopSend() !");
							}
						}

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
								sendmailService.sendmail(email[0], email[1], userReciverMail,
										createPDFService.createFilePDF(listJasper, password_doctor));

								pass = "true";

							} catch (MessagingException | IOException e1) {
								pass = "false";
								System.out.println("fail send mail from method loopSend() !");
							}

							try {

								if (pass.equals("true")) {
									DoctorDAO.SendMailPaymentSuccess(code_doctor);
									System.out.println("success stam reciver from method loopSend()...");
								}

							} catch (SQLException | IOException e) {

								System.out.println("fail sql exception from method loopSend()");
							}
						}

						n_sent++;
						n_reciver--;

					} else {
						// n_reciver == 0
						// เมื่อ ส่งหมด โดยไม่ครบ max sender นั้นๆ ให้หลุดจาก loop
						break;
					}

				}
				System.out.print("\n\n");

			} else {
				// n_reciver == 0
				// กรณีไม่มี ผู้รับตั้งแต่เริ่มต้น
				break;
			}

		}

		System.out.println("send success	:	" + n_sent + "	person\n");
		System.out.println("send tomorrow	:	" + n_reciver + "	person \n");
		// ผลลัพธ์สุดท้าย

		if (n_reciver > 0) {
			System.out.println("To be con.. next Day");

		}
		// สร้าง key = close ปิดkey
		key = "close";
		// เพิ่มวันถัดไป
		day = day + 1;
		// รับจำนวนปัจจุบัน เพื่อให้ method start() ตรวยสอบว่ายังมีคนอีกไหม ? ->
		// มีให้ทำต่อ : ไม่มี รอให้ anotation Schdule กระตุ้นทำเดือนถัดไป
		n_re = n_reciver;

		// กลับมายัง method start() -> check ว่า n_re =0 ?
		start();
	}

}
