package com.example.authBackend.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserDTO {
    private UUID id;
    private String name;
    private String email;
    private String image;
    private String password;
}
