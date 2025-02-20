package com.tenco.bank.controller;


import com.tenco.bank.dto.KakaoProfile;
import com.tenco.bank.dto.OAuthToken;
import com.tenco.bank.dto.SignInDTO;
import com.tenco.bank.dto.SignUpDTO;
import com.tenco.bank.handler.exception.DataDeliveryException;
import com.tenco.bank.repository.model.User;
import com.tenco.bank.service.UserService;
import com.tenco.bank.utils.Define;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;

@Controller // IoC 대상 --> 보통 HTEML 렌더링(자바코드) ---> 클라이언트 응답
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    // @Autowired --> 가독성
    private final UserService userService;
    // 세션 메모리지에 접근하는 클래스가 있다.
    private final HttpSession session;

    @Value("${tenco.key}")
    private String tencoKey;

    /**
     * 회원가입
     * 주소 설계 : http://localhost:8080/user/sign-up
     */
    @GetMapping("/sign-up")
    public String signUpPage() {
        return "/user/signUp";
    }

    // 회원 가입 요청 처리
    // 주소 설계 http://localhost:8800/user/sign-up
    // Get, Post -> sign-up 같은 도메인이라도 구분이 가능하다.
    // REST API 를 사용하는 이유에 대해한번 더 살펴 보세요
    @PostMapping("/sign-up")
    public String signProc(SignUpDTO dto) {
        // 1. 인증검사 x
        // 2. 유효성 검사
        if (dto.getUsername() == null || dto.getUsername().isEmpty()) {
            throw new DataDeliveryException("username을 입력 하세요",
                    HttpStatus.BAD_REQUEST);
        }

        if (dto.getPassword() == null || dto.getPassword().isEmpty()) {
            throw new DataDeliveryException("password을 입력 하세요",
                    HttpStatus.BAD_REQUEST);
        }

        if (dto.getFullname() == null || dto.getFullname().isEmpty()) {
            throw new DataDeliveryException("fullname을 입력 하세요",
                    HttpStatus.BAD_REQUEST);
        }
        userService.createUser(dto);
        // todo account/list 경로로 변경해야 함
        return "redirect:/user/sign-in";
    }

    /**
     * 로그인 화면 요청
     *
     * @return
     */
    @GetMapping("/sign-in")
    public String singInPage() {
        return "/user/signIn";
    }


    /**
     * 예외적으로 보안상 이유로 POST을 사용한다.
     * -- 특정 --> GET 로그인 --> 암호화
     * 로그인 요청 처리
     * 주소설계 : http://localhost:8080/user/sign-in
     *
     * @return
     */
    @PostMapping("/sign-in")
    public String signProc(SignInDTO dto) {
        // 유효성 검사
        if (dto.getUsername() == null || dto.getUsername().isEmpty()) {
            throw new DataDeliveryException(Define.ENTER_YOUR_USERNAME, HttpStatus.BAD_REQUEST);
        }
        if (dto.getPassword() == null || dto.getPassword().isEmpty()) {
            throw new DataDeliveryException(Define.ENTER_YOUR_PASSWORD, HttpStatus.BAD_REQUEST);
        }

        User principal = userService.readUser(dto);
        session.setAttribute(Define.PRINCIPAL, principal);

        // todo account/list 경로로 변경해야 함
        return "redirect:/account/save";
    }


    // 코드 추가
    // 로그아웃 처리
    @GetMapping("/logout")
    public String logout() {
        session.invalidate();
        return "redirect:/user/sign-in";
    }


    @GetMapping("/kakao")
    public String kakaoLogin(@RequestParam(name = "code") String code) {
        // 1. 인증 코드 출력 (디버깅 용)
        System.out.println("code : " + code);

        // 2. 카카오 서버로 부터 액세스 토큰 받기
        // - 카카오에서 발급한 인가 코드를 액세스 토큰과 교환하기 위한 요청을 보내야 한다.

        // HTTP 통신 요청하기 위해서 RestTemplate 객체를 생성 해서 외부 API와 통신할 예정
        RestTemplate tokenRequestRestTemplate = new RestTemplate();
        // exchange - get,post,put,delete ..

        // 2.1 요청 헤더를 구성 : 전송 타입은 form-data 로 전송해야 한다.
        HttpHeaders tokenRequestHeaders = new HttpHeaders();
        tokenRequestHeaders.add("Content-Type",
                "application/x-www-form-urlencoded;charset=utf-8");
        // 2.2 요청 바디를 구성 : grant_type, client_id, redirect_uri, code
        // Map<k, List<String>>  --> {a: "1", b:"2" }   a: "", ""
        // MultiValueMap --> 내부적 Map<k, List<String>> 과 같다.
        // LinkedMultiValueMap --> 키의 삽입 순서를 유지합니다.
        // 이 순서 보장은 인덱스 연산자로 값을 꺼내는 편의성을 제공하는 객체 입니다.
        // get(0)
        MultiValueMap<String, String> tokenRequestParams = new LinkedMultiValueMap<>();
        tokenRequestParams.add("grant_type", "authorization_code");
        tokenRequestParams.add("client_id", "bbd20ecd57a59c85e3ed9b4c1fd55753");
        tokenRequestParams.add("redirect_uri", "http://localhost:8080/user/kakao");
        tokenRequestParams.add("code", code);

        // 2.3 헤더와 바디를 결합한 HttpEntity 생성
        // HTTP 요청 메세지를 구성하기 위해 헤더와 바디에 파라미터를 함께 구성해주는 객체 이다.
        // 즉, 이 객체를 통해서 API 요청시 필요한 모든 정보를 한 번에 전달할 수 있다.
        HttpEntity<MultiValueMap<String, String>> tokenRequestEntity
                = new HttpEntity<>(tokenRequestParams, tokenRequestHeaders);


        ResponseEntity<OAuthToken> tokenResponse = tokenRequestRestTemplate.exchange(
                "https://kauth.kakao.com/oauth/token",
                HttpMethod.POST,
                tokenRequestEntity,
                OAuthToken.class
        );
        // 응답 받은 액세스 토큰 출력 (디버깅 용)
        System.out.println("tokenResponse : " + tokenResponse.getBody().getAccessToken());

        // 3. 사용자 정보 API 요청을 해야 한다.
        // https://kapi.kakao.com/v2/user/me
        // 새로운 RestTemplate 객체 생성
        RestTemplate profileRequestRestTemplate = new RestTemplate();
        // 요청 헤더 구성, 요청 바디 구성 (x)
        // 3.1 요청 헤더 구성
        HttpHeaders profileRequestHeaders = new HttpHeaders();
        // Bearer + 공백 한칸 필수 !!!
        profileRequestHeaders.add("Authorization",
                "Bearer " + tokenResponse.getBody().getAccessToken());
        profileRequestHeaders.add("Content-Type",
                "application/x-www-form-urlencoded;charset=utf-8");

        // 3.2 요청 바디 구성 (x)
        // 사용자 정보 요청은 별도의 바디 없이 헤더만 전달하면 됨 (문서)
        HttpEntity<?> profileRequestEntity =
                new HttpEntity(profileRequestHeaders);

        // 3.3 POST,GET API --> exchange() 메서드 사용
        ResponseEntity<KakaoProfile> profileResponse = profileRequestRestTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.POST,
                profileRequestEntity,
                KakaoProfile.class
        );

        // 디버깅 용 (사용자 정보 확인)
        System.out.println("카카오 사용자 프로필 : "
                + profileResponse.getBody().getProperties().getNickname());

        // DTO 만들어야 한다.
        // 카카오로부터 받은 사용자 정보 객체
        KakaoProfile kakaoProfile = profileResponse.getBody();
        System.out.println(kakaoProfile.toString());

        // 1번 경우 , 최초 사용자라면 자동 회원 가입 처리 (우리 서버) -> 세션 등록
        /// 만약 회원 가입시에 필수적이 사용자 정보가 필요하다면 ---> 화면 생성 (추가 정보 받아야 한다)
        // 2번 경우 회원 가입 이력이 있는 사용자라면 바로 세션에 등록 처리 해야 한다. -> 세션 등록

        // 회원가입 정보를 담을 DTO 생성
        SignUpDTO signUpDTO = SignUpDTO
                .builder()
                .username("OAuth_" + kakaoProfile.getProperties().getNickname())
                .fullname("OAuth_" + kakaoProfile.getProperties().getNickname())
                .password(tencoKey) // 미리 정의된 더미 비밀번호
                .build();
        // 경우에 수
        // searchUsername --> User 객체 생성, null 값이 들어 오든 !
        User user = userService.searchUsername(signUpDTO.getUsername());
        // 문제에 원인
        // --> 최초 소셜로그인 시에 회원 가입까지 정상 동작
        // --> 자동 로그인 처리 (세션에 유저 정보 등록 실패 --> 다시 로그인 하라)
        // --> 최초 접근시라도 자동 로그인 처리 해야 한다.
        if(user == null) {
            userService.createUser(signUpDTO); // 회원 가입 처리
            user = userService.searchUsername(signUpDTO.getUsername());
        }
        // 세션에 정보 저장
        session.setAttribute(Define.PRINCIPAL, user);
        return "redirect:/account/list";
    }

}