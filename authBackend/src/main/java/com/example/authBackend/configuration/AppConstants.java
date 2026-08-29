package com.example.authBackend.configuration;

public class AppConstants {
    public static final String PAGE_NUMBER = "1";
    public static final String PAGE_SIZE = "5";
    public static final String SORT_USER_RECORDS_BY = "email";
    public static final String SORT_DIR = "asc";
    public static final String[] AUTH_PUBLIC_URL = {
            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/api/v1/auth/**",
    };
}
