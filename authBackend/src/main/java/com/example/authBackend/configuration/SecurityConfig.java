package com.example.authBackend.configuration;

import com.example.authBackend.filter.JwtAuthenticationFilter;
import com.example.authBackend.security.oauth2.OAuth2SuccessHandler;
import com.example.authBackend.security.oauth2.Oauth2FailureHandler;
import com.example.authBackend.security.oauth2.userInfo.CustomOAuth2UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.PrintWriter;
import java.util.List;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final Oauth2FailureHandler oauth2FailureHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorizeHttpRequest ->
                authorizeHttpRequest
                        .requestMatchers(AppConstants.AUTH_PUBLIC_URL).permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex.authenticationEntryPoint(unauthorizedEntryPoint()))
                .oauth2Login(oauth2 -> oauth2

                        // endpoint frontend hits to start the Oauth2 flow:
                        // GET /oauth2/authroized/google
                        // GET /oauth2/authrized/github
                        .authorizationEndpoint(endPoint -> endPoint.baseUri("/oauth2/authorization"))

                        // custom service that creates/updates the local User record
                        .userInfoEndpoint(endPoint -> endPoint
                                .userService(customOAuth2UserService))

                        // redirect frontend with jwt token on success
                        .successHandler(oAuth2SuccessHandler)

                        // redirect frontend with error message on failure
                        .failureHandler(oauth2FailureHandler)
                        // the callback URL registered in Google/GitHub console:
                        // /login/oauth2/code/google
                        // /login/oauth2/code/github
                        .redirectionEndpoint(endPoint -> endPoint.baseUri("/login/oauth2/code/*"))
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(AbstractHttpConfigurer::disable);
        return httpSecurity.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    protected AuthenticationEntryPoint unauthorizedEntryPoint() {
        return ((request, response, authException) -> {
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            PrintWriter writer = response.getWriter();
            String json = """
                    {
                      "error: "UNAUTHORIZED",
                      "message":%s
                    }
                    """.formatted(authException.getMessage());

            writer.write(json);
            writer.flush();
        });
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration){
        return configuration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.frontend-url}") String corsUrls) {
        String[] corsAllowedOrigins = corsUrls.trim().split(",");
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(corsAllowedOrigins));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE","OPTIONS","PATCH","HEAD"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);//For Passing Cookies
        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
