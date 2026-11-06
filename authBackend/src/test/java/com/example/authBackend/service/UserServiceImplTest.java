package com.example.authBackend.service;

import com.example.authBackend.api.response.UserResponse;
import com.example.authBackend.dto.UserDTO;
import com.example.authBackend.exceptions.ResourceNotFoundException;
import com.example.authBackend.model.User;
import com.example.authBackend.repository.ForgetPasswordRepository;
import com.example.authBackend.repository.RefreshTokenRepository;
import com.example.authBackend.repository.UserRepository;
import com.example.authBackend.service.implementation.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private ForgetPasswordRepository forgetPasswordRepository;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(passwordEncoder, userRepository, new ModelMapper(), refreshTokenRepository, forgetPasswordRepository);
    }

    // 1. Successful registration
    @Test
    void createUser_shouldRegisterUserSuccessfully() {

        UserDTO request = UserDTO.builder()
                .name("Swati")
                .email("swati@example.com")
                .password("plainPassword123")
                .build();

        when(userRepository.findByEmail("swati@example.com"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("plainPassword123"))
                .thenReturn("encodedPassword");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserDTO result = userService.createUser(request);

        assertNotNull(result);
        assertEquals("Swati", result.getName());
        assertEquals("swati@example.com", result.getEmail());
        assertEquals("encodedPassword", result.getPassword());

        verify(userRepository).findByEmail("swati@example.com");
        verify(passwordEncoder).encode("plainPassword123");
        verify(userRepository).save(any(User.class));
    }

    // 2. Email is null
    @Test
    void createUser_shouldThrowExceptionWhenEmailIsNull() {

        UserDTO request = UserDTO.builder()
                .name("Swati")
                .email(null)
                .password("plainPassword123")
                .build();

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(request)
        );

        verifyNoInteractions(userRepository);
        verifyNoInteractions(passwordEncoder);
    }

    // 3. Email already exists
    @Test
    void createUser_shouldThrowExceptionWhenEmailAlreadyExists() {

        UserDTO request = UserDTO.builder()
                .name("Swati")
                .email("swati@example.com")
                .password("plainPassword123")
                .build();

        User existingUser = User.builder()
                .name("Existing User")
                .email("swati@example.com")
                .build();

        when(userRepository.findByEmail("swati@example.com"))
                .thenReturn(Optional.of(existingUser));

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(request)
        );

        verify(userRepository).findByEmail("swati@example.com");
        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(passwordEncoder);
    }

    // 4. Password must be encoded before saving
    @Test
    void createUser_shouldSaveEncodedPassword() {

        UserDTO request = UserDTO.builder()
                .name("Swati")
                .email("swati@example.com")
                .password("plainPassword123")
                .build();

        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("plainPassword123"))
                .thenReturn("bcryptHash");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        userService.createUser(request);

        ArgumentCaptor<User> userCaptor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertEquals("bcryptHash", savedUser.getPassword());
        assertNotEquals("plainPassword123", savedUser.getPassword());
    }

    // 5. Repository failure should propagate
    @Test
    void createUser_shouldPropagateRepositoryException() {

        UserDTO request = UserDTO.builder()
                .name("Swati")
                .email("swati@example.com")
                .password("plainPassword123")
                .build();

        when(userRepository.findByEmail("swati@example.com"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("plainPassword123"))
                .thenReturn("bcryptHash");

        when(userRepository.save(any(User.class)))
                .thenThrow(new RuntimeException("Database error"));

        assertThrows(
                RuntimeException.class,
                () -> userService.createUser(request)
        );
    }


    @Test
    void getUserByEmail_shouldReturnUserWhenEmailExists() {
        String email = "test@example.com";

        User user = User.builder()
                .id(UUID.randomUUID())
                .name("Test User")
                .email(email)
                .password("encodedPassword")
                .build();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        UserDTO result = userService.getUserByEmail(email);

        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertEquals("Test User", result.getName());

        verify(userRepository).findByEmail(email);
    }

    @Test
    void getUserByEmail_shouldThrowExceptionWhenUserDoesNotExist() {
        String email = "unknown@example.com";

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.getUserByEmail(email)
        );

        verify(userRepository).findByEmail(email);
    }

    @Test
    void getUserByEmail_shouldPropagateRepositoryException() {
        String email = "test@example.com";

        when(userRepository.findByEmail(email))
                .thenThrow(new RuntimeException("Database error"));

        assertThrows(
                RuntimeException.class,
                () -> userService.getUserByEmail(email)
        );
    }

    @Test
    void getAllUsers_shouldReturnPaginatedUsersSuccessfully() {

        // Arrange: Create sample users and mock a database page containing them.
        User user1 = User.builder()
                .id(UUID.randomUUID())
                .name("Alice")
                .email("alice@gmail.com")
                .build();

        User user2 = User.builder()
                .id(UUID.randomUUID())
                .name("Bob")
                .email("bob@gmail.com")
                .build();

        Pageable pageable = PageRequest.of(0, 5);

        Page<User> userPage = new PageImpl<>(
                List.of(user1, user2),
                pageable,
                2
        );

        when(userRepository.findAll(any(Pageable.class)))
                .thenReturn(userPage);

        // Act: Request the first page with five users per page.
        UserResponse response = userService.getAllUsers(1, 5, "name", "asc");

        // Assert: Verify that the service returns the expected users.
        assertNotNull(response);
        assertEquals(2, response.getContent().size());
        assertEquals("Alice", response.getContent().get(0).getName());
        assertEquals("Bob", response.getContent().get(1).getName());
    }


    @Test
    void getAllUsers_shouldReturnEmptyResult() {

        // Arrange: Mock an empty database page.
        Page<User> emptyPage = Page.empty(PageRequest.of(0, 5));

        when(userRepository.findAll(any(Pageable.class)))
                .thenReturn(emptyPage);

        // Act: Request users from the service.
        UserResponse response = userService.getAllUsers(1, 5, "name", "asc");

        // Assert: Verify that the response contains an empty user list.
        assertNotNull(response);
        assertTrue(response.getContent().isEmpty());
    }

    @Test
    void getAllUsers_shouldReturnCorrectPaginationMetadata() {

        // Arrange: Create two users on the current page, with twelve users in total.
        User user1 = User.builder()
                .id(UUID.randomUUID())
                .name("Alice")
                .email("alice@gmail.com")
                .build();

        User user2 = User.builder()
                .id(UUID.randomUUID())
                .name("Bob")
                .email("bob@gmail.com")
                .build();

        Pageable pageable = PageRequest.of(0, 5);

        Page<User> userPage = new PageImpl<>(
                List.of(user1, user2),
                pageable,
                12
        );

        when(userRepository.findAll(any(Pageable.class)))
                .thenReturn(userPage);

        // Act: Request page one with five users per page.
        UserResponse response = userService.getAllUsers(1, 5, "name", "asc");

        // Assert: Verify page number, size, total pages and last-page status.
        assertEquals(1, response.getPageNumber());
        assertEquals(5, response.getPageSize());
        assertEquals(3, response.getTotalPages());
        assertFalse(response.isLastPage());

        // Assert: Verify sorting information is correctly returned.
        assertEquals("name", response.getSortBy());
        assertEquals("asc", response.getSortOrder());
    }

    @Test
    void getAllUsers_shouldRejectZeroPageNumber() {

        // Act and Assert: Page numbers must start from one.
        assertThrows(
                BadCredentialsException.class,
                () -> userService.getAllUsers(0, 5, "name", "asc")
        );
    }

    @Test
    void getAllUsers_shouldRejectNegativePageNumber() {

        // Act and Assert: Negative page numbers must not be accepted.
        assertThrows(
                IllegalArgumentException.class,
                () -> userService.getAllUsers(-1, 5, "name", "asc")
        );
    }

    @Test
    void getAllUsers_shouldRejectNullSortOrder() {

        // Act and Assert: Sort order must be provided.
        assertThrows(
                NullPointerException.class,
                () -> userService.getAllUsers(1, 5, "name", null)
        );
    }
}