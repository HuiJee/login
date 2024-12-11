package com.hjpj.login.service;

import com.hjpj.login.dto.LoginInfoDTO;
import com.hjpj.login.dto.UserDTO;
import com.hjpj.login.dto.UserLogDetail;
import com.hjpj.login.entity.User;
import com.hjpj.login.exception.CustomException;
import com.hjpj.login.exception.ErrorCode;
import com.hjpj.login.jwt.JwtUtil;
import com.hjpj.login.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static java.util.Optional.of;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    // ResponseEntity로 넘기느냐 아니냐...
    public Map<String, Object> authUserLogin(String authHeader, LoginInfoDTO logInfo, HttpServletResponse response) {

        // 헤더 정보가 없는 경우
        if(authHeader == null || !authHeader.startsWith(JwtUtil.INITIAL_TYPE)) {
            throw new CustomException(ErrorCode.MISSING_TOKEN);
        }

        // 앞에 Basic을 제외한 정보 받기(회원 아이디 인코딩한 정보)
        String base64Credentials = authHeader.substring(JwtUtil.INITIAL_TYPE.length()).trim();

        // 위의 정보 디코딩하여 추출
        String credentials = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);
        System.out.println("userLogId : " + credentials);

        // 해당 ID를 가진 사용자 찾아서 없는 경우 에러 날리기
        UserDTO user = userRepository.findUserByUserInfo(credentials).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 해당 사용자를 AUTH에 담을 UserLogDetail 형태로 만들기
        UserLogDetail userLogDetail = new UserLogDetail(user);

        // 인증 정보 만들기
        Authentication auth = new UsernamePasswordAuthenticationToken(userLogDetail, credentials, userLogDetail.getAuthorities());
        
        // SecurityContext에 설정
        SecurityContextHolder.getContext().setAuthentication(auth);

        // 토큰 및 쿠키 생성 후 access만 전달받기
        String accessToken = tokenService.makeTokenAndCookie(auth, userLogDetail,response);

        if (passwordEncoder.matches(logInfo.getUserLogPw(), userLogDetail.getUserLogPw())) {
            Map<String, Object> result = new HashMap<>();
            result.put("user", userLogDetail);
//            result.put("accessToken", accessToken);

            System.out.println("성공적으로 로그인 로직 실행!");

            return result;
        } else {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }
    }

    public Map<String, String> findUserId(User user) {
        String userLogId = userRepository.findIdByUserInfo(user.getUserName(), user.getUserEmail(), user.getUserTel());

        if(userLogId == null) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        Map<String, String> result = new HashMap<>();
        result.put("userInfo", userLogId);

        return result;
    }

    public Map<String, String> findUserPw(User user) {
        String userEmail = userRepository.findEmailByUserInfo(user.getUserLogId(), user.getUserEmail(), user.getUserTel());

        if(userEmail == null) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        Map<String, String> result = new HashMap<>();
        result.put("userInfo", userEmail);

        return result;
    }

}
