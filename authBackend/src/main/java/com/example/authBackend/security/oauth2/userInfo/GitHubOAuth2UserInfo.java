package com.example.authBackend.security.oauth2.userInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;


public class GitHubOAuth2UserInfo extends OAuth2UserInfo {


    public GitHubOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return String.valueOf(attributes.get("id"));
    }

    @Override
    public String getName() {
        Object name = attributes.get("name");
        return name != null ? name.toString() : attributes.get("login").toString();
    }

    @Override
    public String getEmail() {
        Object email = attributes.get("email");
        return email == null ? null : email.toString();
    }

    @Override
    public String getImageUrl() {
        return attributes.get("avatar_url").toString();
    }

}
