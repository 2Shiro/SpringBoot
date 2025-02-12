package com.tenco.bank.controller;

import com.tenco.bank.handler.exception.RedirectException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller  // IoC 대상(싱글톤 패턴 관리가 된다.) --> 제어의 역전  
public class MainController {

	@GetMapping({"/main-page", "/index", "/"})
	public String mainPage() {

		throw new RedirectException("Sorry, the page you are looking for could not be found.", HttpStatus.UNAUTHORIZED);
		//System.out.println("mainPage() 호출 확인");

		//return "/main";
	}
	

}


