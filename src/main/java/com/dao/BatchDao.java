package com.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet; 

import com.util.DbConnector;
 

  
public class BatchDao {
	
	public static String getYear(String hospitalCode) throws Exception {
		String value = "";
		PreparedStatement ps = null;
		
		final String SQL = "SELECT TOP 1 YYYY FROM BATCH WHERE CLOSE_DATE <> '' AND HOSPITAL_CODE = ? ORDER BY CLOSE_DATE DESC ";
		try (Connection conn = DbConnector.getDBConnection()) {
			ps = conn.prepareStatement(SQL);
			ps.setString(1, hospitalCode);
			ResultSet rs = ps.executeQuery();
			rs.next();
			value = rs.getString("YYYY");
		} catch (Exception e) {
			throw e;
		} finally {
			if (ps != null) ps.close();
		}
		return value;
	}
	
	public static String getMonth(String hospitalCode) throws Exception {
		String value = "";
		PreparedStatement ps = null;
		
		final String SQL = "SELECT TOP 1 MM FROM BATCH WHERE CLOSE_DATE <> '' AND HOSPITAL_CODE = ? ORDER BY CLOSE_DATE DESC ";
		try (Connection conn =  DbConnector.getDBConnection()) {
			ps = conn.prepareStatement(SQL);
			ps.setString(1, hospitalCode);
			ResultSet rs = ps.executeQuery();
			rs.next();
			value = rs.getString("MM");
		} catch (Exception e) {
			throw e;
		} finally {
			if (ps != null) ps.close();
		}
		return value;
	}
	
	public static Boolean isBatchClose(String hospitalCode, String year, String mount) throws Exception {
		int value = 0;
		PreparedStatement ps = null;
		
		final String SQL = "SELECT COUNT(*) COUNTDATA FROM BATCH WHERE HOSPITAL_CODE = ? AND YYYY = ? AND MM = ? AND CLOSE_DATE != ''";
		
		try (Connection conn = DbConnector.getDBConnection()) {
			ps = conn.prepareStatement(SQL);
			ps.setString(1, hospitalCode);
			ps.setString(2, year);
			ps.setString(3, mount);
			ResultSet rs = ps.executeQuery();
			rs.next();
			value = rs.getInt("COUNTDATA");
		} catch (Exception e) {
			throw e;
		} finally {
			if (ps != null) ps.close();
		}
		return value == 0 ? true : false;
	}
	 
	
}
