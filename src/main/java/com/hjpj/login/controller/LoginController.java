package com.hjpj.login.controller;

import com.hjpj.login.dto.LoginInfoDTO;
import com.hjpj.login.entity.User;
import com.hjpj.login.service.LoginService;
import com.hjpj.login.util.CommonUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/login/")
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;

    /** 아이디 또는 pw 찾기 */
    @PostMapping("find/{findTarget}")
    public ResponseEntity<?> findIdOrPw(@PathVariable String findTarget,
                                        @RequestBody User user) {
        Map<String, String> result = Objects.equals(findTarget, CommonUtil.FIND_ID) ?
                loginService.findUserId(user) : loginService.findUserPw(user);

        return ResponseEntity.ok(result);
    }

    /** 로그인 진행 */
    @PostMapping("auth-user")
    public ResponseEntity<?> authUserLogin(HttpServletRequest request,
                                      @RequestBody LoginInfoDTO logInfo,
                                      HttpServletResponse response) {
        System.out.println("로그인 진행");
        return ResponseEntity.ok(loginService.authUserLogin(request, logInfo, response));
    }

    /** 자동 로그인 정보 가져오기 */
    @GetMapping("auto-login")
    public ResponseEntity<?> autoLogin(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("자동로그인 실행");
        return ResponseEntity.ok(loginService.findUserForAuthLogin(request, response));
    }

    /** 로그아웃 진행 */
    @Transactional
    @GetMapping("sign-out")
    public ResponseEntity<?> signOut(HttpServletRequest request, HttpServletResponse response) {
        loginService.signOut(request, response);
        return ResponseEntity.ok("로그아웃 처리가 되었습니다.");
    }

}
