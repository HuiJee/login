package com.hjph.login.service;

import com.hjph.login.entity.User;
import com.hjph.login.exception.CustomException;
import com.hjph.login.exception.ErrorCode;
import com.hjph.login.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

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
