package com.example.authBackend.service.implementation;

import com.example.authBackend.api.MailBody;
import com.example.authBackend.model.User;
import com.example.authBackend.repository.ForgetPasswordRepository;
import com.example.authBackend.repository.UserRepository;
import com.example.authBackend.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
    private final ForgetPasswordRepository forgetPasswordRepository;
    @Value("${spring.mail.username}")
    private String emailFrom;

    public void sendSimpleMessage(MailBody mailBody) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(mailBody.to());
        mailMessage.setFrom(emailFrom);
        mailMessage.setSubject(mailBody.subject());
        mailMessage.setText(mailBody.text());
        mailSender.send(mailMessage);
    }

    @Override
    public User verifyEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        return user.orElse(null);
    }

    @Override
    public void sendOtpEmail(String email,Long otp) {
        MailBody mailBody = MailBody.builder()
                .to(email)
                .subject("OTP for Forget Password Request")
                .text("OTP for Forget Password Request: "+otp)
                .build();
        sendSimpleMessage(mailBody);
    }


    @Scheduled(fixedRate = 30*60*1000)
    public void deleteExpiredOtpRecords() {
        forgetPasswordRepository.deleteByExpiryDateBefore(new Date());
    }

}
