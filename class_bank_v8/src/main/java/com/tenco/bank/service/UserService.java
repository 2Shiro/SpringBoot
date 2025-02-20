package com.tenco.bank.service;

import com.tenco.bank.dto.SignInDTO;
import com.tenco.bank.dto.SignUpDTO;
import com.tenco.bank.handler.exception.DataDeliveryException;
import com.tenco.bank.handler.exception.RedirectException;
import com.tenco.bank.repository.interfaces.UserRepository;
import com.tenco.bank.repository.model.User;
import com.tenco.bank.utils.Define;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final PasswordEncoder passwordEncoder;

    // 초기 파라미터 들고 오는 방법
    @Value("${file.upload-dir}")
    private String uploadDir;

    @Transactional // 트랜 잭션 처리 습관
    public void createUser(SignUpDTO dto) {
        // Http 응답으로 클라이언트에게 전달할 오류 메시지는 최소한으로 유지하고,
        // 보안 및 사용자 경험 측면에서 민감한 정보를 노출하지 않도록 합니다.
        int result = 0;
        // 회원 가입시 Insert 처리 O/U
        System.out.println(dto.getCustomFile());
        if(dto.getCustomFile() != null && !dto.getCustomFile().isEmpty()) {
            // 업로드 로직 구현
            String[] fileNames = uploadFile(dto.getCustomFile());
            dto.setOriginFileName(fileNames[0]);
            dto.setUploadFileName(fileNames[1]);
        }

        try {
            // 코드 추가 부분
            String hashPwd = passwordEncoder.encode(dto.getPassword());
            dto.setPassword(hashPwd); // 상태 변경
            result = userRepository.insert(dto.toUser());
            // 여기서 예외 처리를 하면 상위 catch 블록에서 예외를 잡는다.
        } catch (DataAccessException e) {
            // DataAccessException는 Spring의 데이터 액세스 예외 클래스로,
            // 데이터베이스 연결이나 쿼리 실행과 관련된 문제를 처리합니다.
            throw new DataDeliveryException("잘못된 처리 입니다",HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            // 그 외 예외 처리 - 페이지 이동 처리
            throw new RedirectException("알 수 없는 오류" , HttpStatus.SERVICE_UNAVAILABLE);
        }
        // 예외 클래스가 발생이 안되지만 프로세스 입장에서 예외 상황으로 바라 봄
        if (result != 1) {
            // 삽입된 행의 수가 1이 아닌 경우 예외 발생
            throw new DataDeliveryException("회원 가입 실패", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    public User readUser(SignInDTO dto) {
        User user = null;
        try {

            // 한번에 아이디, 비밀번호
            // user = userRepository.findByUsernameAndPassword(dto.getUsername(), dto.getPassword());
            user = userRepository.findByUsername(dto.getUsername());

            // 여기서 예외 처리를 하면 상위 catch 블록에서 예외를 잡는다.
        } catch (DataAccessException e) {
            // DataAccessException는 Spring의 데이터 액세스 예외 클래스로,
            // 데이터베이스 연결이나 쿼리 실행과 관련된 문제를 처리합니다.
            throw new DataDeliveryException("잘못된 처리 입니다",HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            // 그 외 예외 처리 - 페이지 이동 처리
            throw new RedirectException("알 수 없는 오류" , HttpStatus.SERVICE_UNAVAILABLE);
        }

        if(user == null) {
            throw new DataDeliveryException("아이디 혹은 비번이 틀렸습니다.",
                    HttpStatus.BAD_REQUEST);
        }

        boolean isPwdMatched = passwordEncoder.matches(dto.getPassword(), user.getPassword());
        if(isPwdMatched == false) {
            throw new DataDeliveryException("비밀번호가 잘못되었습니다.", HttpStatus.BAD_REQUEST);
        }

        return user;
    }


    private String[] uploadFile(MultipartFile mFile) {

        // 방어적 코드 작성
        if(mFile.getSize() > Define.MAX_FILE_SIZE) {
            throw new DataDeliveryException("파일 크기는 20MB 이상 클 수 없습니다",
                    HttpStatus.BAD_REQUEST);
        }

        // 1. 파일 경로를 설정해 준다.
        String saveDirectory = uploadDir;
        // String saveDirectory = new File(uploadDir).getAbsolutePath();
        // 폴더가 존재 여부 코드를 작성해보자.
        File uploadFolder = new File(saveDirectory);
        if(!uploadFolder.exists()) {
            boolean mkdirsResult = uploadFolder.mkdir();
            if(!mkdirsResult) {
                throw new DataDeliveryException("파일 업로드 폴더를 생성 할 수 없습니다",
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        // 리눅스, MacOS 맞춰서 절대 경로를 맞춰야 한다.


        // 2. 파일명 중복되면 덮어쓰기 됩니다. 예방
        // 2.1 파일 이름을 생성한다.  adfjaldf100123_a.png
        String uploadFileName = UUID.randomUUID() + "_"+ mFile.getOriginalFilename();
        // 3. 파일명을 포함한 전체 경로를 만들자 -> 파일 전체 경로 + 새로 생성한 파일 명
        String uploadPath = saveDirectory + File.separator + uploadFileName;
        // 4. 파일 객체 만들기
        File destination = new File(uploadPath);



        try {
            mFile.transferTo(destination);
        } catch (IOException e) {
            e.printStackTrace();
            throw new DataDeliveryException("파일 업로드 중 오류가 발생", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // 파일까지 생성 --> 원본사진명, 새로 생성한 파일명
        return  new String[] {mFile.getOriginalFilename(), uploadFileName};
    }

    public User searchUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
