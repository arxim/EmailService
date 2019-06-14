package com.dao;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import com.util.DbConnector;
import com.util.Property;

public class ExpenseDetailDAO {

	static ArrayList<HashMap<String, String>> checkFile = null;

	public static ArrayList<HashMap<String, String>> getExpenseDetail(String code_doctor)
			throws IOException, SQLException {

		String from_doctor = code_doctor;
		String to_doctor = code_doctor;
		String hospitalCode = Property.getCenterProperty("/application.properties")
				.getProperty("hospitalCode");
		//String yyyy = Property.getCenterProperty("/application.properties").getProperty("yyyy");
		//String mm = Property.getCenterProperty("/application.properties").getProperty("mm");
		String mm = null;
		String yyyy = null;
		try {
			mm = BatchDao.getMonth(hospitalCode);
			yyyy = BatchDao.getYear(hospitalCode);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// แสดงค่าที่ต้องการ
		checkFile = new ArrayList<>();
		PreparedStatement ps = null;
		String sql = "SELECT DOCTOR.CODE, DOCTOR.NAME_THAI, TRN_EXPENSE_DETAIL.DOCTOR_CODE, TRN_EXPENSE_DETAIL.YYYY, \r\n"
				+ "TRN_EXPENSE_DETAIL.MM, TRN_EXPENSE_DETAIL.AMOUNT * TRN_EXPENSE_DETAIL.EXPENSE_SIGN as AMOUNT,\r\n"
				+ "CASE WHEN TRN_EXPENSE_DETAIL.EXPENSE_SIGN = '-1' THEN AMOUNT ELSE 0 END AS CREDIT_AMOUNT,\r\n"
				+ "CASE WHEN TRN_EXPENSE_DETAIL.EXPENSE_SIGN = '1' THEN AMOUNT ELSE 0 END AS DEBIT_AMOUNT,\r\n"
				+ "TRN_EXPENSE_DETAIL.EXPENSE_CODE, TRN_EXPENSE_DETAIL.INVOICE_TYPE_DESCRIPTION,\r\n"
				+ "TRN_EXPENSE_DETAIL.DOC_DATE, TRN_EXPENSE_DETAIL.DOC_NO, EXPENSE.CODE, EXPENSE.DESCRIPTION,\r\n"
				+ "TRN_EXPENSE_DETAIL.NOTE, TRN_EXPENSE_DETAIL.EXPENSE_SIGN, TRN_EXPENSE_DETAIL.TAX_AMOUNT,\r\n"
				+ "TRN_EXPENSE_DETAIL.TAX_TYPE_CODE\r\n"
				+ "FROM TRN_EXPENSE_DETAIL LEFT OUTER JOIN DOCTOR ON TRN_EXPENSE_DETAIL.DOCTOR_CODE = DOCTOR.CODE\r\n"
				+ "LEFT OUTER JOIN EXPENSE ON TRN_EXPENSE_DETAIL.EXPENSE_CODE = EXPENSE.CODE\r\n"
				+ "where TRN_EXPENSE_DETAIL.HOSPITAL_CODE LIKE ?\r\n"
				+ "and (DOCTOR.DOCTOR_PROFILE_CODE BETWEEN ? and ?)\r\n" + "and TRN_EXPENSE_DETAIL.MM like ?\r\n"
				+ "and TRN_EXPENSE_DETAIL.YYYY = ?\r\n" + "AND DOCTOR.DOCTOR_CATEGORY_CODE like ?\r\n"
				+ "AND DOCTOR.DEPARTMENT_CODE like ?\r\n" + "AND EXPENSE_SIGN like ?\r\n"
				+ "AND EXPENSE_ACCOUNT_CODE like ?\r\n" + "AND EXPENSE_CODE like ?\r\n"
				+ "order by DOCTOR.CODE, TRN_EXPENSE_DETAIL.DOC_NO";
		try (java.sql.Connection conn = DbConnector.getDBConnection()) {
			// ต้องการเลือก row และ Colume ใชเ ArayList HashMap
			ps = conn.prepareStatement(sql);
			ps.setString(1, hospitalCode);
			ps.setString(2, from_doctor);
			ps.setString(3, to_doctor);
			ps.setString(4, mm);
			ps.setString(5, yyyy);
			ps.setString(6, "%");
			ps.setString(7, "%");
			ps.setString(8, "%");
			ps.setString(9, "%");
			ps.setString(10, "%");

			checkFile = DbConnector.convertArrayListHashMap(ps.executeQuery());
			System.out.println("ExecuteQuery Success method >> getExpenseDetail");
			
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			if (ps != null) {
				ps.close();
			}
		}
		return checkFile;

	}

	public static int getNExpenseDetail(String code_doctor) throws SQLException, IOException {

		// return 1;//test
		return getExpenseDetail(code_doctor).size();
		
	}
}
