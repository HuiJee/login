package com.hjpj.login.controller;

import com.hjpj.login.repository.UserRepository;
import com.hjpj.login.service.TokenService;
import com.hjpj.login.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("user")
@RequiredArgsConstructor
public class UserController {

    private final TokenService tokenService;
    private final UserService userService;

    /** 프로필 페이지 (로그인 결과창) */
    @GetMapping("/profile")
    public String userProfilePage() {
        return "login/profile";
    }

    /** 리프레시 토큰으로 access 재발급*/
    @PostMapping("/refresh-token")
    public ResponseEntity<?> getAccessFromRefresh(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("access from refresh");
        tokenService.getAccessFromRefresh(request, response);
        return ResponseEntity.ok("Access token refreshed");
    }

    /** user 정보 가져오기 */
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUser(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.findByUserId(userId));
    }

}
