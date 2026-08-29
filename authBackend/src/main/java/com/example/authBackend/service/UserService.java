package com.example.authBackend.service;

import com.example.authBackend.dto.UserDTO;
import com.example.authBackend.api.response.UserResponse;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    UserDTO createUser(UserDTO userDTO);
    UserDTO updateUser(UserDTO userDTO,String id);
    UserDTO getUserByEmail(String email);
    void deleteUser(String userId);
    UserResponse getAllUsers(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

}
