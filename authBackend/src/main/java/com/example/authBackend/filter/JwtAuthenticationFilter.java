package com.example.authBackend.filter;
import com.example.authBackend.repository.UserRepository;
import com.example.authBackend.utils.JwtService;
import io.jsonwebtoken.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        /*logger.info("Authorization header: {}", header);*/
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try{
                if (!jwtService.isAccessToken(token)) {// || SecurityContextHolder.getContext().getAuthentication() != null -> Already authenticated or not?
                    filterChain.doFilter(request, response);
                    return;
                }
                Jws<Claims> parse = jwtService.parse(token);
                Claims payload = parse.getPayload();
                String userId = payload.getSubject();
                UUID userUuid = UUID.fromString(userId);
                userRepository.findById(userUuid)
                        .ifPresent(user ->{
                            if (user.isEnabled()) {
                                List<GrantedAuthority> authorities = user.getRole() == null ? List.of() :
                                        List.of(new SimpleGrantedAuthority(user.getRole().toString()));
                                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                                        user.getEmail(),
                                        null,
                                        authorities
                                );
                                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                                if(SecurityContextHolder.getContext().getAuthentication() == null) SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                            }
                        });
            }catch (ExpiredJwtException e){
                logger.error("Token expired: {}", e.getMessage());
                request.setAttribute("error","Token expired");
            }catch (Exception e){
                logger.error("Invalid token: {}", e.getMessage());
                request.setAttribute("error","Invalid token");
            }
        }
        //Pass to the next filter in the chain
        logger.info("Request URI: {}, Authentication: {}", request.getRequestURI(), SecurityContextHolder.getContext().getAuthentication());
        filterChain.doFilter(request, response);
    }

    @Override
    //It doesn't apply the filter to the authentication endpoint
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return request.getRequestURI().startsWith("/api/v1/auth");
    }
}