package com.hjpj.login.controller;

import com.hjpj.login.dto.EmailCodeDTO;
import com.hjpj.login.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("mail")
@RequiredArgsConstructor
public class MailController {

    private final MailService mailService;

    @PostMapping("/send-code")
    public ResponseEntity<?> sendCode(@RequestBody String email) {
        System.out.println("메일 전송");
        mailService.sendCodeToEmail(email);
        return ResponseEntity.ok("인증코드가 전송되었습니다.");
    }

    @GetMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@RequestBody EmailCodeDTO dto) {
        boolean isVerified = mailService.verifyCode(dto.getEmail(), dto.getCode());
        if(isVerified) {
            return ResponseEntity.ok("인증에 성공했습니다.");
        }else {
            return ResponseEntity.status(2001).body("인증에 실패했습니다."); // 상태는 200으로 성공적이기에 2001로 임의 설정
        }
    }
}
