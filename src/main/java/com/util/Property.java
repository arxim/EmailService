package com.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import com.service.SendmailService;

public class Property {

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

}
