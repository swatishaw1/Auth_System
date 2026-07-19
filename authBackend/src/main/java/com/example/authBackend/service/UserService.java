package com.example.authBackend.service;

import com.example.authBackend.dto.UserDTO;
import com.example.authBackend.api.response.UserResponse;

public interface UserService {
    UserDTO createUser(UserDTO userDTO);
    UserDTO updateUser(UserDTO userDTO,String id);
    UserDTO getUserByEmail(String email);
    UserDTO getUserByID(String userId);
    void deleteUser(String userId);
    UserResponse getAllUsers(
            Integer pageNumber, Integer pageSize, String sortBy, String sortOrder
    );
}
