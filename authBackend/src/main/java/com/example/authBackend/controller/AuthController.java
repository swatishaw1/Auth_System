package com.example.authBackend.controller;
import com.example.authBackend.api.request.*;
import com.example.authBackend.api.response.TokenResponse;
import com.example.authBackend.dto.UserDTO;
import com.example.authBackend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> loginUser(@RequestBody LoginRequest loginRequest, HttpServletResponse response){
        return ResponseEntity.status(HttpStatus.OK).body(authService.loginUser(loginRequest,response));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response){
        authService.logout(request,response);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(@RequestBody(required = false) RefreshTokenRequest body,
                                                      HttpServletResponse response, HttpServletRequest request){
        return ResponseEntity.status(HttpStatus.OK).body(authService.refreshToken(body,request,response));
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerUser(@RequestBody UserDTO userDTO){
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerUser(userDTO));
    }

    // Verify Email & Send OTP
    @PostMapping("/verifyEmail")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        return authService.verifyEmailAndSendOtp(request.email());
    }

    // Verify OTP
    @PostMapping("/verifyOtp")
    public ResponseEntity<String> verifyOtp(@RequestBody VerifyOtpRequest request) {
        return authService.verifyOtp(request.email(),request.otp());
    }


    // Reset Password
    @PostMapping("/resetPassword")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        return authService.resetPassword( request.email(),request.rewrittenPassword(),request.password());
    }
}
