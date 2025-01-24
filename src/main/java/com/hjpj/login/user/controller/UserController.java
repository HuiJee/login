package com.hjpj.login.user.controller;

import com.hjpj.login.auth.TokenService;
import com.hjpj.login.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/user/")
@RequiredArgsConstructor
public class UserController {

    private final TokenService tokenService;
    private final UserService userService;

    /** 리프레시 토큰으로 access 재발급*/
    @PostMapping("refresh-token")
    public ResponseEntity<?> getAccessFromRefresh(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("access from refresh");
        tokenService.getAccessFromRefreshByUserLogId(request, response);
        return ResponseEntity.ok("Access token refreshed");
    }

    /** user 정보 가져오기 */
    @GetMapping("{userId}")
    public ResponseEntity<?> getUser(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.findByUserId(userId));
    }

}
