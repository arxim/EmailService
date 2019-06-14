package com.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.sql.SQLException;

import org.json.JSONArray;
import org.json.JSONObject;

import com.service.SendmailService;

public class DbConnector {
	private static Connection conn;
	private String conn_class;
	private String conn_url;
	private String conn_user;
	private String conn_password;
	private static Statement stm;
	private static PreparedStatement pstm;

	static Properties propData = new Properties();
	static InputStream in = Property.class.getResourceAsStream("/application.properties");

	public DbConnector() throws IOException {

		propData.load(in);

		try {

			this.conn_url = propData.getProperty("spring.datasource.url");
			this.conn_class = propData.getProperty("spring.datasource.driver-class-name");
			this.conn_user = propData.getProperty("spring.datasource.username");
			this.conn_password = propData.getProperty("spring.datasource.password");
			Class.forName(conn_class);
			conn = DriverManager.getConnection(conn_url, conn_user, conn_password);
			conn.setAutoCommit(false);
		} catch (ClassNotFoundException e) {
			System.out.println(e);
		} catch (SQLException e) {
			System.out.println(e);
		} catch (Exception e) {
			System.out.println(e);
		}

	}

	public Connection getConnection() {
		return this.conn;
	}

	public static Connection getDBConnection() throws IOException {
		Connection connection = null;
		propData.load(in);

		try {

			String conn_url = propData.getProperty("spring.datasource.url");
			String conn_class = propData.getProperty("spring.datasource.driver-class-name");
			String conn_user = propData.getProperty("spring.datasource.username");
			String conn_password = propData.getProperty("spring.datasource.password");
			Class.forName(conn_class);
			connection = DriverManager.getConnection(conn_url, conn_user, conn_password);

		} catch (ClassNotFoundException e) {
			System.out.println(e);
		} catch (SQLException e) {
			System.out.println(e);
		} catch (Exception e) {
			System.out.println(e);
		}

		return connection;

	}

	public void doPrepareConnect(String sql) {
		try {
			pstm = conn.prepareStatement(sql);
		} catch (SQLException e) {
			System.out.println(e);
		}
	}

	public void doConnect() {
		try {
			stm = conn.createStatement();
		} catch (SQLException e) {
			System.out.println(e);
		}
	}

	public static void doDisconnect() {
		try {
			if (!stm.isClosed()) {
				stm.close();
			} else {
				/* not implement */ }
			if (!pstm.isClosed()) {
				pstm.close();
			} else {
				/* not implement */ }
			if (!conn.isClosed()) {
				conn.close();
			} else {
				/* not implement */ }
			in.close();
		} catch (Exception e) {
		}
	}

	public void setPrepareStatement(String sql) {
		try {
			this.conn.prepareStatement(sql);
		} catch (SQLException e) {
		}
	}

	public PreparedStatement getPrepareStatement() {
		return this.pstm;
	}

	public boolean doSave(String sql) {
		try {
			stm.executeUpdate(sql);
			return true;
		} catch (SQLException e) {
			System.out.println(e);
			return false;
		}
	}

	public boolean doDelete(String sql) {
		try {
			stm.executeUpdate(sql);
			return true;
		} catch (SQLException e) {
			System.out.println(e);
			return false;
		}
	}

	public boolean doCommit() {
		try {
			conn.commit();
			return true;
		} catch (SQLException e) {
			System.out.println(e);
			return false;
		}
	}

	public boolean doRollback() {
		try {
			conn.rollback();
			return true;
		} catch (SQLException e) {
			System.out.println(e);
			return false;
		}
	}

	public JSONArray getJsonAutoComplete(String sql) {
		// return array data for autocomplete
		JSONArray arrayObj = new JSONArray();
		JSONObject jsonObj = new JSONObject();
		try {
			this.doConnect();
			ResultSet rs = this.stm.executeQuery(sql);
			arrayObj = new JSONArray();
			while (rs.next()) {
				jsonObj = new JSONObject();
				jsonObj.put("id", rs.getString(1));
				jsonObj.put("value", rs.getString(2));
				arrayObj.put(jsonObj);
			}
			rs.close();
		} catch (Exception e) {
			System.out.println("Error " + e.getMessage());
		}
		return arrayObj;
	}

	public static JSONArray getJsonAutoComplete(ResultSet rs) {
		JSONArray arrayObj = new JSONArray();
		JSONObject jsonObj = new JSONObject();
		try {
			while (rs.next()) {
				jsonObj = new JSONObject();
				jsonObj.put("id", rs.getString(1)); // Code
				jsonObj.put("view", rs.getString(2)); // Description
				jsonObj.put("value", rs.getString(3)); // Code : Description ...
				arrayObj.put(jsonObj);
			}
			rs.close();
		} catch (Exception e) {
			System.out.println("Error " + e.getMessage());
		}
		return arrayObj;
	}

	public JSONObject getJsonArrayData(String sql) {
		// return object data for datatable
		JSONArray arrayObj = new JSONArray();
		JSONObject jsonObj = new JSONObject();
		JSONArray arrayObjMain = new JSONArray();
		try {
			this.doConnect();
			ResultSet rs = this.stm.executeQuery(sql);
			ResultSetMetaData rsMetaData = rs.getMetaData();
			while (rs.next()) {
				arrayObj = new JSONArray();
				for (int i = 1; i <= rsMetaData.getColumnCount(); i++) {
					String value = "";
					if (rs.getString(i) != null && !rs.getString(i).equals("")) {
						value = rs.getString(i);
						arrayObj.put(value);
					} else {
						arrayObj.put(value);
					}
				}
				arrayObjMain.put(arrayObj);
			}
			jsonObj.put("data", arrayObjMain);
			rs.close();
		} catch (Exception e) {
			System.out.println("Error " + e.getMessage());
		}
		return jsonObj;
	}

	public JSONObject getJsonData(String sql) {
		JSONObject jsonObj = new JSONObject();
		ResultSet rs = null;
		try {
			this.doConnect();
			rs = this.stm.executeQuery(sql);
			ResultSetMetaData rsMetaData = rs.getMetaData();
			while (rs.next()) {
				HashMap<String, String> rtnData = new HashMap<String, String>();
				for (int i = 1; i <= rsMetaData.getColumnCount(); i++) {
					String value = "";
					if (rs.getString(i) != null && !rs.getString(i).equals("")) {
						value = rs.getString(i);
						jsonObj.put(rsMetaData.getColumnName(i), value);
					} else {
						jsonObj.put(rsMetaData.getColumnName(i), value);
					}
				}
			}
			rs.close();
		} catch (Exception e) {
			System.out.println("Error " + e.getMessage());
		}
		return jsonObj;
	}

	public ArrayList<HashMap<String, Object>> getData() {
		// for prepare statement select
		ResultSet rs = null;
		try {
			ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
			rs = pstm.executeQuery();
			ResultSetMetaData md = rs.getMetaData();

			while (rs.next()) {
				HashMap<String, Object> map = new HashMap<String, Object>();

				for (int i = 1; i <= md.getColumnCount(); i++) {
					map.put(md.getColumnLabel(i), rs.getObject(i));
				}
				list.add(map);
			}
			return list;
		} catch (Exception e) {
			System.out.println(e);
			return null;
		}
	}
	// New version

	public static List<Map<String, String>> getData(ResultSet rs) {
		List<Map<String, String>> lsQueryData = new ArrayList<Map<String, String>>();
		try {
			ResultSetMetaData rsMetaData = rs.getMetaData();
			while (rs.next()) {
				Map<String, String> rtnData = new HashMap<String, String>();
				for (int i = 1; i <= rsMetaData.getColumnCount(); i++) {
					String value = "";
					if (rs.getString(i) != null && !rs.getString(i).equals("")) {
						value = rs.getString(i);
						rtnData.put(rsMetaData.getColumnName(i), value);
					} else {
						rtnData.put(rsMetaData.getColumnName(i), value);
					}
				}
				lsQueryData.add(rtnData);
			}
			rs.close();
		} catch (Exception e) {
			System.out.println("Error " + e.getMessage());
		}
		return lsQueryData;
	}

	public ArrayList<HashMap<String, String>> getData(String sql) {
		ArrayList<HashMap<String, String>> lsQueryData = new ArrayList<HashMap<String, String>>();
		ResultSet rs = null;
		try {
			this.doConnect();
			rs = this.stm.executeQuery(sql);
			ResultSetMetaData rsMetaData = rs.getMetaData();
			while (rs.next()) {
				HashMap<String, String> rtnData = new HashMap<String, String>();
				for (int i = 1; i <= rsMetaData.getColumnCount(); i++) {
					String value = "";
					if (rs.getString(i) != null && !rs.getString(i).equals("")) {
						value = rs.getString(i);
						rtnData.put(rsMetaData.getColumnName(i), value);
					} else {
						rtnData.put(rsMetaData.getColumnName(i), value);
					}
				}
				lsQueryData.add(rtnData);
			}
			rs.close();
		} catch (Exception e) {
			System.out.println("Error " + e.getMessage());
		}
		return lsQueryData;
	}

	public Map<String, Object[]> getDataMap(String sql) {
		Map<String, Object[]> lsQueryData = new TreeMap<String, Object[]>();
		ResultSet rs = null;
		try {
			this.doConnect();
			rs = this.stm.executeQuery(sql);
			ResultSetMetaData rsMetaData = rs.getMetaData();

			// column name
			int count = rsMetaData.getColumnCount();

			Object[] metaColumn = new Object[count];
			for (int i = 1; i <= count; i++) {
				metaColumn[i - 1] = rsMetaData.getColumnLabel(i);
			}
			lsQueryData.put("1", metaColumn);

			int row = 2;
			while (rs.next()) {
				Object[] metaData = new Object[count];
				for (int i = 1; i <= count; i++) {
					String value = "";
					if (rs.getString(i) != null && !rs.getString(i).equals("")) {
						value = rs.getString(i);
						metaData[i - 1] = value;
					} else {
						metaData[i - 1] = value;
					}
				}
				lsQueryData.put(String.valueOf(row), metaData);
				row++;
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error " + e.getMessage());
		}
		return lsQueryData;
	}

	public String getDropDownList(String s) {
		return "";
	}

	public static ArrayList<HashMap<String, String>> convertArrayListHashMap(ResultSet rs) throws Exception {
		ArrayList<HashMap<String, String>> lsQueryData = new ArrayList<HashMap<String, String>>();
		try {
			ResultSetMetaData rsMetaData = rs.getMetaData();
			while (rs.next()) {
				HashMap<String, String> rtnData = new HashMap<String, String>();
				for (int i = 1; i <= rsMetaData.getColumnCount(); i++) {
					String value = "";
					if (rs.getString(i) != null && !rs.getString(i).equals("")) {
						value = rs.getString(i);
						rtnData.put(rsMetaData.getColumnName(i), value);
					} else {
						rtnData.put(rsMetaData.getColumnName(i), value);
					}
				}
				lsQueryData.add(rtnData);
			}
			rs.close();
		} catch (Exception e) {
			System.out.println("Error " + e.getMessage());
		}
		return lsQueryData;
	}

	public static JSONObject convertJsonObj(ResultSet rs) throws Exception {
		JSONArray arrayObj = new JSONArray();
		JSONObject jsonObj = new JSONObject();
		JSONArray arrayObjMain = new JSONArray();
		try {
			ResultSetMetaData rsMetaData = rs.getMetaData();
			while (rs.next()) {
				arrayObj = new JSONArray();
				for (int i = 1; i <= rsMetaData.getColumnCount(); i++) {
					String value = "";
					if (rs.getString(i) != null && !rs.getString(i).equals("")) {
						value = rs.getString(i);
						arrayObj.put(value);
					} else {
						arrayObj.put(value);
					}
				}
				arrayObjMain.put(arrayObj);
			}
			jsonObj.put("data", arrayObjMain);
			rs.close();
		} catch (Exception e) {
			throw e;
		}
		return jsonObj;
	}

	public static JSONArray queryJsonObj(ResultSet rs) throws Exception {
		JSONArray arrayArray = new JSONArray();
		try {
			ResultSetMetaData rsMetaData = rs.getMetaData();
			while (rs.next()) {
				JSONObject arrayObj = new JSONObject();
				for (int i = 1; i <= rsMetaData.getColumnCount(); i++) {
					String value = "";
					if (rs.getString(i) != null && !value.equals(rs.getString(i))) {
						value = rs.getString(i);
						arrayObj.put(rsMetaData.getColumnName(i).toLowerCase(), value);
					} else {
						arrayObj.put(rsMetaData.getColumnName(i).toLowerCase(), value);
					}
				}
				arrayArray.put(arrayObj);
			}
			rs.close();
		} catch (Exception e) {
			throw e;
		}
		return arrayArray;
	}
}