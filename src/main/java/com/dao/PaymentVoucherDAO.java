package com.dao;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import com.util.DbConnector;
import com.util.Property;

public class PaymentVoucherDAO {

	static ArrayList<HashMap<String, String>> checkFile = null;

	public static ArrayList<HashMap<String, String>> getPaymentVoucher(String code_doctor)
			throws IOException, SQLException {

		String from_doctor = code_doctor;
		String to_doctor = code_doctor;
		String yyyy = Property.getCenterProperty("/application.properties").getProperty("yyyy");
		String mm = Property.getCenterProperty("/application.properties").getProperty("mm");

		PreparedStatement ps = null;
		String sql = "SELECT PM.YYYY,PM.MM, PM.TRANSACTION_DATE, PM.SUM_AMT, PM.DR_SUM_AMT, PM.HP_SUM_AMT,\r\n" + 
				"PM.EXDR_AMT, PM.EXCR_AMT * -1 AS EXCR_AMT, PM.GDR_AMT, PM.GCR_AMT, PM.ABCR_AMT, PM.REMARK,\r\n" + 
				"PM.DR_NET_PAID_AMT, PM.DOCTOR_CODE, DR.NAME_THAI, PM.REF_PAID_NO, PM.PAYMENT_TERM_DATE AS 'PAY_DATE',\r\n" + 
				"PM.DR_SUM_AMT+EXDR_AMT+GDR_406+GDR_402 AS REVENUE, BK.DESCRIPTION_ENG AS BANK_NAME,\r\n" + 
				"PM.DR_PREMIUM_AMT, PM.GDR_402, PM.GDR_406, DP.NAME_THAI AS PROFILE_NAME,\r\n" + 
				"DR.DOCTOR_PROFILE_CODE AS PROFILE_CODE, PM.PAYMENT_MODE_CODE\r\n" + 
				"FROM PAYMENT_MONTHLY PM \r\n" + 
				"LEFT OUTER JOIN DOCTOR DR ON (PM.DOCTOR_CODE = DR.CODE AND PM.HOSPITAL_CODE = DR.HOSPITAL_CODE)\r\n" + 
				"LEFT OUTER JOIN DOCTOR_PROFILE DP ON (DR.DOCTOR_PROFILE_CODE = DP.CODE AND DR.HOSPITAL_CODE = DP.HOSPITAL_CODE)\r\n" + 
				"LEFT OUTER JOIN BANK BK ON (DR.BANK_CODE = BK.CODE)\r\n" + 
				"WHERE PM.YYYY LIKE ? AND PM.MM LIKE ?\r\n" + 
				"AND (DR.DOCTOR_PROFILE_CODE BETWEEN ? AND ?)\r\n" + 
				"AND PM.PAYMENT_MODE_CODE <> 'U'\r\n" + 
				"AND PM.DR_NET_PAID_AMT > 0\r\n" + 
				"--AND LEN(PM.PAYMENT_TERM_DATE) = 8\r\n" + 
				"ORDER BY DR.DOCTOR_PROFILE_CODE, DR.CODE";
		try (java.sql.Connection conn = DbConnector.getDBConnection()) {
			// ต้องการเลือก row และ Colume ใชเ ArayList HashMap
			ps = conn.prepareStatement(sql);
			ps.setString(1, yyyy);
			ps.setString(2, mm);
			ps.setString(3, from_doctor);
			ps.setString(4, to_doctor);

			checkFile = DbConnector.convertArrayListHashMap(ps.executeQuery());
			System.out.println("ExecuteQuery Success method >> getPaymentVoucher");

		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			if (ps != null) {
				ps.close();
			}
		}
		return checkFile;

	}

	public static int getNPaymentVoucher(String code_doctor) throws SQLException, IOException {

		// return 1;//test
		return getPaymentVoucher(code_doctor).size();
	}

}
