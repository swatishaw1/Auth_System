package com.example.authBackend.security.oauth2.userInfo;

import com.example.authBackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomeUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new UsernameNotFoundException("Username cannot be null or blank");
        }
        return userRepository.findByEmail(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with email: " + username));
    }
}
