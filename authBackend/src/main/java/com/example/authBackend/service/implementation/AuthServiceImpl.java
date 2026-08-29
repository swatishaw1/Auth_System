package com.example.authBackend.service.implementation;

import com.example.authBackend.Enum.Provider;
import com.example.authBackend.Enum.Role;
import com.example.authBackend.api.request.LoginRequest;
import com.example.authBackend.api.response.TokenResponse;
import com.example.authBackend.api.request.RefreshTokenRequest;
import com.example.authBackend.dto.UserDTO;
import com.example.authBackend.model.ForgetPassword;
import com.example.authBackend.model.RefreshToken;
import com.example.authBackend.model.User;
import com.example.authBackend.repository.ForgetPasswordRepository;
import com.example.authBackend.repository.UserRepository;
import com.example.authBackend.security.CookieService;
import com.example.authBackend.service.*;
import com.example.authBackend.utils.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final UserService userService;
    private final AuthenticationService authenticationService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final CookieService cookieService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${spring.otp.validation.time}")
    private Integer otpValidationTime;
    private final ForgetPasswordRepository forgetPasswordRepository;

    @Override
    public UserDTO registerUser(UserDTO userDTO) {
        return userService.createUser(userDTO);
    }

    @Override
    public TokenResponse loginUser(LoginRequest loginRequest, HttpServletResponse response) {
        User user = userRepository.findByEmail(loginRequest.email()).orElseThrow(() ->
                new BadCredentialsException("User Not Found Please Sign Up"));

        if (user.getProvider() == Provider.GOOGLE){
            throw new BadCredentialsException("You can't access this you used Google Previously Please use that for security purpose");
        }

        if (user.getProvider() == Provider.GITHUB){
            throw new BadCredentialsException("You can't access this you used Github Previously Please use that for security purpose");
        }
        authenticationService.authenticate(loginRequest.email(), loginRequest.password());
        if (!user.isEnabled()) {
            throw new BadCredentialsException("User is disabled");
        }
        if (user.getRole() == null) {
            user.setRole(Role.USER);
        }
        log.info("User {} logged in", user.getId());
        String jti = UUID.randomUUID().toString();
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshedToken(user, jti);
        refreshTokenService.createOrUpdate(user, jti);
        cookieService.attachRefreshCookie(response,refreshToken,(int)jwtService.getRefreshTokenValiditySeconds());
        cookieService.attachAccessTokenCookie(response,accessToken,(int)jwtService.getAccessTokenValiditySeconds()
        );
        cookieService.addNoStoreHeaders(response);
        return TokenResponse.of(accessToken,refreshToken, jwtService.getAccessTokenValiditySeconds(),
                modelMapper.map(user, UserDTO.class)
        );
    }

    @Override
    public TokenResponse refreshToken(RefreshTokenRequest body,HttpServletRequest request,HttpServletResponse response) {
        String refreshToken = readRefreshTokenFromRequest(body, request).orElseThrow(() ->  new BadCredentialsException("Refresh Token Missing"));
        RefreshToken storedToken = refreshTokenService.validate(refreshToken);
        User user = storedToken.getUser();
        String newJti = UUID.randomUUID().toString();
        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshedToken(user, newJti);
        refreshTokenService.rotate(storedToken,newJti, newRefreshToken);
        cookieService.attachRefreshCookie(response, newRefreshToken, (int) jwtService.getRefreshTokenValiditySeconds());
        cookieService.attachAccessTokenCookie(response, newAccessToken,(int) jwtService.getAccessTokenValiditySeconds());
        cookieService.addNoStoreHeaders(response);
        return TokenResponse.of(newAccessToken, newRefreshToken,
                jwtService.getAccessTokenValiditySeconds(), modelMapper.map(user, UserDTO.class));
    }

    @Override
    public ResponseEntity<String> verifyEmailAndSendOtp(String email) {
        User user = emailService.verifyEmail(email);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid Email Id");
        }
        Long otp = otpGenerator();
        emailService.sendOtpEmail(email,otp);
        ForgetPassword fp = ForgetPassword.builder()
                .otp(otp)
                .expiryDate(new Date(System.currentTimeMillis() + otpValidationTime))
                .user(user)
                .build();

        forgetPasswordRepository.save(fp);
        return ResponseEntity.ok("Email Sent for Verification");
    }

    @Override
    public ResponseEntity<String> verifyOtp(String email, String otp) {
        User user = userRepository.findByEmail(email).orElseThrow(() ->  new BadCredentialsException("User Not Found"));

        ForgetPassword fp = forgetPasswordRepository.findByOtpAndUser(Long.parseLong(otp),user).orElseThrow(() -> new RuntimeException("Invalid otp"));

        if (fp.getExpiryDate().before(Date.from(Instant.now()))) {
            forgetPasswordRepository.deleteById(fp.getId());
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("OTP Expired Resend OTP");
        }

        System.out.println("Current time: " + new Date());
        System.out.println("OTP expiry: " + fp.getExpiryDate());

        fp.setVerified(true);
        forgetPasswordRepository.save(fp);
        return ResponseEntity.ok("OTP Verified!");
    }

    @Override
    public ResponseEntity<String> resetPassword(String email, String rewrittenPassword, String password) {
        if (!rewrittenPassword.equals(password)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Passwords do not match");
        }
        User user = userRepository.findByEmail(email).orElseThrow(() ->  new BadCredentialsException("User not found"));
        ForgetPassword fp = forgetPasswordRepository.findByUserAndVerifiedTrue(user)
                        .orElseThrow(() -> new BadCredentialsException("OTP verification required"));
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        forgetPasswordRepository.delete(fp);
        return ResponseEntity.ok("Password Updated Successfully");
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        readRefreshTokenFromRequest(null, request).ifPresent(token -> {
            try {
                if (jwtService.isRefreshToken(token)) {
                    String jti = jwtService.getJwtId(token);
                    refreshTokenService.revoke(jti);
                }
            } catch (JwtException ignored) {
                log.error("Error while processing Refresh Token Message:{}", ignored.getMessage());
            }
        });
        cookieService.clearRefreshCookie(response);
        cookieService.clearAccessCookie(response);
        cookieService.addNoStoreHeaders(response);
        SecurityContextHolder.clearContext();
    }

    //Read data from the refresh token from the body
    public Optional<String> readRefreshTokenFromRequest(RefreshTokenRequest body, HttpServletRequest request){
        if (request.getCookies()!=null){
            Optional<String> fromCookie= Arrays.stream(request.getCookies())
                    .filter(c-> cookieService.getRefreshTokenCookieName().equals(c.getName()))
                    .map(Cookie::getValue)
                    .filter(v -> v!=null && !v.isBlank()).findFirst();
            if (fromCookie.isPresent()){
                return fromCookie;
            }
        }
        //2. Body
        if (body!=null && body.refreshToken()!=null && !body.refreshToken().isBlank()){
            return Optional.of(body.refreshToken().trim());
        }
        //3. Custome Header
        String refreshHeader = request.getHeader("X-Refresh-Token");
        if (refreshHeader!=null && !refreshHeader.isBlank()){
            return Optional.of(refreshHeader.trim());
        }
        //Authorization = Bearer <token>
        String authHeader =  request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader!=null && !authHeader.regionMatches(true,0,"Bearer ",0,7)) {
            String candidate = authHeader.substring(7).trim();
            if (!candidate.isEmpty()) {
                try {
                    if (jwtService.isRefreshToken(candidate)) {
                        return Optional.of(candidate);
                    }else {
                        return Optional.empty();
                    }
                } catch (Exception ignored) {
                    throw new BadCredentialsException("Unauthorized: "+ignored.getMessage());
                }
            }
        }
        return Optional.empty();
    }


    private Long otpGenerator(){
        Random random = new Random();
        return random.nextLong(100000,999999);
    }
}