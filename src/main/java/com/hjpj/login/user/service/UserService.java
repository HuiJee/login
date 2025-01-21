package com.hjpj.login.user.service;

import com.hjpj.login.user.dto.UserDTO;
import com.hjpj.login.exception.CustomException;
import com.hjpj.login.exception.ErrorCode;
import com.hjpj.login.user.repository.UserRepository;
import com.hjpj.login.common.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserDTO findByUserId(Long userId) {
        UserDTO user = userRepository.findUserByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        user.setUserRole(UserRole.getUserRole(user.getUserRole()));
        return user;
    }

}
