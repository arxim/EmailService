package com.dao;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import com.util.DbConnector;
import com.util.Property;

public class DoctorDAO {
	private static ArrayList<HashMap<String, String>> listReciver = null;

	public static ArrayList<HashMap<String, String>> getReciver() throws SQLException, IOException {

		String hospitalCode = Property.getCenterProperty("/application.properties").getProperty("hospitalCode");
		String yyyy = Property.getCenterProperty("/application.properties").getProperty("yyyy");
		String mm = Property.getCenterProperty("/application.properties").getProperty("mm");
		// String mm = null;
		// String yyyy = null;
		try {
			// mm = BatchDao.getMonth(hospitalCode);
			// yyyy = BatchDao.getYear(hospitalCode);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// แสดงค่าที่ต้องการ
		listReciver = new ArrayList<>();
		PreparedStatement ps = null;
		String sql = "\r\n"
				+ "SELECT T1.HOSPITAL_CODE,T2.CODE AS DOCTOR_CODE, COALESCE(NULLIF(T2.EMAIL,''),'') EMAIL,\r\n"
				+ "COALESCE(NULLIF(T1.DR_SUM_AMT,'0.00'),'0.00') AS REVENUE_DETAIL, COALESCE(NULLIF(T1.EXDR_AMT+T1.EXCR_AMT,'0.00'),'0.00') AS EXPENSE, COALESCE(NULLIF(T1.DR_NET_PAID_AMT,'0.00'),'0.00') AS VOUCHER,\r\n"
				+ "COALESCE(NULLIF(T3.DR_AMTs,'0.00'),'0.00') AS UNPAID,\r\n"
				+ "CASE WHEN T2.LICENSE_ID = '' THEN T2.CODE ELSE T2.LICENSE_ID END AS PASS_ENCRYPT,\r\n"
				+ "T1.YYYY,T1.MM,B.BATCH_NO,T1.STATUS_MODIFY\r\n" + "FROM PAYMENT_MONTHLY T1\r\n" + "\r\n"
				+ "LEFT OUTER JOIN BATCH B ON T1.HOSPITAL_CODE = B.HOSPITAL_CODE AND CLOSE_DATE = ''\r\n"
				+ "LEFT OUTER JOIN DOCTOR T2 ON T1.HOSPITAL_CODE = T2.HOSPITAL_CODE AND T1.DOCTOR_CODE = T2.CODE\r\n"
				+ "LEFT OUTER JOIN \r\n" + "(\r\n"
				+ "SELECT DOCTOR_CODE,SUM(DR_AMT) AS DR_AMTs,YYYY,MM AS DR_AMT,HOSPITAL_CODE FROM TRN_DAILY WHERE HOSPITAL_CODE = ? AND YYYY = ? AND MM = ? GROUP BY DOCTOR_CODE,HOSPITAL_CODE,YYYY,MM\r\n"
				+ ") T3 ON T1.HOSPITAL_CODE = T3.HOSPITAL_CODE AND T1.DOCTOR_CODE = T3.DOCTOR_CODE \r\n" + "\r\n"
				+ "WHERE T1.HOSPITAL_CODE = ?\r\n" + "AND T1.YYYY = ?\r\n" + "AND T1.MM = ?\r\n"
				+ "AND T2.EMAIL != ''\r\n" + "AND (T1.STATUS_MODIFY = '' OR T1.STATUS_MODIFY  is null)\r\n"
				+ "Order by DOCTOR_CODE\r\n" + "\r\n" + "";
		try (java.sql.Connection conn = DbConnector.getDBConnection()) {
			// ต้องการเลือก row และ Colume ใชเ ArayList HashMap
			ps = conn.prepareStatement(sql);
			ps.setString(1, hospitalCode);
			ps.setString(2, yyyy);
			ps.setString(3, mm);
			ps.setString(4, hospitalCode);
			ps.setString(5, yyyy);
			ps.setString(6, mm);

			listReciver = DbConnector.convertArrayListHashMap(ps.executeQuery());
			// System.out.println("success getReciver() from DoctorDAO");

		} catch (Exception e) {
			System.out.println("fail getReciver() from DoctorDAO");
		} finally {
			if (ps != null) {
				ps.close();
			}
		}

		return listReciver;
	}



	// Stam code หมอ แต่ละคนว่าได้ส่งแล้วง
	public static void SendMailPaymentSuccess(String code_doctor) throws SQLException, IOException {
		String hospitalCode = Property.getCenterProperty("/application.properties").getProperty("hospitalCode");
		String yyyy = Property.getCenterProperty("/application.properties").getProperty("yyyy");
		String mm = Property.getCenterProperty("/application.properties").getProperty("mm");

		PreparedStatement ps = null;
		String SQL = "UPDATE PAYMENT_MONTHLY \r\n" + "		            SET \r\n"
				+ "		               STATUS_MODIFY = 'T' \r\n" + "		           WHERE DOCTOR_CODE = ?\r\n"
				+ "		            AND HOSPITAL_CODE = ?\r\n" + "		            AND MM = ?\r\n"
				+ "		            AND YYYY = ? \r\n" + "";
		try (java.sql.Connection conn = DbConnector.getDBConnection()) {

			ps = conn.prepareStatement(SQL);
			ps.setString(1, code_doctor);
			ps.setString(2, hospitalCode);
			ps.setString(3, mm);
			ps.setString(4, yyyy);

			ps.executeQuery();
			System.out.println("success SendMailPaymentSuccess() from DoctorDAO");

		} catch (Exception e) {

		} finally {
			if (ps != null)
				ps.close();
		}
	}

}
