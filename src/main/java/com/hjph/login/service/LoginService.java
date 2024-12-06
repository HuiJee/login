package com.hjph.login.service;

import com.hjph.login.entity.EmailCode;
import com.hjph.login.entity.User;
import com.hjph.login.exception.CustomException;
import com.hjph.login.exception.ErrorCode;
import com.hjph.login.repository.EmailCodeRepository;
import com.hjph.login.repository.UserRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final UserRepository userRepository;


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
