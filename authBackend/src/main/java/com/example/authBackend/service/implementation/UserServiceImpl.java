package com.example.authBackend.service.implementation;

import com.example.authBackend.dto.UserDTO;
import com.example.authBackend.exceptions.ResourceNotFoundException;
import com.example.authBackend.helper.UserHelper;
import com.example.authBackend.model.User;
import com.example.authBackend.api.response.UserResponse;
import com.example.authBackend.repository.ForgetPasswordRepository;
import com.example.authBackend.repository.RefreshTokenRepository;
import com.example.authBackend.repository.UserRepository;
import com.example.authBackend.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ForgetPasswordRepository forgetPasswordRepository;

    @Override
    @Transactional
    public UserDTO createUser(UserDTO userDTO) {
        if (userDTO.getEmail()==null){
            throw new IllegalArgumentException("Email Is Required");
        }
        if (userRepository.findByEmail(userDTO.getEmail()).isPresent()){
            throw new IllegalArgumentException("Email Already Exists");
        }
        userDTO.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        User user = modelMapper.map(userDTO, User.class);
        User savedUser = userRepository.save(user);
        return modelMapper.map(savedUser, UserDTO.class);
    }

    @Override
    public UserDTO updateUser(UserDTO userDTO,String id) {
        UUID uId = UserHelper.parseUUID(id);
        User savedUser = userRepository.findById(uId)
                .orElseThrow(()-> new ResourceNotFoundException("User not found"));
        if(userDTO.getName()!=null){ savedUser.setName(userDTO.getName());}
        if (userDTO.getEmail()!=null) savedUser.setEmail(userDTO.getEmail());
        if (userDTO.getPassword()!=null) savedUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        savedUser.setUpdatedAt(Instant.now());
        savedUser.setEnable(true);
        User savedUser1 = userRepository.save(savedUser);
        return modelMapper.map(savedUser1, UserDTO.class);
    }

    @Override
    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(()-> new ResourceNotFoundException("Email not found"));;
        return modelMapper.map(user,UserDTO.class);
    }

    @Override
    @Transactional
    public void deleteUser(String userId) {
        UUID uId = UserHelper.parseUUID(userId);
        User user = userRepository.findById(uId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        forgetPasswordRepository.deleteByUser(user);
        refreshTokenRepository.deleteByUser(user);
        userRepository.delete(user);
    }

    @Override
    @Transactional
    public UserResponse getAllUsers(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")?Sort.by(sortBy).ascending():Sort.by(sortBy).descending();
        if (pageNumber==0) throw new BadCredentialsException("Page number must be greater than zero");
        Pageable pageable = PageRequest.of(pageNumber-1,pageSize,sortByAndOrder);
        Page<User> pageUser = userRepository.findAll(pageable);
        List<User> users = pageUser.getContent();
        List<UserDTO> userDTOList=users.stream().map(user -> modelMapper.map(user, UserDTO.class)).toList();
        UserResponse userResponse = new UserResponse();
        userResponse.setContent(userDTOList);
        userResponse.setTotalPages(pageUser.getTotalPages());
        userResponse.setPageNumber(pageUser.getNumber() + 1);
        userResponse.setPageSize(pageUser.getSize());
        userResponse.setSortOrder(sortOrder);
        userResponse.setSortBy(sortBy);
        userResponse.setLastPage(pageUser.isLast());
        return userResponse;
    }

}
