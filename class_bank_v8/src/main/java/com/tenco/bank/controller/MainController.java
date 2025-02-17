package com.tenco.bank.controller;

import com.tenco.bank.handler.exception.DataDeliveryException;
import com.tenco.bank.handler.exception.RedirectException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller  // IoC 대상(싱글톤 패턴 관리가 된다.) --> 제어의 역전  
public class MainController {

	// 	// http:localhost:8080/main/main-page
	// 주소설계 
	// http:localhost:8080/main-page
	@GetMapping({"/main-page", "/index", "/"})
	// @ResponseBody
	public String mainPage() {

		return "/main";
	}
	

}


