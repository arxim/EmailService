package com.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import com.util.DbConnector;

public class DoctorDAO {
	static ArrayList<HashMap<String, String>> listReciver = null;
	static ArrayList<HashMap<String, String>> checkFile = null;

	public static ArrayList<HashMap<String, String>> getReciver() throws SQLException {

		// แสดงค่าที่ต้องการ
		listReciver = new ArrayList<>();
		PreparedStatement ps = null;
		String sql = "SELECT [HOSPITAL_CODE]\r\n" + "      ,[HOSPITAL_UNIT_CODE]\r\n"
				+ "      ,[DOCTOR_PROFILE_CODE]\r\n" + "      ,[GUARANTEE_DR_CODE]\r\n" + "      ,[CODE]\r\n"
				+ "      ,[NAME_THAI]\r\n" + "      ,[NAME_ENG]\r\n" + "      ,[LICENSE_ID]\r\n"
				+ "      ,[FROM_DATE]\r\n" + "      ,[TO_DATE]\r\n" + "      ,[BANK_ACCOUNT_NO]\r\n"
				+ "      ,[BANK_ACCOUNT_NAME]\r\n" + "      ,[BANK_BRANCH_CODE]\r\n" + "      ,[BANK_CODE]\r\n"
				+ "      ,[DOCTOR_TYPE_CODE]\r\n" + "      ,[DOCTOR_CATEGORY_CODE]\r\n" + "      ,[PAY_TAX_402_BY]\r\n"
				+ "      ,[PAYMENT_MODE_CODE]\r\n" + "      ,[DEPARTMENT_CODE]\r\n" + "      ,[TAX_ID]\r\n"
				+ "      ,[NOTE]\r\n" + "      ,[ADDRESS1]\r\n" + "      ,[ADDRESS2]\r\n" + "      ,[ADDRESS3]\r\n"
				+ "      ,[ZIP]\r\n" + "      ,[EMAIL]\r\n" + "      ,[GUARANTEE_START_DATE]\r\n"
				+ "      ,[GUARANTEE_EXPIRE_DATE]\r\n" + "      ,[GUARANTEE_SOURCE]\r\n"
				+ "      ,[OVER_GUARANTEE_PCT]\r\n" + "      ,[IN_GUARANTEE_PCT]\r\n"
				+ "      ,[IS_ADVANCE_PAYMENT]\r\n" + "      ,[ACTIVE]\r\n" + "      ,[UPDATE_DATE]\r\n"
				+ "      ,[UPDATE_TIME]\r\n" + "      ,[USER_ID]\r\n" + "      ,[SALARY]\r\n"
				+ "      ,[POSITION_AMT]\r\n" + "      ,[IS_HOLD]\r\n" + "      ,[DISTRIBUTE_INCOME]\r\n"
				+ "      ,[ABSORB_TAX_TYPE]\r\n" + "      ,[VENDOR_ID]\r\n" + "      ,[HP_ABSORB_DOCTOR_CODE]\r\n"
				+ "  FROM [DFVTNPRD].[dbo].[DOCTOR] where EMAIL != '' and SALARY ='0.00'   ";
		try (java.sql.Connection conn = DbConnector.getDBConnection()) {
			// ต้องการเลือก row และ Colume ใชเ ArayList HashMap
			ps = conn.prepareStatement(sql);

			// ps.setString(1, doctorCode);
			listReciver = DbConnector.convertArrayListHashMap(ps.executeQuery());

		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			if (ps != null) {
				ps.close();
			}
		}

		return listReciver;
	}

	public static int getNReciver() throws SQLException {

		// return 1;//test
		return getReciver().size();
	}

}
