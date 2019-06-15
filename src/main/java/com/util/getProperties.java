package com.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;


public class getProperties {

	//รูปแบบการรับค่า คือ  "."
	
	/*
	public static void main(String[] args) throws IOException, JSONException {

		Properties getPro = getProperties.getPropertie("s");
		Set URL = getPro.keySet();
		Iterator itr = URL.iterator();

		while (itr.hasNext()) {
			String str = (String) itr.next();
			System.out.println("Key is	" + str + "	Value is " + getPro.getProperty(str));
		}

		System.out.println();

	}

*/
	public static Properties getPropertie(String InputKeys) throws IOException {
		// โหลดสิ่งของทั้งหมดลงใน prop แล้วค่อยเรียกใช้
		Properties prop = new Properties();
		Properties getProp = new Properties();
		InputStream in = getProperties.class.getResourceAsStream("/application.properties");
		prop.load(in);
		in.close();

		//รับ input
		String input = InputKeys;
		//เช็คความยาวของ input
		int lenIn = input.length();// 3

		//สร้างพ้อยเตอร์ ชี้
		Set url = prop.keySet();
		Iterator it = url.iterator();

		//ชี้พ้อยเตอร์ลงมาที่ตำแหน่งที่ 0 
		while (it.hasNext()) {

			//รับ key ของ พ้อยเตอร์ ที่ hasNext >> ตั้งแต่ ... จน มีค่าเป็น false = "-1"
			String key = (String) it.next();
			//รับ value ของ key นั้นๆ
			String value = prop.getProperty(key);
			//นับความยาวของ key
			int lenKey = key.length();
			//สร้างตัวแปรเช็คเงื่อนไข
			int i = 0;
			//System.out.println("\nNext Key....	" + key);

			// เช็ค ความยาวของคำ
			// กรณีความยาวอินพุธ น้อยกว่า ความยยาวคีย์
			if (lenIn < lenKey) {
				for (i = 0; i < lenIn; i++) {
					//วนเช็คอัขระทีละตัว
					if (input.charAt(i) == key.charAt(i)) {
						//System.out.println(i + " " + input.charAt(i) + "	" + key.charAt(i));

					} else {
						// เกิด เท็จ > ออกจากเงื่อนไข
						break;
					}

				}
				//System.out.println("number of length >> " + i + " " + lenIn);
				//ถ้าตรวจพบ ขนาดมีเท่ากัน เช็คว่า อักขระถัดไป คือ ? , ? แทนด้วยสิ่งต่างๆที่ต้องการ เช่น . > ให้ทำการ put ค่าลงใน  ตัวแปร getProp ที่มีชนิดเป็น properties 
				if (i == lenIn) {
					if (key.charAt(lenIn) == '.') {
						//System.out.println("Add >>>>key :	" + key + "\nvalue :	" + value + "\ncon key :	"
								//+ key.charAt(lenIn));
						getProp.put(key, value);
					}

				} else {
					//กรณีเจอว่าไม่ใช่ ให้ทำการ hasNext ไปตัวถัดไป สามารถ  ถ้าลบ continue ออกให้นำ เงื่อนไข else ออกไปด้วย การใส่ break จะทำให้ หลุดออกจาก loop while
					//System.out.println("----------------------");
					continue;
				}

			}

			//กรณี ขนาดของ input กับ key ใน file .properties มีขนาดเท่ากัน
			if (lenIn == lenKey) {
				//สร้างเงื่อนไข
				String check = "false";
				for (i = 0; i < lenIn; i++) {

					//เช็คทีละตัว
					if (input.charAt(i) == key.charAt(i)) {
						//กรณีใช้ จน จบ  max length ของ key
						check = "true";
						//System.out.println(i + " " + input.charAt(i) + "	" + key.charAt(i));

					} else {
						//เมื่อพบว่า ไม่เท่ากัน
						check = "false";
						break;
					}

				}

				//กรณีที่เท่ากัน ให้ put ค่าลงใน getProp ที่มีขนิดข้อมูลเป็น Properties
				if (check.equals("true")) {
					//System.out.println("Add >>>>key :	" + key + "\nvalue :	" + value + "\nNon con key :	-");
					getProp.put(key, value);
				}

			}
			//วนทำ จนครบทุก key 
			
			//System.out.println("----------------------");

		}
		
		// output ที่ออกไปจะเป็นค่าที่ถูก put ลงใน ตัวแปร getProp ที่มีชนิด เป็น Properties
		return getProp;
	}

}
