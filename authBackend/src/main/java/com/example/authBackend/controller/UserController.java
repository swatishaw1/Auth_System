package com.example.authBackend.controller;

import com.example.authBackend.configuration.AppConstants;
import com.example.authBackend.dto.UserDTO;
import com.example.authBackend.api.response.UserResponse;
import com.example.authBackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<UserResponse> getAllTheUser(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_USER_RECORDS_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder
    ){
        return ResponseEntity.ok(userService.getAllUsers(pageNumber,pageSize,sortBy,sortOrder));
    }

    @PostMapping
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO userDTO){
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(userDTO));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserDTO> updateUser(@RequestBody UserDTO userDTO,@PathVariable String userId){
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(userService.updateUser(userDTO,userId));
    }
    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable String userId){
        userService.deleteUser(userId);
        return ResponseEntity.ok("User Deleted Successfully");
    }

    @GetMapping("/byId/{userId}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable String userId){
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(userService.getUserByID(userId));
    }

    @GetMapping("/byEmail/{email}")
    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email){
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(userService.getUserByEmail(email));
    }


}
