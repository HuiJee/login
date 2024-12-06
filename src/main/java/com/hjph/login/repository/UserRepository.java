package com.hjph.login.repository;

import com.hjph.login.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 아이디 찾기 용
    @Query("SELECT u.userLogId from User u " +
            "WHERE u.userName = :userName and u.userEmail = :userEmail and u.userTel = :userTel")
    String findIdByUserInfo(@Param("userName") String userName, @Param("userEmail") String userEmail, @Param("userTel") String userTel);

    // 비밀번호 찾기 용 (이메일 인증을 위해)
    @Query("SELECT u.userEmail from User u " +
            "WHERE u.userLogId = :userLogId and u.userEmail = :userEmail and u.userTel = :userTel")
    String findEmailByUserInfo(@Param("userLogId") String userLogId, @Param("userEmail") String userEmail, @Param("userTel") String userTel);

}
