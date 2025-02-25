package com.tenco.class_jwt.controller;

import com.tenco.class_jwt.dto.LoginResponseDto;
import com.tenco.class_jwt.dto.RegisterRequestDto;
import com.tenco.class_jwt.service.UserService;
import com.tenco.class_jwt.util.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    JWTUtil jwtUtil;

    // 회원가입 페이지, 회원가입 기능 처리
    @GetMapping("/register")
    public String showRegisterForm() {

        return "register";
    }

    @PostMapping("/register")
    public String register(RegisterRequestDto dto) {
        // 유효성 검사 생략
        userService.register(dto);

        return "redirect:/login";
    }
    
    // 로그인 페이지, 로그인 기능 처리
    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    // key = 값
    // 로그인 성공 시 -> welcome.mustache 파일로 이동(토큰) - 리프래시 토큰
    // 로그인 실패 시 -> login(error="실패")
    @PostMapping("/login")
    public String login(String username, String password, Model model) {
        LoginResponseDto response = userService.login(username, password);
        if(response != null) {
            model.addAttribute("accessToken", response.getAccessToken());
            model.addAttribute("refreshToken", response.getRefreshToken());
            model.addAttribute("username", response.getUsername());

            return "welcome";
        }
        model.addAttribute("error", "아이디 또는 비밀번호 잘못됨");

        return "login";
    }

    @PostMapping("/refresh")
    public String refreshToken(@RequestParam String refreshToken, Model model) {

        String newAccessToken = userService.refreshAccessToken(refreshToken);

        if(newAccessToken != null) {
            String username = jwtUtil.extractUsername(newAccessToken);
            model.addAttribute("accessToken", newAccessToken);
            model.addAttribute("refreshToken", refreshToken);
            model.addAttribute("username", username);

            return "welcome";
        }
        model.addAttribute("error", "리프레시 토큰이 만료 되었거나 유효하지 않습니다");
        return "login";
    }

    // JWT 인증 페이지 테스트
    @GetMapping("/protected")
    public String protectedPage(@RequestParam String accessToken, Model model) {
        String username = jwtUtil.extractUsername(accessToken);
        if(jwtUtil.validateToken(accessToken, username)) {
            model.addAttribute("username", username);
            return "protected";
        }
        return "redirect:/login";
    }

    // 로그아웃
    @PostMapping("/logout")
    public String logout(String refreshToken) {
        userService.logout(refreshToken);
        return "redirect:/login";
    }

}
