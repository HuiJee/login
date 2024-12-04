package com.hjph.login.controller;

import com.hjph.login.entity.User;
import com.hjph.login.service.LoginService;
import com.hjph.login.util.CommonUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

@Controller
@RequestMapping("login")
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;

    // 일반 로그인 페이지
    @GetMapping("/generic")
    public String loginGeneric() {
        System.out.println("일반 로그인");
        return "login/generic";
    }

    // 아이디 또는 pw 찾기 페이지
    @GetMapping("/find/{findTarget}")
    public String loginFindForm(@PathVariable String findTarget, Model model) {
        System.out.println("아이디/비번 찾기 : " + findTarget);
        model.addAttribute("target", findTarget);
        return "login/findForm";
    }

    // 아이디 또는 pw 찾기
    @PostMapping("/api/find/{findTarget}")
    public ResponseEntity<?> findIdOrPw(@PathVariable String findTarget,
                                        @RequestBody User user) {
        Map<String, String> result = Objects.equals(findTarget, CommonUtil.FIND_ID) ?
                loginService.findUserId(user) : loginService.findUserPw(user);

        return ResponseEntity.ok(result);
    }

    // 소셜 로그인 페이지
    @GetMapping("/social")
    public String loginSocial() {
        System.out.println("소셜 로그인");
        return "login/social";
    }
}
