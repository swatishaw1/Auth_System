package com.example.authBackend.security.oauth2.userInfo;

import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

import java.util.Map;

public class Oauth2UserInfoFactory {

    private Oauth2UserInfoFactory() {
       
    }


    public static OAuth2UserInfo getOauth2UserInfo(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> new GoogleOAuth2UserInfo(attributes);
            case "github" -> new GitHubOAuth2UserInfo(attributes);
            default -> throw new OAuth2AuthenticationException(
                    new OAuth2Error("unsupported_provider"),
                    "Provider [" + registrationId + "] is nto currently supported"
            );
        };
    }
}
