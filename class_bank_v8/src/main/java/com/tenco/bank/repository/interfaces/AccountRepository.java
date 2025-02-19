package com.tenco.bank.repository.interfaces;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import com.tenco.bank.repository.model.Account;
import org.apache.ibatis.annotations.Param;

@Mapper // 반드시 작성
public interface AccountRepository {

    public int insert(Account account);
    public int updateById(Account account);
    // 주의! 파라미터가 2개 이상일 때 반드시 @Param 어노테이션을 사용하자.
    public int deleteById(@Param("id") Integer id, @Param("name") String name);
    public List<Account> findByUserId(@Param("userId") Integer principalId);
    public Account findByNumber(@Param("number") String id);

    // 계좌 조회 - 1 유저 , N 계좌
    // interface 파라미터명과 xml에 사용할 변수명을 다르게 해야 된다면 @Param
    // 어노테이션을 활용할 수 있습니다.
    // 2개 이상에 파라미터을 설계한다면 반드시 @Param 어노테이션을 지정해 주세요
    public List<Account> findAllByUserId(@Param("userId") Integer principalId);

    // 코드 추가
    // Account PK 로 조회 하는 기능
    public Account findByAccountId(Integer accountId);


}