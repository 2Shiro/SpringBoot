package com.tenco.bank.service;

import com.tenco.bank.dto.*;
import com.tenco.bank.handler.exception.DataDeliveryException;
import com.tenco.bank.handler.exception.RedirectException;
import com.tenco.bank.repository.interfaces.AccountRepository;
import com.tenco.bank.repository.interfaces.HistoryRepository;
import com.tenco.bank.repository.model.Account;
import com.tenco.bank.repository.model.History;
import com.tenco.bank.utils.Define;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class AccountService {

    // @Autowired
    private final AccountRepository accountRepository;
    private final HistoryRepository historyRepository;

    public AccountService(AccountRepository accountRepository, HistoryRepository historyRepository) {
        this.accountRepository = accountRepository;
        this.historyRepository = historyRepository;
    }

    /**
     * 계좌 생성 기능
     *
     * @param dto
     * @param pricipalId
     */
    @Transactional
    public void createAccount(AccountSaveDTO dto, Integer pricipalId) {
        try {
            // 바로 save , 조회 하고 계좌 존재시 알려주기
            accountRepository.insert(dto.toAccount(pricipalId));
        } catch (DataAccessException e) {
            // DB연결 및 제약 사항 위한 및 쿼리 오류
            throw new DataDeliveryException("잘못된 처리 입니다", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            // 예외 처리 - 에러 페이지로 이동
            throw new RedirectException("알 수 없는 오류", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    /**
     * 복잡한 Select 쿼리문일 경우 트랜잭션 처리를 해주 것이 좋습니다.
     * 여기서는 단순한 Select 구문이라 바로 진행 합니다.
     * @param principalId
     * @return
     */
    public List<Account> readAccountListByUserId(Integer principalId) {
        List<Account> accountListEntity = null;
        try {
            accountListEntity = accountRepository.findAllByUserId(principalId);
        } catch (DataAccessException e) {
            throw new DataDeliveryException("잘못된 처리 입니다", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            // 예외 처리 - 에러 페이지로 이동
            throw new RedirectException("알 수 없는 오류", HttpStatus.SERVICE_UNAVAILABLE);
        }
        return accountListEntity;
    }

    // 출금 -->
    // 1. 트랜잭션 처리
    // 2. 계좌 번호 존재 여부 확인 -- select
    // 3. 본인 계좌 여부 확인 -- 객체에서 확인 가능
    // 4. 계좌에 비밀번호 확인 -- 객체에서 확인 가능
    // 5. 잔액 여부 확인(출금 가능 금액)
    // 6. 출금 처리 ----> update
    // 7. 거래 내역 등록 ----> insert(history)
    @Transactional
    public void updateAccountWithdraw(WithdrawalDTO dto, Integer principalId) {
        // 1000
        Account account = accountRepository.findByNumber(dto.getWAccountNumber());
        if(account == null) {
            throw new DataDeliveryException(Define.NOT_EXIST_ACCOUNT, HttpStatus.BAD_REQUEST);
        }
        account.checkOwner(principalId); // 아니리면 E,H 실행
        account.checkPassword(dto.getWAccountPassword());
        account.checkBalance(dto.getAmount());
        account.withdraw(dto.getAmount()); // 객체 상태 변경
        accountRepository.updateById(account);
        // History insrt 처리
        // History 객체 생성해서 넣기
        History history = new History();
        history.setAmount(dto.getAmount());
        history.setWBalance(account.getBalance());
        history.setDBalance(null);
        history.setWAccountId(account.getId());
        history.setDAccountId(null);
        // 히스토리 레파지토리 필요
        int rowResetCount = historyRepository.insert(history);
        if(rowResetCount != 1) {
            throw new DataDeliveryException(Define.FAILED_PROCESSING,
                  HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    // 임금 기능 만들기
    // 1. 트랜잭션
    // 2. 계좌 존재 여부 확인 ---> select --> Account 모델 리턴
    // 3. 본인 계좌 여부 확인 ---> 객체 상태값에서 확인 가능
    // 4. 입금 처리 --> update
    // 5. 거래내역 등록 --> history table --> insert
    public void updateAccountDeposit(DepositDTO dto, Integer principalId) {
// 1. 계좌 존재 여부 확인
        Account accountEntity = accountRepository.findByNumber(dto.getDAccountNumber());
        if (accountEntity == null) {
            throw new DataDeliveryException(Define.NOT_EXIST_ACCOUNT, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // 2. 본인 계좌 여부 확인
        accountEntity.checkOwner(principalId);

        // 3. 입금처리(객체 상태 변경 후 update 처리)
        accountEntity.deposit(dto.getAmount());
        accountRepository.updateById(accountEntity);

        // 4. history에 거래내역 등록
        History history = new History();
        history.setAmount(dto.getAmount());
        history.setWAccountId(null);
        history.setDAccountId(accountEntity.getId());
        history.setWBalance(null);
        history.setDBalance(accountEntity.getBalance());

        int rowResultCount = historyRepository.insert(history);
        if (rowResultCount != 1) {
            throw new DataDeliveryException(Define.FAILED_PROCESSING, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * 이체 기능 처리
     * @param dto
     * @param principalId
     */
    // 1. 트랜잭션 처리
    // 2. 출금 계좌 존재 여부 확인 --> select
    // 3. 출금 계좌 본인 소유 여부 확인 --> 객체 상태 값
    // 4. 출금 계좌 비밀번호 확인  --> 객체 상태 값
    // 5. 출금 계좌 잔액 여부 확인 --> 객체 상태 값
    // 6. 입금 계좌 존재 여부 확인 --> select
    // 7. 출금 계좌 잔액 수정 -->  객체 상태 값
    // 8. 출금 계좌 잔액 수정 -->  update
    // 9. 입금 계좌 객체 상태 변경 --> 객체 상태 값
    // 10. 입금 계좌 잔액 변경 --> update
    // 11. 거래 내역 등록 처리 --> insert 처리
    @Transactional
    public void updateAccountTransfer(TransferDTO dto, Integer principalId) {
        Account withdrawAccount = accountRepository.findByNumber(dto.getWAccountNumber());
        // 2. 출금 계좌 존재 여부 확인 --> select
        if(withdrawAccount == null) {
            throw new DataDeliveryException(Define.NOT_EXIST_ACCOUNT, HttpStatus.BAD_REQUEST);
        }
        // 3. 출금 계좌 본인 소유 여부 확인 --> 객체 상태 값
        withdrawAccount.checkOwner(principalId);
        // 4. 출금 계좌 비밀번호 확인  --> 객체 상태 값
        withdrawAccount.checkPassword(dto.getPassword());
        // 5. 출금 계좌 잔액 여부 확인 --> 객체 상태 값
        withdrawAccount.checkBalance(dto.getAmount());
        // 6. 입금 계좌 존재 여부 확인 --> select
        Account depositAccount = accountRepository.findByNumber(dto.getDAccountNumber());
        if(depositAccount == null) {
            throw new DataDeliveryException("상태방의 계좌 번호가 없습니다.", HttpStatus.BAD_REQUEST);
        }
        // 7. 출금 계좌 잔액 수정 -->  객체 상태 값
        withdrawAccount.withdraw(dto.getAmount());
        // 8. 출금 계좌 잔액 수정 -->  update
        accountRepository.updateById(withdrawAccount);
        // 9. 입금 계좌 객체 상태 변경 --> 객체 상태 값
        depositAccount.deposit(dto.getAmount());
        // 10. 입금 계좌 잔액 변경 --> update
        accountRepository.updateById(depositAccount);
        // 11. 거래 내역 등록 처리 --> insert 처리
        History history = History.builder()
                .amount(dto.getAmount()) // 이체 금액
                .wAccountId(withdrawAccount.getId()) // 출금 계좌 PK
                .dAccountId(depositAccount.getId())  // 입금 계좌 PK
                .wBalance(withdrawAccount.getBalance()) // 출금 계좌 잔액(그 시점)
                .dBalance(depositAccount.getBalance()) // 입금 계좌 잔액 (그 시점)
                .build();

         int resultRowCount = historyRepository.insert(history);
         if(resultRowCount != 1) {
            throw new DataDeliveryException(Define.FAILED_PROCESSING, HttpStatus.INTERNAL_SERVER_ERROR);
         }
    }

    /**
     * 단일 계좌 조회 기능
     * @param accountId
     * @return
     */
    public Account readAccountId(Integer accountId) {
        Account account =  accountRepository.findByAccountId(accountId);
        if(account == null) {
            throw new DataDeliveryException(Define.NOT_EXIST_ACCOUNT,
                    HttpStatus.BAD_REQUEST);
        }
        return  account;
    }

    /**
     * 단일 계좌 거래 내역 조회
     * @param type [all, deposit, withdrawal]
     * @param accountId (pk)
     * @return 입금, 출금, 입출금 거래내역 (3가지 타입으로 반환 처리)
     */
    public List<HistoryAccountDTO> readHistoryByAccountId(String type, Integer accountId, int page, int size) {
        List<HistoryAccountDTO> list = new ArrayList<>();
        // limit 변수, offset 변수
        int limit = size;
        int offset = (page - 1) * size;
        list = historyRepository.findByAccountIdAndTypeOfHistory(type, accountId, limit, offset);
        return list;
    }

    /**
     * 단일 해당 계좌와 거래 유형에 따른 전체 레코드 수를 반환하는 메서드
     * @param type
     * @param accountId
     * @return int
     */
    public int countHistoryByAccountAndType(String type, Integer accountId) {
        return historyRepository.countHistoryAccountIdAndType(type, accountId);
    }
}