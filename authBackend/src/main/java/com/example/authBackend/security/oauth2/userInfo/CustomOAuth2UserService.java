package com.example.authBackend.security.oauth2.userInfo;

import com.example.authBackend.Enum.Provider;
import com.example.authBackend.Enum.Role;
import com.example.authBackend.model.User;
import com.example.authBackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    /**
     * called by spring security after it fetches the user's profile from
     * the OAuth2 provider google & GitHub, this method:
     * 1. extracts attributes from the provider response
     * 2. creates or updates the local user record
     * 3. returns a DefaultOAuth2User that spring security places in the security-context
     *
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        log.info("CUSTOM OAUTH SERVICE CALLED");
        try {
            return processOAuth2User(userRequest, oAuth2User);
        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            // warp non-auth exceptions so spring security's failure handler is invoked
            log.error("Failed to process OAuth2 user from provider [{}] : {}", userRequest.getClientRegistration().getRegistrationId(), e.getMessage(), e);
            throw new InternalAuthenticationServiceException(e.getMessage(), e);
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest request, OAuth2User oAuth2User) {
        String registrationId = request.getClientRegistration().getRegistrationId();
        log.info("CUSTOM OAUTH PROCESSED SERVICE CALLED");
        String userNameAttributeName = request.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
        log.info("USER NAME ATTRIBUTE NAME : {}", userNameAttributeName);

        // normalise provider specific attribute maps into a consistent interface
        OAuth2UserInfo userInfo = Oauth2UserInfoFactory.getOauth2UserInfo(registrationId, oAuth2User.getAttributes());

        //In Github if email is not in the url
        String email = userInfo.getEmail()==null?fetchGithubEmail(request.getAccessToken().getTokenValue()):userInfo.getEmail();
        log.info("USER EMAIL : {}", email);
        log.info("USER INFO name: {}", userInfo.getName());
        log.info("USER INFO picture: {}", userInfo.getImageUrl());
        log.info("USER INFO id: {}", userInfo.getId());

        if (email == null) {
            // GitHub users with private emails will hit this
            // instruct them to make their email public or add a fallback strategy.
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("email_not_found"),
                    "no email return from " + registrationId + ". Please make your email public in your " + registrationId + " account settings."
            );
        }

        Provider provider = Provider.valueOf(registrationId.toUpperCase());
        // primary lookup: by provider + providerId
        User user = userRepository.findByProviderAndProviderId(provider,userInfo.getId())
                .map(existing -> updateExistingUser(existing, userInfo))
                .orElseGet(() -> {
                    // secondary check: email exists under a different provider
                    if (userRepository.existsByEmail(email)) {
                        throw new OAuth2AuthenticationException(
                                new OAuth2Error("email_conflict"),
                                "An account with email [" + email + "] already exits. please log in with your original method."
                        );
                    }
                    log.info("OAuth Email: {}", email);
                    return registerNewUser(provider, userInfo,email);
                });

        Map<String, Object> enrichedAttributes = new HashMap<>(oAuth2User.getAttributes());
        enrichedAttributes.put("email", user.getEmail()); // normalize: always present

        return new DefaultOAuth2User(List.of(new SimpleGrantedAuthority(user.getRole().toString())), enrichedAttributes, userNameAttributeName);
    }

    private User registerNewUser(Provider provider, OAuth2UserInfo userInfo,String email) {
        // TODO: remove or comment on production
        log.info("registration new {} user: {}", provider, email);

        User newUser = User.builder()
                .email(email)
                .name(userInfo.getName())
                .image(userInfo.getImageUrl())
                .provider(provider)
                .providerId(userInfo.getId())
                .role(Role.USER) // default role for OAuth2 registrations
                .build();

        return userRepository.save(newUser);
    }

    private User updateExistingUser(User user, OAuth2UserInfo userInfo) {
        // refresh display name and avatar on every login so they stay in sync
        user.setName(userInfo.getName());
        user.setImage(userInfo.getImageUrl());
        return userRepository.save(user);
    }

    private String fetchGithubEmail(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<List<Map<String, Object>>> response =
                restTemplate.exchange(
                        "https://api.github.com/user/emails", HttpMethod.GET, entity,
                        new ParameterizedTypeReference<>() {});

        return response.getBody().stream()
                .filter(email ->
                        Boolean.TRUE.equals(email.get("primary")) && Boolean.TRUE.equals(email.get("verified")))
                .map(email -> (String) email.get("email"))
                .findFirst()
                .orElse(null);
    }
}
