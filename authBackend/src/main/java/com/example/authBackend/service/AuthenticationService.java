package com.example.authBackend.service;

public interface AuthenticationService {

    void authenticate(String email, String password);
}