package com.hjph.login.controller;

//import com.hjpj.myfunction.dto.UserLogDetail;
//import com.hjpj.myfunction.entity.User;
//import com.hjpj.myfunction.exception.CustomException;
//import com.hjpj.myfunction.service.LoginService;
//import com.hjpj.myfunction.service.TokenService;
//import com.hjpj.myfunction.util.CommonUtil;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@Controller
@RequestMapping("login")
//@RequiredArgsConstructor
public class LoginController {

//    private final LoginService loginService;
//    private final TokenService tokenService;

    // 일반 로그인 페이지
    @GetMapping("/generic")
    public String loginGeneric() {
        System.out.println("일반 로그인");
        return "login/generic";
    }

    // 로그인 진행
//    @PostMapping("/authentic")
//    public ResponseEntity<?> loginAuthentic(@RequestHeader("Authorization") String authHeader,
//                                            @RequestBody UserLogDetail logInfo,
//                                            HttpServletResponse response) {
//        System.out.println("로그인 진행!");
//        return loginService.authLogin(authHeader, logInfo, response);
//    }

    // 아이디 또는 pw 찾기 페이지
    @GetMapping("/find/{findTarget}")
    public String loginFindForm(@PathVariable String findTarget, Model model) {
        System.out.println("아이디/비번 찾기 : " + findTarget);
        model.addAttribute("target", findTarget);
        return "login/findForm";
    }

    // 아이디 또는 pw 찾기
    @PostMapping("/api/find/{findTarget}")
//    public ResponseEntity<?> findIdOrPw(@PathVariable String findTarget,
//                                        @RequestBody User user) {
//        ResponseEntity<?> result = Objects.equals(findTarget, CommonUtil.FIND_ID) ?
//                loginService.findUserId(user) : loginService.findUserPw(user);
//
//        return result;
//    }

    // 소셜 로그인 페이지
    @GetMapping("/social")
    public String loginSocial() {
        System.out.println("소셜 로그인");
        return "login/social";
    }

//    @GetMapping("/out")
//    public ResponseEntity<?> logout(@RequestHeader("UserLogId") String userLogId, HttpServletResponse response) {
//        try {
//            System.out.println("로그아웃");
//            return tokenService.deleteTokenCookie(userLogId, response);
//
//        } catch (CustomException e) {
//            // 서비스에서 던진 예외 처리
//            throw e;
//
//        } catch (Exception e) {
//            // 그 외 예외 처리
//            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while logging out.");
//        }
//    }
}
