package com.dao;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import com.util.DbConnector;
import com.util.Property;

public class SummaryRevenueByDetailDAO {

	static ArrayList<HashMap<String, String>> checkFile = null;

	public static ArrayList<HashMap<String, String>> getSummaryDFUnpaidByDetailAsOfDate(String code_doctor)
			throws IOException, SQLException {

		String from_doctor = code_doctor;
		String to_doctor = code_doctor;
		String hospitalCode = Property.getCenterProperty("/application.properties").getProperty("hospitalCode");
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
		String sql = "(SELECT \r\n" + "		DP.CODE AS PROFILE_CODE,\r\n" + "		DP.NAME_THAI AS PROFILE_NAME,\r\n"
				+ "		1 AS 'TYPE_QUERY',\r\n" + "		SD.VERIFY_DATE AS 'VERIFY_DATE',\r\n"
				+ "		SD.DOCTOR_CODE AS 'DOCTOR_CODE',\r\n" + "		SD.VERIFY_TIME AS 'START_TIME',\r\n"
				+ "		SD.VERIFY_DATE AS 'END_DATE',\r\n" + "		SD.VERIFY_TIME AS 'END_TIME',\r\n"
				+ "		0.00 AS 'ABSORB_AMOUNT',\r\n" + "		SD.YYYY AS 'YYYY', \r\n" + "		SD.MM AS 'MM', \r\n"
				+ "		DR.NAME_THAI AS 'NAME_THAI',\r\n" + "		DR.DOCTOR_CATEGORY_CODE,\r\n"
				+ "		SD.INVOICE_NO AS 'INVOICE_NO', \r\n" + "		SD.INVOICE_DATE AS 'INVOICE_DATE',\r\n"
				+ "		SD.PATIENT_NAME AS 'PATIENT_NAME',\r\n" + "		SD.HN_NO AS 'HN_NO', \r\n"
				+ "		SD.IS_PAID, \r\n" + "		SD.ADMISSION_TYPE_CODE,\r\n"
				+ "		SD.AMOUNT_AFT_DISCOUNT AS 'OLD_AMOUNT',\r\n"
				+ "		SD.AMOUNT_BEF_DISCOUNT-SD.AMOUNT_OF_DISCOUNT AS 'WRITE_OFF_AMOUNT', \r\n"
				+ "		SD.INVOICE_TYPE,\r\n" + "		SD.LINE_NO AS 'LINE_NO',\r\n"
				+ "		OI.DESCRIPTION_THAI AS 'ORDER_ITEM_CODE', \r\n" + "		OI.ORDER_ITEM_CATEGORY_CODE,\r\n"
				+ "		CASE WHEN SD.MM = ?\r\n" + "	 		THEN \r\n" + "			SD.DR_AMT\r\n"
				+ "	 		ELSE\r\n"
				+ "			CASE WHEN SD.GUARANTEE_TERM_MM = ? AND SD.GUARANTEE_TERM_YYYY = ? \r\n"
				+ "			     AND SD.GUARANTEE_NOTE IN ('ABSORB SOME GUARANTEE','ABSORB OLD SOME')\r\n"
				+ "				THEN SD.GUARANTEE_PAID_AMT\r\n" + "			 	ELSE SD.DR_AMT				\r\n"
				+ "			END\r\n" + "		END AS 'DR_AMT',\r\n" + "		--SD.DR_AMT AS 'DR_AMT', \r\n"
				+ "		SD.GUARANTEE_NOTE,\r\n" + "		SD.IS_WRITE_OFF AS 'IS_WRITE_OFF', \r\n"
				+ "		CASE WHEN SD.PAY_BY_AR = 'Y' THEN 'A' ELSE 'B' END AS PAY_METHOD,\r\n"
				+ "		--SD.DESCRIPTION_THAI AS 'DESCRIPTION_THAI',\r\n" + "		CASE WHEN SD.MM = ?\r\n"
				+ "	 	     THEN SD.DR_TAX_406 \r\n"
				+ "	 	     ELSE CASE WHEN SD.GUARANTEE_TERM_MM = ? AND SD.GUARANTEE_TERM_YYYY = ?\r\n"
				+ "				    AND SD.GUARANTEE_NOTE IN ('ABSORB OLD SOME','ABSORB SOME GUARANTEE')\r\n"
				+ "				    AND SD.DR_AMT > 0\r\n" + "			       THEN SD.GUARANTEE_PAID_AMT\r\n"
				+ "			       ELSE SD.DR_TAX_406 END\r\n"
				+ "		     END AS DR_TAX_AMT, SD.OLD_DR_AMT, SD.PRIVATE_DOCTOR\r\n"
				+ "	FROM TRN_DAILY SD LEFT OUTER JOIN DOCTOR DR ON (SD.DOCTOR_CODE = DR.CODE\r\n"
				+ "	AND SD.HOSPITAL_CODE = DR.HOSPITAL_CODE)\r\n"
				+ "	LEFT OUTER JOIN DOCTOR_PROFILE DP ON (DR.DOCTOR_PROFILE_CODE = DP.CODE\r\n"
				+ "	AND DR.HOSPITAL_CODE = DP.HOSPITAL_CODE)\r\n"
				+ "	LEFT OUTER JOIN ORDER_ITEM OI ON (OI.CODE = SD.ORDER_ITEM_CODE\r\n"
				+ "	AND OI.HOSPITAL_CODE = SD.HOSPITAL_CODE)\r\n" + "	WHERE (DP.CODE BETWEEN ? AND ?)\r\n"
				+ "	AND SD.DOCTOR_CODE IN (SELECT DOCTOR_CODE FROM PAYMENT_MONTHLY WHERE YYYY = ?\r\n"
				+ "	AND MM = ?)\r\n" + "	AND ((SD.YYYY LIKE ? AND SD.MM LIKE ?) \r\n"
				+ "	OR (SD.GUARANTEE_TERM_YYYY LIKE ? AND SD.GUARANTEE_TERM_MM LIKE ? AND SD.GUARANTEE_NOTE LIKE 'ABS%'))\r\n"
				+ "	AND SD.HOSPITAL_CODE LIKE ?\r\n" + "	AND DR.DOCTOR_CATEGORY_CODE LIKE ?\r\n"
				+ "	AND DR.DEPARTMENT_CODE LIKE ?\r\n" + "	AND SD.ACTIVE = '1' AND SD.ORDER_ITEM_ACTIVE = '1'\r\n"
				+ "	AND OI.IS_COMPUTE = 'Y'\r\n" + "	AND SD.ORDER_ITEM_CODE LIKE ?\r\n"
				+ "	AND OI.ORDER_ITEM_CATEGORY_CODE LIKE ?\r\n"
				+ "	AND SD.COMPUTE_DAILY_DATE is not null AND SD.COMPUTE_DAILY_DATE != ''\r\n"
				+ "	--AND (SD.PAY_BY_CASH='Y' OR SD.PAY_BY_AR='Y' OR SD.PAY_BY_DOCTOR='Y' OR SD.PAY_BY_PAYOR='Y' OR SD.PAY_BY_CASH_AR='Y' )\r\n"
				+ "	--AND SD.IS_PAID = 'Y'\r\n" + "	AND SD.AMOUNT_AFT_DISCOUNT > 0\r\n"
				+ "	AND (SD.GUARANTEE_NOTE LIKE '%OLD%' OR SD.DR_AMT > 0)\r\n" + "	)\r\n" + "UNION\r\n"
				+ "	(SELECT top 3000\r\n" + "		DP.CODE AS PROFILE_CODE,\r\n"
				+ "		DP.NAME_THAI AS PROFILE_NAME,\r\n" + "		2 AS 'TYPE_QUERY',		\r\n"
				+ "		STP.START_DATE AS 'VERIFY_DATE',\r\n" + "		STP.HP_ABSORB_DOCTOR_CODE AS 'DOCTOR_CODE',\r\n"
				+ "		STP.START_TIME AS 'START_TIME',\r\n" + "		STP.END_DATE AS 'END_DATE',\r\n"
				+ "		STP.END_TIME AS 'END_TIME',\r\n" + "		CASE WHEN HP402_ABSORB_AMOUNT > 0 \r\n"
				+ "			THEN (HP402_ABSORB_AMOUNT)\r\n" + "			ELSE DF402_CASH_AMOUNT\r\n"
				+ "		END AS 'ABSORB_AMOUNT',\r\n" + "		'' AS 'YYYY', \r\n" + "		'' AS 'MM', \r\n"
				+ "		DR.NAME_THAI AS 'NAME_THAI', \r\n" + "		'' AS 'DOCTOR_CATEGORY_CODE',\r\n"
				+ "		'' AS 'INVOICE_NO',\r\n" + "		'' AS 'INVOICE_DATE',\r\n"
				+ "		'' AS 'PATIENT_NAME',\r\n" + "		'' AS 'HN_NO', \r\n" + "		'' AS 'IS_PAID', \r\n"
				+ "		'Z' AS 'ADMISSION_TYPE_CODE',\r\n" + "		0.00 AS 'OLD_AMOUNT', \r\n"
				+ "		0.00 AS 'WRITE_OFF_AMOUNT',\r\n" + "		CASE WHEN STP.GUARANTEE_EXCLUDE_AMOUNT > 0 \r\n"
				+ "			THEN STP.GUARANTEE_TYPE_CODE+' : Extra Amount'\r\n"
				+ "			ELSE STP.GUARANTEE_TYPE_CODE+' : Absorb Guarantee'\r\n" + "		END AS 'INVOICE_TYPE',\r\n"
				+ "		STP.GUARANTEE_TYPE_CODE AS 'LINE_NO',\r\n" + "		'' AS 'ORDER_ITEM_CODE', \r\n"
				+ "		'' AS 'ORDER_ITEM_CATEGORY_CODE',\r\n" + "		0.00 AS 'DR_AMT', \r\n"
				+ "		'' AS 'GUARANTEE_NOTE',\r\n" + "		'N' AS 'IS_WRITE_OFF', \r\n"
				+ "		'B' AS 'PAY_METHOD',\r\n" + "		CASE WHEN HP402_ABSORB_AMOUNT > 0 \r\n"
				+ "			THEN (HP402_ABSORB_AMOUNT)\r\n" + "			ELSE DF402_CASH_AMOUNT\r\n"
				+ "		END AS 'DR_TAX_AMT',\r\n" + "		0.00 AS 'OLD_DR_AMT',\r\n"
				+ "		'' AS 'PRIVATE_DOCTOR'\r\n" + "	FROM STP_GUARANTEE STP \r\n"
				+ "	LEFT OUTER JOIN DOCTOR DR ON (STP.GUARANTEE_DR_CODE = DR.CODE AND STP.HOSPITAL_CODE = DR.HOSPITAL_CODE)\r\n"
				+ "	LEFT OUTER JOIN DOCTOR_PROFILE DP ON (DR.DOCTOR_PROFILE_CODE = DP.CODE AND\r\n"
				+ "	DR.HOSPITAL_CODE = DP.HOSPITAL_CODE)\r\n"
				+ "	WHERE (STP.DF402_CASH_AMOUNT+STP.HP402_ABSORB_AMOUNT+STP.DF406_HOLD_AMOUNT >0)\r\n"
				+ "	AND STP.GUARANTEE_TYPE_CODE = 'DLY'\r\n" + "	AND (DR.DOCTOR_PROFILE_CODE BETWEEN ? AND ?)\r\n"
				+ "	AND STP.YYYY LIKE ?\r\n" + "	AND STP.MM LIKE ?\r\n" + "	AND STP.HOSPITAL_CODE LIKE ?\r\n"
				+ "	AND DR.DOCTOR_CATEGORY_CODE LIKE ?\r\n" + "	AND DR.DEPARTMENT_CODE LIKE ?)\r\n" + "UNION\r\n"
				+ "	(SELECT DISTINCT\r\n" + "		DP.CODE AS PROFILE_CODE,\r\n"
				+ "		DP.NAME_THAI AS PROFILE_NAME,\r\n" + "		2 AS 'TYPE_QUERY',		\r\n"
				+ "		STP.YYYY + STP.MM +'01' AS 'VERIFY_DATE',\r\n"
				+ "		STP.HP_ABSORB_DOCTOR_CODE AS 'DOCTOR_CODE',\r\n" + "		'' AS 'START_TIME',\r\n"
				+ "		STP.YYYY+STP.MM+'31' AS 'END_DATE',\r\n" + "		'' AS 'END_TIME',\r\n"
				+ "		CASE WHEN HP402_ABSORB_AMOUNT > 0 \r\n" + "			THEN (HP402_ABSORB_AMOUNT)\r\n"
				+ "			ELSE DF402_CASH_AMOUNT\r\n" + "		END AS 'ABSORB_AMOUNT',\r\n"
				+ "		'' AS 'YYYY', \r\n" + "		'' AS 'MM', \r\n" + "		DR.NAME_THAI AS 'NAME_THAI', \r\n"
				+ "		'' AS 'DOCTOR_CATEGORY_CODE',\r\n" + "		'' AS 'INVOICE_NO',\r\n"
				+ "		'' AS 'INVOICE_DATE',\r\n" + "		'' AS 'PATIENT_NAME',\r\n" + "		'' AS 'HN_NO', \r\n"
				+ "		'' AS 'IS_PAID', \r\n" + "		'Z' AS 'ADMISSION_TYPE_CODE',\r\n"
				+ "		0.00 AS 'OLD_AMOUNT', \r\n" + "		0.00 AS 'WRITE_OFF_AMOUNT',\r\n"
				+ "		CASE WHEN STP.GUARANTEE_EXCLUDE_AMOUNT > 0 \r\n"
				+ "			THEN STP.GUARANTEE_TYPE_CODE+' : Extra Amount'\r\n"
				+ "			ELSE STP.GUARANTEE_TYPE_CODE+' : Absorb Guarantee'\r\n" + "		END AS 'INVOICE_TYPE',\r\n"
				+ "		STP.GUARANTEE_TYPE_CODE AS 'LINE_NO',\r\n" + "		'' AS 'ORDER_ITEM_CODE', \r\n"
				+ "		'' AS 'ORDER_ITEM_CATEGORY_CODE',\r\n" + "		0.00 AS 'DR_AMT', \r\n"
				+ "		'' AS 'GUARANTEE_NOTE',\r\n" + "		'N' AS 'IS_WRITE_OFF', \r\n"
				+ "		'B' AS 'PAY_METHOD',\r\n" + "		CASE WHEN HP402_ABSORB_AMOUNT > 0 \r\n"
				+ "			THEN (HP402_ABSORB_AMOUNT)\r\n" + "			ELSE DF402_CASH_AMOUNT\r\n"
				+ "		END AS 'DR_TAX_AMT',\r\n" + "		0.00 AS 'OLD_DR_AMT',\r\n"
				+ "		'' AS 'PRIVATE_DOCTOR'\r\n" + "	FROM STP_GUARANTEE STP \r\n"
				+ "	LEFT OUTER JOIN DOCTOR DR ON (STP.GUARANTEE_DR_CODE = DR.CODE AND STP.HOSPITAL_CODE = DR.HOSPITAL_CODE)\r\n"
				+ "	LEFT OUTER JOIN DOCTOR_PROFILE DP ON (DR.DOCTOR_PROFILE_CODE = DP.CODE AND\r\n"
				+ "	DR.HOSPITAL_CODE = DP.HOSPITAL_CODE)\r\n"
				+ "	WHERE (STP.DF402_CASH_AMOUNT+STP.HP402_ABSORB_AMOUNT+STP.DF406_HOLD_AMOUNT >0)\r\n"
				+ "	AND STP.GUARANTEE_TYPE_CODE <> 'DLY'\r\n" + "	AND (DR.DOCTOR_PROFILE_CODE BETWEEN ? AND ?)\r\n"
				+ "	AND STP.YYYY LIKE ?\r\n" + "	AND STP.MM LIKE ?\r\n" + "	AND STP.HOSPITAL_CODE LIKE ?\r\n"
				+ "	AND DR.DOCTOR_CATEGORY_CODE LIKE ?\r\n" + "	AND DR.DEPARTMENT_CODE LIKE ?)\r\n"
				+ "ORDER BY PROFILE_CODE,DOCTOR_CODE ,PAY_METHOD , END_DATE , END_TIME ,TYPE_QUERY  , ADMISSION_TYPE_CODE ,  INVOICE_DATE  , INVOICE_NO ;";
		try (java.sql.Connection conn = DbConnector.getDBConnection()) {
			// ต้องการเลือก row และ Colume ใชเ ArayList HashMap
			ps = conn.prepareStatement(sql);
			ps.setString(1, mm);
			ps.setString(2, mm);
			ps.setString(3, yyyy);
			ps.setString(4, mm);
			ps.setString(5, mm);
			ps.setString(6, yyyy);
			ps.setString(7, from_doctor);
			ps.setString(8, to_doctor);
			ps.setString(9, yyyy);
			ps.setString(10, mm);
			ps.setString(11, yyyy);
			ps.setString(12, mm);
			ps.setString(13, yyyy);
			ps.setString(14, mm);
			ps.setString(15, hospitalCode);
			ps.setString(16, "%");
			ps.setString(17, "%");
			ps.setString(18, "%");
			ps.setString(19, "%");
			ps.setString(20, from_doctor);
			ps.setString(21, to_doctor);
			ps.setString(22, yyyy);
			ps.setString(23, mm);
			ps.setString(24, hospitalCode);
			ps.setString(25, "%");
			ps.setString(26, "%");
			ps.setString(27, from_doctor);
			ps.setString(28, to_doctor);
			ps.setString(29, yyyy);
			ps.setString(30, mm);
			ps.setString(31, hospitalCode);
			ps.setString(32, "%");
			ps.setString(33, "%");

			checkFile = DbConnector.convertArrayListHashMap(ps.executeQuery());
			System.out.println("ExecuteQuery Success method >> getSummaryDFUnpaidByDetailAsOfDate");

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
