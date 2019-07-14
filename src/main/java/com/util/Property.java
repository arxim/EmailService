package com.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import com.service.SendmailService;

public class Property {

	// ใช้ property ร่วมกัน
	/*
	 * public static Properties getCenterProperty(String path) throws IOException {
	 * 
	 * // สร้าง Object ขึ้นมาใหม่ ทุกครั้งที่มีการเรียก prop = new Properties();
	 * InputStream in = SendmailService.class.getResourceAsStream(path);
	 * prop.load(in); in.close(); return prop; }
	 */
	static Properties prop = null;

	// ใช้ property ร่วมกัน
	public static Properties getCenterProperty(String path) throws IOException {

		// สร้าง Object ขึ้นมาใหม่ ทุกครั้งที่มีการเรียก
		prop = new Properties();
		InputStream in = SendmailService.class.getResourceAsStream(path);
		prop.load(in);
		in.close();
		return prop;
	}

	public static Properties getProp(String keys) throws IOException {

		System.out.println("\n");
		prop = getCenterProperty("/application.properties");
		// รับ input
		// ให้สามารถใส่ไปหลายๆค่าได้ในอันเดียวกัน
		Properties properties = null;
		Set<?> url = null;
		Iterator<?> it = null;

		String key = "";
		String value = "";

		String[] skey = keys.split(",");

		for (String k : skey) {

			String input = k;
			int lenIn = input.length();
			// เช็คความยาวของ input

			// สร้างพ้อยเตอร์ ชี้
			url = prop.keySet();
			it = url.iterator();

			// ต้องการ sort ค่า

			properties = new Properties();
			// ต้องการให้เรียงข้อมูล
			// ชี้พ้อยเตอร์ลงมาที่ตำแหน่งที่ 0
			while (it.hasNext()) {

				// รับ key ของ พ้อยเตอร์ ที่ hasNext >> ตั้งแต่ ... จน มีค่าเป็น false = "-1"
				key = (String) it.next();
				// รับ value ของ key นั้นๆ
				value = prop.getProperty(key);
				// นับความยาวของ key
				int lenKey = key.length();
				// จัดระเบียบคีย์

				// ดึงข้อมู,ที่ต้องการออกมาจาก properties
				// เงื่อนไข ความยาวของตัวอักษรของ key ยาวมากกว่า ความยาวของ input
				if (lenKey >= lenIn) {

					String check = "false";

					for (int i = 0; i < lenIn; i++) {
						if (key.charAt(i) == input.charAt(i)) {
							check = "true";
						} else {
							check = "false";
							break;
						}
					}
					// คัดแยกคีย์ที่ไม่ต้องการออกจาก property -> ตัวที่ต้องการเก็บไว้ใน treeMap
					// เพื่อจัดเรียงข้อมูล
					// เงี่ยนไขแรก กรอกเอาตัวที่ ใช่คำนั้นๆเลย
					// เงื่อนไขที่สอง คำนั้น ที่ไม่ติดข้อความอะไร พร้อมที่จะ เรียกค่า sub key
					if (((lenKey == lenIn) && (check == "true"))
							|| ((lenKey >= lenIn) && (check == "true") && (key.charAt(lenIn) == '.'))) {
						// เก็บไว้ใน maptree ที่สามารถ sort ค่า key ได้
						// System.out.println("Key = " + key + " value = " + value);
						properties.put(key, value);
					}
				}
			}
		}

		return properties;

	}

}
