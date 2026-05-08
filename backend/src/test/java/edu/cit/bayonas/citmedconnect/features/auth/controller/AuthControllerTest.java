package edu.cit.bayonas.citmedconnect.features.auth.controller;

import edu.cit.bayonas.citmedconnect.features.auth.dto.AuthResponse;
import edu.cit.bayonas.citmedconnect.features.auth.dto.LoginRequest;
import edu.cit.bayonas.citmedconnect.features.auth.dto.RegisterRequest;
import edu.cit.bayonas.citmedconnect.features.auth.dto.UserDTO;
import edu.cit.bayonas.citmedconnect.features.auth.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@DisplayName("AuthController Unit Tests")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private RegisterRequest validRegisterRequest;
    private LoginRequest validLoginRequest;
    private UserDTO testUserDTO;

    @BeforeEach
    void setUp() {
        validRegisterRequest = new RegisterRequest();
        validRegisterRequest.setName("John Doe");
        validRegisterRequest.setEmail("john.doe@test.com");
        validRegisterRequest.setPassword("password123");

        validLoginRequest = new LoginRequest();
        validLoginRequest.setEmail("john.doe@test.com");
        validLoginRequest.setPassword("password123");

        testUserDTO = new UserDTO();
        testUserDTO.setSchoolId("TEST001");
        testUserDTO.setFirstName("John");
        testUserDTO.setLastName("Doe");
        testUserDTO.setEmail("john.doe@test.com");
        testUserDTO.setRole("STUDENT");
    }

    @Nested
    @DisplayName("User Registration Tests")
    class UserRegistrationTests {

        @Test
        @DisplayName("Should register user successfully with valid data")
        void shouldRegisterUserSuccessfully() throws Exception {
            // Given
            when(userService.existsByEmail(validRegisterRequest.getEmail())).thenReturn(false);
            when(userService.createUser(any(UserDTO.class))).thenReturn(testUserDTO);

            // When & Then
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRegisterRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Registration successful"))
                    .andExpect(jsonPath("$.token").exists())
                    .andExpect(jsonPath("$.user.id").value("TEST001"))
                    .andExpect(jsonPath("$.user.name").value("John Doe"))
                    .andExpect(jsonPath("$.user.email").value("john.doe@test.com"));

            verify(userService).existsByEmail(validRegisterRequest.getEmail());
            verify(userService).createUser(any(UserDTO.class));
        }

        @Test
        @DisplayName("Should return error when name is missing")
        void shouldReturnErrorWhenNameMissing() throws Exception {
            // Given
            validRegisterRequest.setName(null);

            // When & Then
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRegisterRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Name is required"));

            verify(userService, never()).createUser(any());
        }

        @Test
        @DisplayName("Should return error when email is missing")
        void shouldReturnErrorWhenEmailMissing() throws Exception {
            // Given
            validRegisterRequest.setEmail(null);

            // When & Then
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRegisterRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Email is required"));

            verify(userService, never()).createUser(any());
        }

        @Test
        @DisplayName("Should return error when password is too short")
        void shouldReturnErrorWhenPasswordTooShort() throws Exception {
            // Given
            validRegisterRequest.setPassword("123");

            // When & Then
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRegisterRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Password must be at least 6 characters"));

            verify(userService, never()).createUser(any());
        }

        @Test
        @DisplayName("Should return conflict when email already exists")
        void shouldReturnConflictWhenEmailExists() throws Exception {
            // Given
            when(userService.existsByEmail(validRegisterRequest.getEmail())).thenReturn(true);

            // When & Then
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRegisterRequest)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Email already registered"));

            verify(userService).existsByEmail(validRegisterRequest.getEmail());
            verify(userService, never()).createUser(any());
        }

        @Test
        @DisplayName("Should handle service exception during registration")
        void shouldHandleServiceExceptionDuringRegistration() throws Exception {
            // Given
            when(userService.existsByEmail(validRegisterRequest.getEmail())).thenReturn(false);
            when(userService.createUser(any(UserDTO.class))).thenThrow(new RuntimeException("Database error"));

            // When & Then
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRegisterRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Database error"));
        }
    }

    @Nested
    @DisplayName("User Login Tests")
    class UserLoginTests {

        @Test
        @DisplayName("Should login user successfully with valid credentials")
        void shouldLoginUserSuccessfully() throws Exception {
            // Given
            when(userService.authenticateUser(validLoginRequest.getEmail(), validLoginRequest.getPassword()))
                    .thenReturn(testUserDTO);

            // When & Then
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validLoginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Login successful"))
                    .andExpect(jsonPath("$.token").exists())
                    .andExpect(jsonPath("$.user.id").value("TEST001"))
                    .andExpect(jsonPath("$.user.name").value("John Doe"))
                    .andExpect(jsonPath("$.user.email").value("john.doe@test.com"));

            verify(userService).authenticateUser(validLoginRequest.getEmail(), validLoginRequest.getPassword());
        }

        @Test
        @DisplayName("Should return error when email is missing")
        void shouldReturnErrorWhenLoginEmailMissing() throws Exception {
            // Given
            validLoginRequest.setEmail(null);

            // When & Then
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validLoginRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Email is required"));

            verify(userService, never()).authenticateUser(any(), any());
        }

        @Test
        @DisplayName("Should return error when password is missing")
        void shouldReturnErrorWhenLoginPasswordMissing() throws Exception {
            // Given
            validLoginRequest.setPassword(null);

            // When & Then
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validLoginRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Password is required"));

            verify(userService, never()).authenticateUser(any(), any());
        }

        @Test
        @DisplayName("Should return unauthorized when authentication fails")
        void shouldReturnUnauthorizedWhenAuthenticationFails() throws Exception {
            // Given
            when(userService.authenticateUser(validLoginRequest.getEmail(), validLoginRequest.getPassword()))
                    .thenThrow(new RuntimeException("Invalid credentials"));

            // When & Then
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validLoginRequest)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Invalid credentials"));

            verify(userService).authenticateUser(validLoginRequest.getEmail(), validLoginRequest.getPassword());
        }

        @Test
        @DisplayName("Should return null when user service returns null")
        void shouldReturnUnauthorizedWhenUserServiceReturnsNull() throws Exception {
            // Given
            when(userService.authenticateUser(validLoginRequest.getEmail(), validLoginRequest.getPassword()))
                    .thenReturn(null);

            // When & Then
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validLoginRequest)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Invalid email or password"));

            verify(userService).authenticateUser(validLoginRequest.getEmail(), validLoginRequest.getPassword());
        }
    }

    @Nested
    @DisplayName("User Management Endpoint Tests")
    class UserManagementEndpointTests {

        @Test
        @DisplayName("Should get all users successfully")
        void shouldGetAllUsersSuccessfully() throws Exception {
            // Given
            when(userService.getAllUsers()).thenReturn(java.util.Arrays.asList(testUserDTO));

            // When & Then
            mockMvc.perform(get("/api/auth/users")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].schoolId").value("TEST001"))
                    .andExpect(jsonPath("$[0].email").value("john.doe@test.com"));

            verify(userService).getAllUsers();
        }

        @Test
        @DisplayName("Should get user by school ID successfully")
        void shouldGetUserBySchoolIdSuccessfully() throws Exception {
            // Given
            when(userService.getUserById("TEST001")).thenReturn(testUserDTO);

            // When & Then
            mockMvc.perform(get("/api/auth/users/TEST001")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.schoolId").value("TEST001"))
                    .andExpect(jsonPath("$.email").value("john.doe@test.com"));

            verify(userService).getUserById("TEST001");
        }

        @Test
        @DisplayName("Should return not found when user does not exist")
        void shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
            // Given
            when(userService.getUserById("NONEXISTENT")).thenReturn(null);

            // When & Then
            mockMvc.perform(get("/api/auth/users/NONEXISTENT")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("User not found with school_id: NONEXISTENT"));

            verify(userService).getUserById("NONEXISTENT");
        }

        @Test
        @DisplayName("Should check if email exists")
        void shouldCheckIfEmailExists() throws Exception {
            // Given
            when(userService.existsByEmail("john.doe@test.com")).thenReturn(true);

            // When & Then
            mockMvc.perform(get("/api/auth/users/email-exists")
                    .param("email", "john.doe@test.com")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.exists").value(true));

            verify(userService).existsByEmail("john.doe@test.com");
        }

        @Test
        @DisplayName("Should get user count")
        void shouldGetUserCount() throws Exception {
            // Given
            when(userService.getUserCount()).thenReturn(5L);

            // When & Then
            mockMvc.perform(get("/api/auth/users/count")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalUsers").value(5));

            verify(userService).getUserCount();
        }
    }
}