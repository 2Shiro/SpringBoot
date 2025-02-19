
package com.tenco.bank.handler;


import com.tenco.bank.handler.exception.UnAuthorizedException;
import com.tenco.bank.repository.model.User;
import com.tenco.bank.utils.Define;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

// IoC
@Component
public class AuthInterceptor implements HandlerInterceptor {

    // 동작 흐름
    // 컨트롤러에 들어가기 전에 동작하는 메서드 이다.
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 코드 추가
        HttpSession session =  request.getSession();
        User principal = (User)session.getAttribute(Define.PRINCIPAL);
        if(principal == null) {
            throw new UnAuthorizedException("로그인 먼저 해주세요",
                    HttpStatus.UNAUTHORIZED);
        }
        return true;
    }

    // 뷰가 렌더링 되지 전에 호출 시킬 수 있다.
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    // 요청 처리가 완료 된 후, 뷰 렌더링이 완료된 후 호출 된다.
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}