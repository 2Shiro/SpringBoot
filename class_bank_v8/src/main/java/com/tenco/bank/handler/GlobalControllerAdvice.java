package com.tenco.bank.handler;

import com.tenco.bank.handler.exception.DataDeliveryException;
import com.tenco.bank.handler.exception.RedirectException;
import com.tenco.bank.handler.exception.UnAuthorizedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.ModelAndView;

// 중앙에서 관리할 예외처리 핸들러 만들어 보기
@ControllerAdvice // Ioc 대상(싱글톤) - HTML 렌더링시 예외에서 많이 사용
//@RestControllerAdvice RESET API 활용이 많이 됨.
//@ControllerAdvice +  @ResponseBody
public class GlobalControllerAdvice {

    // 모든 예외 클래스를 알 수 없기 때문에 로깅으로 확인할 수 있도록 적용
    @ExceptionHandler({Exception.class})
    public void exception(Exception e) {
        System.out.println("----------------");
        System.out.println(e.getClass().getName());
        System.out.println(e.getMessage());
        System.out.println("----------------");
    }

    // 데이터로 클라이언트에게 전달
    @ResponseBody // 뷰 리졸버 안탐
    @ExceptionHandler(DataDeliveryException.class)
    public String dataDeliveryException(DataDeliveryException e) {
        StringBuffer sb = new StringBuffer();
        sb.append(" <script>");
        sb.append(" alert(' "+ e.getMessage() + " ' );");
        sb.append(" window.history.back();");
        sb.append(" </script>");
        return  sb.toString();
    }

    @ResponseBody // 뷰 리졸버 안탐
    @ExceptionHandler(UnAuthorizedException.class)
    public String unAuthorizedException(UnAuthorizedException e) {
        StringBuffer sb = new StringBuffer();
        sb.append(" <script>");
        sb.append(" alert(' "+ e.getMessage() + " ' );");
        sb.append(" window.history.back();");
        sb.append(" </script>");
        return  sb.toString();
    }


    /**
     * 에러 페이지로 이동 처리 하기
     * JSP로 이동시 데이터를 담아서 보내는 방법
     */
    @ExceptionHandler(RedirectException.class)
    public ModelAndView redirectException(RedirectException e) {
        ModelAndView modelAndView = new ModelAndView("/errorPage");
        modelAndView.addObject("statusCode", e.getStatus().value());
        modelAndView.addObject("message", e.getMessage());
        return  modelAndView;
    }
}