package com.service;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TreeMap;
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
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;
import com.dao.DoctorDAO;
import com.ibm.icu.text.SimpleDateFormat;
import com.util.Property;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;

public class GenReportAndMailProcess {

	private static CreatePDFService createPDFService = new CreatePDFService();

	private static int n_sent = 0;// นับจำนวนที่ถูกส่งไปแล้ว เริ่มต้นยังไม่ถูกส่ง sened = 0
	static int n_re;
	static int day = 15;
	static int sec = 0;
	static int min = 0;
	// int day = 1;// Test
	static String key = "open";
	static SimpleDateFormat sdf = null;
	static Date now = null;
	static String strDate = "";
	static ArrayList<HashMap<String, String>> list = null;

	public static void main(String status) throws IOException {

		if (status.equals("true")) {
			// เก็บจำนวนทั้งหมดตอนเริ่มต้น เพื่อให้ cron ทำงานในวันแรก
			if (key.equals("open")) {
				System.out.println("\n----------------Spring send mail service...----------------");

				list = new ArrayList<HashMap<String, String>>();
				try {
					list = DoctorDAO.getReciver();
				} catch (SQLException e2) {
					// TODO Auto-generated catch block
					System.out.println("fail get number docter from method main");
				}

				n_re = list.size();

			}
			// change cron
			// กรณีพบว่า ต้องทำงานเพิ่ม เปลี่ยน cron
			if (n_re > 0) {
				// Job[1] คืนค่า > 0 (ยังเหลือผู้รับ) -> doSetTimeScherdule วันถัดไป -> Job[2]

				System.out.println("\n\nTotal reciver	:	" + n_re + "	person");
				System.out.println("\nProgram has beginning...Day " + day + " min " + min + " sec " + sec);

				try {
					deleteScheduler();
					setSchedule(sec + " " + min + " 2 " + day + " * ?");

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
					// เปิด key เมื่อ job ทำงานเสร็จ
					day = 15;
					n_sent = 0;
					key = "open";

				} catch (SchedulerException e) {

					System.out.println("fail condition 'n_re == 0' from method start() !");
				}
			} 

		}else {
			System.out.print("not equal 'true' ...");
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

	public static void setSchedule(String cronExp)
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
	public static void deleteScheduler() throws SchedulerException {

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

	public static String getDate() {
		sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		now = new Date();
		strDate = sdf.format(now);
		System.out.println("\nJava cron job expression: " + strDate + "\n");
		return strDate;
	}

	public static int getNumSender() {
		// จำนวนการส่งสูงสุดของ Sender
		int sent = 0;
		try {
			sent = Integer.parseInt(Property.getCenterProperty("/application.properties").getProperty("Nsend"));
			System.out.println("success get max sender from method loopSend()...");
		} catch (NumberFormatException | IOException e1) {

			System.out.println("fail get max sender from method loopSend() !");
		}
		return sent;

	}

	public static void main(String[] arg) {

		try {
			main("true");

		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	static int n_reciver;

	public static void loopSend() throws IOException, SQLException {

		// Test
		System.out.println("\n\nloopDoctor : " + (n_sent + 1) + "\n\n");
		// -----------------------------------------------------------
		// รับอีเทล์ sender
		Properties propSender = Property.getProp("sendersMail");
		// สร้างตัวแปรสำหรับ sort เรียงลำดับผู้ส่ง
		TreeMap<Object, Object> mapTreeSender = new TreeMap<Object, Object>(propSender);
		// เข้าถึง key กับ value ด้วย iterator
		String keys = "";
		String values = "";
		String mail_doctor = "";
		String code_doctor = "";
		String password_doctor = "";
		String[] email;

		// รับจำนวนผู้รับ
		// int n_reciver = getNumRecive();
		if (key.equals("open")) {
			n_reciver = list.size();
		}

		System.out.println(n_reciver);
		// รับจำนวนการส่ง
		int sent = getNumSender();

		List<JasperPrint> listJasper = null;

		// กรณีไม่มีผู้รับในฐานข้อมูล
		if (n_reciver != 0) {

			// หาแอคเค้าผู้ส่ง
			for (Iterator<Object> itSender = mapTreeSender.keySet().iterator(); itSender.hasNext();) {

				if (n_reciver != 0) {
					// key เก็บ คีย์ที่เรียกของตัวนั้นๆ
					keys = (String) itSender.next();
					// value เก็บ อีเมล์ไอดี และพาสเวิส
					values = propSender.getProperty(keys);
					System.out.println("Key :" + keys + " value :" + values);
					// สปลิสด้วย ","
					email = values.split(",");
					System.out.println("Sender e-mail using ID,password < " + email[0] + " , " + email[1] + " >\n");

					// วนส่งตามจำนวนสูงสุดของผู้ส่ง
					for (int j = 0; j < sent; j++) {

						// กรณียังส่งไม่หมดให้ส่งต่อไป จนกว่าจะส่งครบ ให้หลุดจากลูป
						if (n_reciver != 0) {
							System.out.print("	(" + (n_sent + 1) + ") ");

							// mail_doctor เก็บเมล์หมอแต่ละคน
							mail_doctor = list.get(n_sent).get("EMAIL");
							// code_doctor เก็บโค้ดหมอจากฐานข้อมูลแต่ละคน
							code_doctor = list.get(n_sent).get("DOCTOR_CODE");
							// pass_encrytDoctor เก็บรหัสผ่านไฟล์ pdf
							// password_doctor = list.get(n_sent).get("PASS_ENCRYPT");
							password_doctor = "1234";

							System.out.println("\nEmail:\n\t" + mail_doctor + "\nDoctor code:\n\t" + code_doctor
									+ "\nPassword doctor:\n\t" + password_doctor);

							// เรียงลำดับข้อมูล ไฟล์ jasperตามสิ่งที่ตั้งค่าไว้ใน properties
							// construct > เก็บไฟล์ที่หมอจะต้องทำ ไว้ใน list
							Properties propJasper = Property.getProp("jasperFiles");
							TreeMap<Object, Object> mapTreeJasper = new TreeMap<Object, Object>(propJasper);
							// ประกาศ listJasper เก็บไฟล์ jasper ที่มีการ put parameter เรียบร้อยแล้ว
							listJasper = new ArrayList<JasperPrint>();

							for (Iterator<Object> itJasper = mapTreeJasper.keySet().iterator(); itJasper.hasNext();) {

								String keyJasper = (String) itJasper.next();
								// เก็บชื่ไฟล์ jasper ทีละไฟล์
								String valueJasperFileName = propJasper.getProperty(keyJasper);
								System.out.println("Jasper : Key " + keyJasper + " value " + valueJasperFileName);

								// ถ้าคนนัน้มีข้อมูลให้ put ค่า แล้วเก็บใน list แล้วนำไป ส่ง
								if (!(CreatePDFService.getNRowReport(valueJasperFileName, n_sent, list)
										.equals("0.00"))) {
									// เก็บ JasperPrint ลงใน listJasper จากการส่งไป ยังฟังก์ชัน getInJasperFile()
									// เพื่อ map parameter แล้ว
									System.out.println("SubString : "
											+ valueJasperFileName.substring(0, valueJasperFileName.length() - 7));

									// Substring เริ่มต้นที่ index 0 จบที่ ชื่อไฟล์ ไม่ติดนามสกุล .jasper ออกมา , -7
									// คือ จำนวนอักขระของคำว่า '.jasper'
									try {
										listJasper.add(createPDFService.getInJasperFile(
												valueJasperFileName.substring(0, valueJasperFileName.length() - 7),
												code_doctor));
									} catch (JRException | SQLException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}

								}

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
										System.out
												.println("success stam reciver from method loopSend()...Doctor code is "
														+ code_doctor);
									}

								} catch (SQLException | IOException e) {

									System.out.println("fail sql exception from method loopSend()");
								}
							}

							System.out.println(listJasper);
							n_sent++;
							n_reciver--;

						} else {
							// n_reciver == 0
							// เมื่อ ส่งหมด โดยไม่ครบ max sender นั้นๆ ให้หลุดจาก loop
							System.out.println("End for loop method loopSend()...");
							break;
						}

					}

				} else {
					System.out.println("end  data...");
					break;
				}
			}

		} else {
			System.out.println("no data...");
		}

		System.out.println("*-*-*-**-*-*-**-*-*-**-*-*-**-*-*-**-*-*-**-*-*-**-*-*-**-*-*-**-*-*-*\n");
		System.out.println("total reciver	:	" + list.size() + "	person  " + "" + "\n");
		System.out.println("send success	:	" + n_sent + "	person  " + "" + "\n");
		System.out.println("send tomorrow	:	" + n_reciver + "	person \n");
		System.out.println("*-*-*-**-*-*-**-*-*-**-*-*-**-*-*-**-*-*-**-*-*-**-*-*-**-*-*-**-*-*-*");

		// -----------------------------------------------------------

		n_re = n_reciver;
		if (n_re > 0) {
			System.out.println("\n\ncontinue...\n\n");
		}
		// day = day + 1;
		min = min + 2;
		key = "false";

		main("true");

	}

}
