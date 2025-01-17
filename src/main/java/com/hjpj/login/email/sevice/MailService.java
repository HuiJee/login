package com.hjpj.login.email.sevice;

import com.hjpj.login.email.entity.EmailCode;
import com.hjpj.login.email.repository.EmailCodeRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class MailService {

    private final MailSettingService mailSettingService;
    private final EmailCodeRepository repository;

    /** 메일 내용 설정해서 전송하기 (최종) */
    public void sendCodeToEmail(String email) {
        EmailCode emailCode = createVerificationCode(email);

        String title = "Test NZ 이메일 인증 번호";
        String content = "<html>" +
                "<body>" +
                "<h2>Test NZ 인증코드를 알려드립니다.</h2>" +
                "<p>인증코드 : <span style='color:darkred; font-weight:bold;'>" + emailCode.getCode() + "</span></p>" +
                "<p>해당 코드를 홈페이지 입력창에 입력하세요.</p>" +
                "<footer style='color:grey; font-size:small;'>" +
                "<p>※본 메일은 자동응답 메일로, 회신 불가합니다.</p>" +
                "</footer>" +
                "</body>" +
                "</html>";

        try {
            mailSettingService.sendEmail(email, title, content);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    /** 생성된 인증코드 저장 */
    public EmailCode createVerificationCode(String email) {
        String randomCode = generateRandomCode(6);
        EmailCode emailCode = new EmailCode(email, randomCode);

        return repository.save(emailCode);
    }

    /** 랜덤 인증 코드 생성 */
    public String generateRandomCode(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        ThreadLocalRandom random = ThreadLocalRandom.current();

        for(int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }

        return sb.toString();
    }

    /** 인증코드 유효성 검사 */
    public boolean verifyCode(String email, String code) {
        return repository.findExpireDateByEmailAndCode(email, code)
                .map(expireDate -> expireDate.isAfter(LocalDateTime.now()))
                .orElse(false);
    }

    /** 서버 계속 가동 시 매일 정오에 만료 코드 삭제 */
    @Transactional
    @Scheduled(cron = "0 0 12 * * ?")
    public void deleteExpiredCodes() {
        repository.deleteExpiredCode(LocalDateTime.now());
    }

}
