package com.hjpj.login.user.repository;

import com.hjpj.login.user.dto.UserDTO;
import com.hjpj.login.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 아이디 찾기 용
    @Query("SELECT u.userLogId from User u " +
            "WHERE u.userName = :userName and u.userEmail = :userEmail and u.userTel = :userTel and u.userStatus = true")
    String findIdByUserInfo(@Param("userName") String userName, @Param("userEmail") String userEmail, @Param("userTel") String userTel);

    // 비밀번호 찾기 용 (이메일 인증을 위해)
    @Query("SELECT u.userEmail from User u " +
            "WHERE u.userLogId = :userLogId and u.userEmail = :userEmail and u.userTel = :userTel and u.userStatus = true")
    String findEmailByUserInfo(@Param("userLogId") String userLogId, @Param("userEmail") String userEmail, @Param("userTel") String userTel);

    // 정보 찾기 (자동로그인 비교)
    @Query("SELECT new com.hjpj.login.user.dto.UserDTO(u.userId, u.userLogId, u.userLogPw, u.userNickname, u.userRole, u.loginType) " +
            "FROM User u WHERE u.userLogId = :userLogId and u.userStatus = true")
    Optional<UserDTO> findUserByUserLogId(@Param("userLogId") String userLogId);

    // 소셜 로그인 대상 찾기
    @Query("SELECT new com.hjpj.login.user.dto.UserDTO(u.userId, u.userLogId, u.userLogPw, u.userNickname, u.userRole, u.loginType) " +
            "FROM User u WHERE u.userLogId = :userLogId AND u.loginType = :loginType")
    Optional<UserDTO> findUserBySocialInfo(@Param("userLogId") String userLogId, @Param("loginType") String loginType);

    // 프로필 설정 용
    @Query("SELECT new com.hjpj.login.user.dto.UserDTO(u.userId, u.userLogId, null, u.userNickname, u.userRole, u.loginType) " +
            "FROM User u WHERE u.userId = :userId and u.userStatus = true")
    Optional<UserDTO> findUserByUserId(@Param("userId") Long userId);

    // 탈퇴 용 상태 변경
    @Modifying
    @Query("UPDATE User u SET u.userStatus = false, u.userUpdateDate = CURRENT_TIMESTAMP WHERE u.userLogId = :userLogId")
    int updateUserStatus(@Param("userLogId") String userLogId);

    // 아이디 중복 검사
    @Query("SELECT COUNT(1) FROM User u WHERE u.userLogId = :userLogId")
    int countUserLogId(@Param("userLogId") String userLogId);
    default boolean existsByUserLogId(String userLogId) {return countUserLogId(userLogId) > 0;}

    // 로그인 타입 확인
    @Query("SELECT u.loginType FROM User u WHERE u.userLogId = :userLogId")
    Integer userLoginType(@Param("userLogId") String userLogId);
}
