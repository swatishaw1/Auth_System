package com.example.authBackend.helper;

import java.util.UUID;

public class UserHelper {
    public static UUID parseUUID(String uuid) {
        return UUID.fromString(uuid);
    }
}