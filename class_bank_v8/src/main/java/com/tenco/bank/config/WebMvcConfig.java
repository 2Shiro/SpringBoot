
package com.tenco.bank.config;


import com.tenco.bank.handler.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration // IoC 대상 (스프링 부트 설정 클래스 이다)
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    // DI 처리
    private final AuthInterceptor authInterceptor;

    // 우리가 만든 인터셉트 클래스를 등록할 수 있다.
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/account/**")
                .addPathPatterns("/auth/**");
    }
}
