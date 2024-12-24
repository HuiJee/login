package com.hjpj.login.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class PageController {

    /** 일반 로그인 페이지 이동 */
    @GetMapping({"login/generic", "/"})
    public String loginGeneric() {
        System.out.println("일반 로그인");
        return "login/generic";
    }

    /** 아이디 또는 pw 찾기 페이지 이동 */
    @GetMapping("login/find/{findTarget}")
    public String loginFindForm(@PathVariable String findTarget, Model model) {
        System.out.println("아이디/비번 찾기 : " + findTarget);
        model.addAttribute("target", findTarget);
        return "login/findForm";
    }

    /** 소셜 로그인 페이지 */
    @GetMapping("login/social")
    public String loginSocial() {
        System.out.println("소셜 로그인");
        return "login/social";
    }

    /** 프로필 페이지 (로그인 결과창) */
    @GetMapping("user/profile")
    public String userProfilePage() {
        System.out.println("로그인 성공!(프로필로...)");
        return "user/profile";
    }

    /** 회원가입 페이지 */
    @GetMapping("user/register")
    public String userRegisterPage() {
        System.out.println("회원가입 페이지");
        return "user/register";
    }

}
