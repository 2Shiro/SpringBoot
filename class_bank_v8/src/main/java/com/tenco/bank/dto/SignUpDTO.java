package com.tenco.bank.dto;

import com.tenco.bank.repository.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

// SignUpFormDTO
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignUpDTO {

    private String username;
    private String password;
    private String fullname;

    private MultipartFile customFile; // name 속성과 일치 시켜야 함
	private String originFileName;
	private String uploadFileName;

//	private String eMail;

    // DTO --> 변환
    public User toUser() {
        return User.builder()
                .username(this.username)
                .password(this.password)
                .fullname(this.fullname)
                .uploadFileName(this.uploadFileName)
                .originFileName(this.originFileName)
                .build();
    }

}