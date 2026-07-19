package com.example.authBackend.api.request;

public record LoginRequest(
        String email,
        String password
        /*String provider*/     //Need to seperate the provider google, github, etc. in the future if we want to support multiple providers, for now we will only support email/password for login
) {
}
