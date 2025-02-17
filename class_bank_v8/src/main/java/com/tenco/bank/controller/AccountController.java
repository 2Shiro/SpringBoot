package com.tenco.bank.controller;

import com.tenco.bank.dto.AccountSaveDTO;
import com.tenco.bank.dto.DepositDTO;
import com.tenco.bank.dto.WithdrawalDTO;
import com.tenco.bank.handler.exception.DataDeliveryException;
import com.tenco.bank.handler.exception.UnAuthorizedException;
import com.tenco.bank.repository.model.Account;
import com.tenco.bank.repository.model.User;
import com.tenco.bank.service.AccountService;
import com.tenco.bank.utils.Define;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller // IoC
@RequestMapping("/account")
public class AccountController {
    // final 처리
    private final HttpSession session;
    private final AccountService accountService;

    // 생성자 의존 주입 - DI 처리
    public AccountController(HttpSession session, AccountService accountService) {
        this.session = session;
        this.accountService = accountService;
    }

    // 주소 설계 - http://localhost:8080/account/save
    @GetMapping("/save")
    public String savePage() {
        // 1.인증 검사가 필요(account 전체 필요)
        User principal = (User)session.getAttribute("principal");
//        if(principal == null) {
//            throw new UnAuthorizedException("인증된 사용자가 아닙니다",
//                    HttpStatus.UNAUTHORIZED);
//        }
        return "/account/save";
    }


    // 계좌생성 기능
    @PostMapping("/save")
    public String saveProc(AccountSaveDTO dto) {
        // 유효성 검사보다 먼저 인증검사를 먼저 하는 것이 좋습니다.

        // 1. 인증검사
        User principal = (User)session.getAttribute("principal");
        if(principal == null) {
            throw new UnAuthorizedException("로그인 먼저 해주세요",
                    HttpStatus.UNAUTHORIZED);
        }

        // 2. 유효성 검사
        if(dto.getNumber() == null || dto.getNumber().isEmpty()) {
            throw new DataDeliveryException("계좌번호를 입력하시오",
                    HttpStatus.BAD_REQUEST);
        }

        if(dto.getPassword() == null || dto.getPassword().isEmpty()) {
            throw new DataDeliveryException("계좌비밀번호를 입력하시오",
                    HttpStatus.BAD_REQUEST);
        }

        if(dto.getBalance() == null || 	dto.getBalance() <= 0 ) {
            throw new DataDeliveryException("잘못된 입력 입니다",
                    HttpStatus.BAD_REQUEST);
        }
        accountService.createAccount(dto, principal.getId());
        return "redirect:/account/list";
    }

    /**
     * 계좌 목록 페이지
     *
     * @param model - accountList
     * @return list.jsp
     */
    @GetMapping({ "/list", "/" })
    public String listPage(Model model) {

        // 1.인증 검사가 필요(account 전체 필요)
        User principal = (User) session.getAttribute("principal");
        if (principal == null) {
            throw new UnAuthorizedException("인증된 사용자가 아닙니다", HttpStatus.UNAUTHORIZED);
        }

        // 경우의 수 -> 유, 무
        List<Account> accountList = accountService.readAccountListByUserId(principal.getId());

        if (accountList.isEmpty()) {
            model.addAttribute("accountList", null);
        } else {
            model.addAttribute("accountList", accountList);
        }

        return "/account/list";
    }

    @GetMapping("/withdrawal")
    public String withdrawalPage() {

        User principal = (User) session.getAttribute(Define.PRINCIPAL);
        if(principal == null) {
            throw new UnAuthorizedException(Define.ENTER_YOUR_LOGIN, HttpStatus.UNAUTHORIZED);
        }
        return "/account/withdrawal";
    }

    @PostMapping("/withdrawal")
    public String withdrawalProc(WithdrawalDTO dto) {

        User principal = (User) session.getAttribute(Define.PRINCIPAL);
        if(principal == null) {
            throw new UnAuthorizedException(Define.ENTER_YOUR_LOGIN, HttpStatus.UNAUTHORIZED);
        }

        // 2. 유효성 검사
        // 유효성 검사
        if(dto.getAmount() == null) {
            throw new DataDeliveryException(Define.ENTER_YOUR_BALANCE,
                    HttpStatus.BAD_REQUEST);
        }

        if(dto.getAmount().longValue() <= 0) {
            throw new DataDeliveryException(Define.W_BALANCE_VALUE,
                    HttpStatus.BAD_REQUEST);
        }

        if(dto.getWAccountNumber() == null) {
            throw new DataDeliveryException(Define.ENTER_YOUR_ACCOUNT_NUMBER,
                    HttpStatus.BAD_REQUEST);
        }

        if(dto.getWAccountPassword() == null || dto.getWAccountPassword().isEmpty() ) {
            throw new DataDeliveryException(Define.ENTER_YOUR_PASSWORD,
                    HttpStatus.BAD_REQUEST);
        }

        accountService.updateAccountWithdraw(dto, principal.getId());

        return "redirect:/account/list";
    }

    @GetMapping("/deposit")
    public String depositPage() {
        // 1. 인증 검사
        User principal = (User) session.getAttribute(Define.PRINCIPAL); // 다운 캐스팅
        if (principal == null) {
            throw new UnAuthorizedException(Define.ENTER_YOUR_LOGIN, HttpStatus.UNAUTHORIZED);
        }
        return "/account/deposit";
    }


    /**
     * 입금 기능 처리
     * @param DepositDTO
     * @return 계좌 목록 페이지
     */
    @PostMapping("/deposit")
    public String depositProc(DepositDTO dto) {
        // 1. 인증 검사
        User principal = (User) session.getAttribute(Define.PRINCIPAL); // 다운 캐스팅
        if (principal == null) {
            throw new UnAuthorizedException(Define.ENTER_YOUR_LOGIN, HttpStatus.UNAUTHORIZED);
        }

        // 2. 유효성 검사
        if (dto.getAmount() == null) {
            throw new DataDeliveryException(Define.ENTER_YOUR_BALANCE, HttpStatus.BAD_REQUEST);
        }
        if (dto.getAmount().longValue() <= 0) {
            throw new DataDeliveryException(Define.D_BALANCE_VALUE, HttpStatus.BAD_REQUEST);
        }
        if (dto.getDAccountNumber() == null) {
            throw new DataDeliveryException(Define.ENTER_YOUR_ACCOUNT_NUMBER, HttpStatus.BAD_REQUEST);
        }

        // 서비스 호출
        accountService.updateAccountDeposit(dto, principal.getId());

        return "redirect:/account/list";

    }

}