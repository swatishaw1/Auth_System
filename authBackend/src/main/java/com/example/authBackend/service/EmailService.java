package com.example.authBackend.service;

import com.example.authBackend.api.MailBody;
import com.example.authBackend.model.User;

public interface EmailService {

    User verifyEmail(String email);

    void sendOtpEmail(String email,Long otpNumber);
}
