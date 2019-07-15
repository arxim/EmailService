package com.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.service.GenReportAndMailProcess;
import com.service.GenReportAndMailProcessWithDoctorCode;

@Controller
@CrossOrigin
@RequestMapping(value = "/sendmail")
public class test {

	@RequestMapping(value = "/test", method = RequestMethod.POST)
	public @ResponseBody String main(@RequestParam("status") String status) {

		System.out.println("response : " + status);

		try {
			GenReportAndMailProcess.main();
		} catch (IOException e) {

			e.printStackTrace();
		}

		return status + "Success";

	}

	@RequestMapping(value = "/test2", method = RequestMethod.POST)
	public @ResponseBody String main2(@RequestParam("mail_doc") String mail_doc,
			@RequestParam("code_doc") String code_doc, @RequestParam("password_doc") String password_doc,
			@RequestParam("jasperFileName") String jasperFileName) {

		GenReportAndMailProcessWithDoctorCode.sendByCodeDoctor(mail_doc, code_doc, password_doc, jasperFileName);

		return mail_doc + code_doc + password_doc + jasperFileName + "Success";
	}

}
