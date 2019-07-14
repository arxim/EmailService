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


@Controller
@CrossOrigin
@RequestMapping(value = "/sendmail")
public class test {

	@RequestMapping(value = "/test", method = RequestMethod.POST)
	public @ResponseBody String main(@RequestParam("status") String status) {

		System.out.println("response : " + status);

		try {
			GenReportAndMailProcess.main(status);
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return status;

	}

}
