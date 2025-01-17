package com.hjpj.login.email.repository;

import com.hjpj.login.email.entity.EmailCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface EmailCodeRepository extends JpaRepository<EmailCode, Long> {

    @Query("SELECT ec.expireDate FROM EmailCode ec WHERE ec.email = :email AND ec.code = :code")
    Optional<LocalDateTime> findExpireDateByEmailAndCode(@Param("email") String email, @Param("code") String code);

    @Modifying
    @Query("DELETE EmailCode ec WHERE ec.expireDate < :now")
    public void deleteExpiredCode(@Param("now") LocalDateTime now);

}
