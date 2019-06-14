package com.dao;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import com.util.DbConnector;
import com.util.Property;

public class SummaryDFUnpaidByDetailAsOfDateDAO {

	static ArrayList<HashMap<String, String>> checkFile = null;

	public static ArrayList<HashMap<String, String>> getSummaryDFUnpaidByDetailAsOfDate(String code_doctor)
			throws IOException, SQLException {

		String to_date = Property.getCenterProperty("/application.properties").getProperty("to_date");
		String from_date = Property.getCenterProperty("/application.properties").getProperty("from_date");

		// แสดงค่าที่ต้องการ
		checkFile = new ArrayList<>();
		PreparedStatement ps = null;
		String sql = "SELECT \r\n" + "DP.CODE AS PROFILE_CODE,\r\n" + "DP.NAME_THAI AS PROFILE_NAME,\r\n"
				+ "SD.VERIFY_DATE AS 'VERIFY_DATE',\r\n" + "SD.DOCTOR_CODE AS 'DOCTOR_CODE',\r\n"
				+ "SD.VERIFY_TIME AS 'START_TIME',\r\n" + "SD.YYYY AS 'YYYY', \r\n" + "SD.MM AS 'MM', \r\n"
				+ "DR.NAME_THAI AS 'NAME_THAI', \r\n" + "SD.INVOICE_NO AS 'INVOICE_NO', \r\n"
				+ "SD.INVOICE_DATE AS 'INVOICE_DATE',\r\n" + "SD.PATIENT_NAME AS 'PATIENT_NAME', \r\n"
				+ "SD.HN_NO AS 'HN_NO',\r\n" + "SD.IS_PAID,\r\n"
				+ "SD.ADMISSION_TYPE_CODE AS 'ADMISSION_TYPE_CODE', \r\n"
				+ "SD.TRANSACTION_MODULE AS 'TRANSACTION_MODULE', \r\n" + "SD.LINE_NO AS 'LINE_NO',\r\n"
				+ "SD.ORDER_ITEM_CODE AS 'ORDER_ITEM_CODE', \r\n" + "SD.PAYOR_OFFICE_CODE,\r\n"
				+ "SD.PAYOR_OFFICE_NAME,\r\n" + "SD.DR_AMT AS 'DR_AMT', \r\n" + "SD.AMOUNT_AFT_DISCOUNT, \r\n"
				+ "SD.AMOUNT_BEF_DISCOUNT - SD.AMOUNT_OF_DISCOUNT AS 'OLD_AMOUNT',\r\n"
				+ "SD.GUARANTEE_PAID_AMT, --SD.GUARANTEE_NOTE,\r\n"
				+ "CASE WHEN SD.GUARANTEE_NOTE IS NULL THEN '' ELSE SD.GUARANTEE_NOTE END AS GUARANTEE_NOTE,\r\n"
				+ "OI.DESCRIPTION_THAI AS 'DESCRIPTION_THAI',\r\n" + "SD.DR_TAX_406 AS 'DR_TAX_AMT',\r\n"
				+ "DR.DOCTOR_CATEGORY_CODE AS 'DOCTOR_CATEGORY_CODE'\r\n"
				+ "FROM TRN_DAILY SD LEFT OUTER JOIN DOCTOR DR ON (SD.DOCTOR_CODE = DR.CODE\r\n"
				+ "AND SD.HOSPITAL_CODE = DR.HOSPITAL_CODE)\r\n"
				+ "LEFT OUTER JOIN DOCTOR_PROFILE DP ON (DR.DOCTOR_PROFILE_CODE = DP.CODE\r\n"
				+ "AND DR.HOSPITAL_CODE = DP.HOSPITAL_CODE)\r\n"
				+ "LEFT OUTER JOIN ORDER_ITEM OI ON (OI.CODE = SD.ORDER_ITEM_CODE\r\n"
				+ "AND OI.HOSPITAL_CODE = SD.HOSPITAL_CODE)\r\n" + "WHERE (SD.INVOICE_DATE BETWEEN ? AND ?) AND\r\n"
				+ "DP.CODE LIKE ? AND \r\n" + "SD.PATIENT_DEPARTMENT_CODE LIKE ? AND\r\n"
				+ "SD.PAYOR_OFFICE_CODE LIKE ? AND\r\n"
				+ "SD.ACTIVE = '1' AND DR.ACTIVE = '1' AND SD.ORDER_ITEM_ACTIVE = '1' AND\r\n"
				+ "SD.YYYY = '' AND SD.MM = '' AND\r\n" + "(SD.BATCH_NO = '' OR SD.BATCH_NO IS NULL) AND \r\n"
				+ "SD.INVOICE_TYPE <> 'ORDER' AND\r\n" + "SD.HOSPITAL_CODE LIKE ?\r\n"
				+ "ORDER BY PROFILE_CODE,DOCTOR_CODE,INVOICE_DATE,VERIFY_DATE";
		try (java.sql.Connection conn = DbConnector.getDBConnection()) {
			// ต้องการเลือก row และ Colume ใชเ ArayList HashMap
			ps = conn.prepareStatement(sql);
			ps.setString(1, from_date);
			ps.setString(2, to_date);
			ps.setString(3, code_doctor);
			ps.setString(4, "%");
			ps.setString(5, "%");
			ps.setString(6, "%");

			checkFile = DbConnector.convertArrayListHashMap(ps.executeQuery());

		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			if (ps != null) {
				ps.close();
			}
		}
		return checkFile;

	}

	public static int getNSummaryDFUnpaidByDetailAsOfDate(String code_doctor) throws SQLException, IOException {

		// return 1;//test
		return getSummaryDFUnpaidByDetailAsOfDate(code_doctor).size();
	}

}
