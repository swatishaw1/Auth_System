package com.example.authBackend.repository;

import com.example.authBackend.model.ForgetPassword;
import com.example.authBackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Date;
import java.util.Optional;

public interface ForgetPasswordRepository extends JpaRepository<ForgetPassword, Long> {
    Optional<ForgetPassword> findByOtpAndUser(Long otp, User user);
    Optional<ForgetPassword> findByUserAndVerifiedTrue(User user);

    void deleteByExpiryDateBefore(Date expiryDateBefore);

    void deleteByUser(User user);
}
