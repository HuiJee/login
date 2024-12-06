package com.hjph.login.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
@RequiredArgsConstructor
public class MailSettingService {

    private final JavaMailSender mailSender;

    public void sendEmail(String toMail, String title, String content) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(toMail);
        helper.setSubject(title);
        helper.setText(content, true); // true로 쓰면 HTML이 사용 가능하다
        helper.setReplyTo("testnz2412@gamil.com"); // 회신 불가능한 주소 설정

        mailSender.send(message);
    }

    public SimpleMailMessage createEmail(String toEmail, String title, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(title);
        message.setText(text);
        return message;
    }



//    public String sendMail(String email) {
//
//        Random r = new Random();
//        int checkNum = r.nextInt(888888) + 111111;
//
//        String title = "비밀번호 찾기 인증 이메일입니다.";
//        String to = email;
//        String content =
//                System.lineSeparator() +
//                System.lineSeparator() +
//                        "안녕하세요. Nizy를 방문해주셔서 감사합니다." +
//                        System.lineSeparator() +
//                        "요청하신 인증번호는 " + checkNum + "입니다." +
//                        System.lineSeparator();
//
//        try {
//            MimeMessage message = mailSender.createMimeMessage();
//            MimeMessageHelper messageHelper = new MimeMessageHelper(message, true, "UTF-8");
//
//            messageHelper.setFrom(from);
//            messageHelper.setTo(to);
//            messageHelper.setSubject(title);
//            messageHelper.setText(content, true);
//
//            mailSender.send(message);
//        } catch (MessagingException e) {
//            e.printStackTrace();
//        }
//        return checkNum+"";
//    }

}
