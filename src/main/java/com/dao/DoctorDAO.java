package com.dao;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import com.util.DbConnector;
import com.util.Property;

public class DoctorDAO {
	static ArrayList<HashMap<String, String>> listReciver = null;
	static ArrayList<HashMap<String, String>> checkFile = null;

	// ------------------------------------------------------------------------------------
	// Copy Code from vtnjar
	// get Email and Doctor code from mm,yyyy,hospital code
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
		String sql = "SELECT T1.DOCTOR_CODE, " + "       T2.NAME_THAI, " + "       T1.STATUS_MODIFY,"
				+ "       T2.EMAIL ," + "       T1.MM," + "       T1.YYYY " + "FROM PAYMENT_MONTHLY T1 "
				+ "     LEFT JOIN DOCTOR T2 ON T1.HOSPITAL_CODE = T2.HOSPITAL_CODE "
				+ "                            AND T1.DOCTOR_CODE = T2.CODE " + "WHERE T1.YYYY = ? "
				+ "      AND T1.MM = ? " + "      AND (T1.STATUS_MODIFY = '' "
				+ "           OR T1.STATUS_MODIFY IS NULL) " + "      AND T1.HOSPITAL_CODE = ? "
				+ "      AND DR_NET_PAID_AMT > 0 " + "      AND DOCTOR_CODE IN " + "( " + "    SELECT CODE "
				+ "    FROM DOCTOR " + "    WHERE EMAIL <> '' " + ")";
		try (java.sql.Connection conn = DbConnector.getDBConnection()) {
			// ต้องการเลือก row และ Colume ใชเ ArayList HashMap
			ps = conn.prepareStatement(sql);
			ps.setString(1, yyyy);
			ps.setString(2, mm);
			ps.setString(3, hospitalCode);

			
			listReciver = DbConnector.convertArrayListHashMap(ps.executeQuery());
			//System.out.println("success getReciver() from DoctorDAO");

		} catch (Exception e) {
			System.out.println("fail getReciver() from DoctorDAO");
		} finally {
			if (ps != null) {
				ps.close();
			}
		}

		return listReciver;
	}

	public static int getNReciver() throws SQLException, IOException {

		// return 1;//test
		return getReciver().size();
	}

	// get password will encrytion from mm,yyyy,hospital_code,doctor_code :)
	public static ArrayList<HashMap<String, String>> getPassEncryt(String code_doctor)
			throws SQLException, IOException {

		String hospitalCode = Property.getCenterProperty("/application.properties").getProperty("hospitalCode");
		String yyyy = Property.getCenterProperty("/application.properties").getProperty("yyyy");
		String mm = Property.getCenterProperty("/application.properties").getProperty("mm");
		// แสดงค่าที่ต้องการ
		listReciver = new ArrayList<>();
		PreparedStatement ps = null;
		String sql = "SELECT T1.HOSPITAL_CODE, \r\n" + "		                  T2.CODE AS DOCTOR_CODE, \r\n"
				+ "		                  COALESCE(NULLIF(T2.EMAIL,''),'0') EMAIL, \r\n"
				+ "		                  CASE \r\n" + "		                      WHEN T2.LICENSE_ID = '' \r\n"
				+ "		                      THEN T2.CODE \r\n"
				+ "		                      ELSE T2.LICENSE_ID\r\n"
				+ "		                  END AS PASS_ENCRYPT, \r\n" + "		                  T1.YYYY, \r\n"
				+ "		                 T1.MM, \r\n" + "		                  T1.STATUS_MODIFY \r\n"
				+ "		           FROM PAYMENT_MONTHLY T1 \r\n"
				+ "		                LEFT JOIN DOCTOR T2 ON T1.HOSPITAL_CODE = T2.HOSPITAL_CODE \r\n"
				+ "		                                       AND T1.DOCTOR_CODE = T2.CODE \r\n"
				+ "		           WHERE T1.HOSPITAL_CODE = ? \r\n" + "		           		AND T2.CODE = ?\r\n"
				+ "		           		AND T1.YYYY = ? \r\n" + "		                 AND T1.MM = ?\r\n"
				+ "		                 AND (T1.STATUS_MODIFY = '' OR T1.STATUS_MODIFY IS NULL); ";
		try (java.sql.Connection conn = DbConnector.getDBConnection()) {
			// ต้องการเลือก row และ Colume ใชเ ArayList HashMap
			ps = conn.prepareStatement(sql);
			ps.setString(1, hospitalCode);
			ps.setString(2, code_doctor);
			ps.setString(3, yyyy);
			ps.setString(4, mm);

			// ps.setString(1, doctorCode);
			listReciver = DbConnector.convertArrayListHashMap(ps.executeQuery());
			System.out.println("success getPassEncryt() from DoctorDAO");
		} catch (Exception e) {
			// TODO: handle exception
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
