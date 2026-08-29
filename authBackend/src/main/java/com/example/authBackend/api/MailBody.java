package com.example.authBackend.api;

import lombok.Builder;

@Builder
public record MailBody(String to, String subject, String text) {
}
