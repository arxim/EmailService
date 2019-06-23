package com.dao;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import com.util.DbConnector;
import com.util.Property;

public class SummaryDFUnpaidSubreportDAO {

	static ArrayList<HashMap<String, String>> checkFile = null;

	public static ArrayList<HashMap<String, String>> getSummaryDFUnpaidByDetailAsOfDate(String code_doctor)
			throws IOException, SQLException {

		String hospitalCode = Property.getCenterProperty("/property/application.properties")
				.getProperty("hospitalCode");
		String to_date = Property.getCenterProperty("/property/application.properties").getProperty("to_date");
		String from_date = Property.getCenterProperty("/property/application.properties").getProperty("from_date");

		// แสดงค่าที่ต้องการ
		checkFile = new ArrayList<>();
		PreparedStatement ps = null;
		String sql = "SELECT S.DOCTOR_CODE, \r\n" + "SUM(S.DR_AMT) AS DR_AMT,\r\n"
				+ "SUM(S.DR_TAX_400+S.DR_TAX_401+S.DR_TAX_402+S.DR_TAX_406) AS DR_TAX_AMT\r\n" + "FROM TRN_DAILY S \r\n"
				+ "WHERE S.HOSPITAL_CODE = ? AND \r\n" + "(S.INVOICE_DATE BETWEEN ? AND ?) AND\r\n"
				+ "S.DOCTOR_CODE = ? AND INVOICE_TYPE <> 'ORDER' AND S.ACTIVE = '1' AND\r\n"
				+ "S.YYYY = '' AND S.MM = '' AND\r\n" + "(S.BATCH_NO = '' OR S.BATCH_NO IS NULL) AND\r\n"
				+ "S.IS_PAID <> 'N' AND S.ORDER_ITEM_ACTIVE = '1'\r\n" + "GROUP BY S.DOCTOR_CODE";
		try (java.sql.Connection conn = DbConnector.getDBConnection()) {
			// ต้องการเลือก row และ Colume ใชเ ArayList HashMap
			ps = conn.prepareStatement(sql);
			ps.setString(1, hospitalCode);
			ps.setString(2, from_date);
			ps.setString(3, to_date);
			ps.setString(4, code_doctor);

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
