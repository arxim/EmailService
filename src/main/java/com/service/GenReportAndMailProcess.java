package com.service;

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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;
import org.springframework.stereotype.Component;

import com.ibm.icu.text.SimpleDateFormat;
import com.dao.DoctorDAO;
import com.util.Property;
import com.service.CreatePDFService;
import com.service.SendmailService;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;

@Component
public class GenReportAndMailProcess {
	// Test
	int n_re;
	int day = 15;
	String key = "open";

	// กระตุ้นงานด้วย Scheduled anotation ->method start() ทำงาน -> เกิดJob[1]
	@Scheduled(cron = "0 59 1 15 * ?")
	public void start()
			throws ClassNotFoundException, NoSuchMethodException, SchedulerException, ParseException, SQLException {

		// เก็บจำนวนทั้งหมดตอนเริ่มต้น เพื่อให้ cron ทำงานในวันแรก
		if (key.equals("open")) {
			n_re = DoctorDAO.getNReciver();
		}

		// change cron
		// กรณีพบว่า ต้องทำงานเพิ่ม เปลี่ยน cron
		if (n_re > 0) {
			// Job[1] คืนค่า > 0 (ยังเหลือผู้รับ) -> doSetTimeScherdule วันถัดไป -> Job[2]
			System.out.println("\n\n1.It has begun...!");
			System.out.println("\nDo Day >>> " + day + "\n");

			deleteScheduler();
			setSchedule("0 0 2 " + day + " * ?");

		}
		if (n_re == 0) {
			// เปลี่ยนกุญแจ เป็นเปิดเมื่อ ทำจนเสร็จ รอวันถัดไป เพื่อรอรค่าหมอทั้งหมด
			System.out.println("\n3.-------------------End--------------------");
			deleteScheduler();
			// เปิด key เมื่อ job ทำงานเสร็จ
			day = 15;
			key = "open";
		}

	}

	// method สร้าง job detail
	private static JobDetail createJob(GenReportAndMailProcess worker, String identity)
			throws ClassNotFoundException, NoSuchMethodException {

		MethodInvokingJobDetailFactoryBean jobDetail = new MethodInvokingJobDetailFactoryBean();

		// set object งานลงไป
		jobDetail.setTargetObject(worker);

		// บอกว่าจะให้ทำที่ method ไหน ของ object นั้น ในที่นี้คือให้ทำที่ method
		// loppSend
		jobDetail.setTargetMethod("loopSend");

		// ระบุคีย์ โดยจะต้องไม่ซ้ำกัน
		jobDetail.setName(identity);
		jobDetail.setConcurrent(false);
		jobDetail.afterPropertiesSet();
		return jobDetail.getObject();

	}

	// method สร้าง trigger
	private static Trigger createTrigger(String identity, String cronExp) throws ParseException {
		// ตั้งเวลา จากตัวอย่างคือ ทุกๆ 5 วินาที
		CronExpression cx = new CronExpression(cronExp);
		CronScheduleBuilder atHourAndMinuteOnGivenDaysOfWeek = CronScheduleBuilder.cronSchedule(cx);
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

		// start งานนั้น ให้เริ่มทำงาน
		sched.start();

	}

	// ฟังชัน ลบงานใน Schdule
	public void deleteScheduler() throws SchedulerException {

		/*
		 * SchedulerFactory sf = new StdSchedulerFactory(); Scheduler sched =
		 * sf.getScheduler();
		 */
		try {
			SchedulerFactory sf = new StdSchedulerFactory();
			Scheduler sched = sf.getScheduler();

			JobKey jobkey = new JobKey("workJob1");
			sched.deleteJob(jobkey); // ลบ job นั้นๆ

		} catch (SchedulerException ex) {

		}

		System.out.println("Delete Job Key OK !!!");

	}

	// Test
	int n_reciver;
	int sent;

	// อังกอริทึมสำหรับวนลูปส่งเมล์ทีละ ตามจำนวนสูงสุดของผู้ส่ง
	public void loopSend() throws JRException, IOException, MessagingException, SQLException, ClassNotFoundException,
			NoSuchMethodException, SchedulerException, ParseException {

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		Date now = new Date();
		String strDate = sdf.format(now);
		System.out.println("\nJava cron job expression:: " + strDate + "\n");

		// account เก็บ อีเมล์ ผู้ส่งจาก data.properties
		String[] account = Property.getCenterProperty("/application.properties").getProperty("sendersMail").split(",");
		// reciver มีชนิดเป็น int เก็บจำนวนหมอที่ยังไม่ถูกส่งจดหมาย
		n_reciver = DoctorDAO.getNReciver();

		// จำนวนการส่งสูงสุดของ Sender
		sent = Integer.parseInt(Property.getCenterProperty("/application.properties").getProperty("Nsend"));
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
						String[] jasperFiles = Property.getCenterProperty("/application.properties")
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
							// userReciverMail.add(mail_doctor);
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
		// ผลลัพธ์สุดท้าย
		int n_re2 = n_reciver;

		if (n_re2 > 0) {
			System.out.println("2.Continueu");
		}

		// n_re = 800;
		// สร้าง key = close ปิดkey
		key = "close";
		n_re = n_re2;
		day = day + 1;
		// กลับมายัง method start() -> check ว่า n_re =0 ?
		start();
	}

}
