package edu.cit.bayonas.citmedconnect.features.auth.service;

import edu.cit.bayonas.citmedconnect.BaseUnitTest;
import edu.cit.bayonas.citmedconnect.features.auth.dto.UserDTO;
import edu.cit.bayonas.citmedconnect.features.auth.entity.UserEntity;
import edu.cit.bayonas.citmedconnect.features.auth.mapper.UserMapper;
import edu.cit.bayonas.citmedconnect.features.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("UserService Unit Tests")
class UserServiceTest extends BaseUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UserDTO testUserDTO;
    private UserEntity testUserEntity;

    @BeforeEach
    void setUp() {
        // Setup test data
        testUserDTO = new UserDTO();
        testUserDTO.setSchoolId("TEST001");
        testUserDTO.setFirstName("John");
        testUserDTO.setLastName("Doe");
        testUserDTO.setEmail("john.doe@test.com");
        testUserDTO.setPassword("password123");
        testUserDTO.setRole("STUDENT");
        testUserDTO.setPhone("1234567890");
        testUserDTO.setGender("MALE");
        testUserDTO.setAge(25);

        testUserEntity = new UserEntity();
        testUserEntity.setSchoolId("TEST001");
        testUserEntity.setFirstName("John");
        testUserEntity.setLastName("Doe");
        testUserEntity.setEmail("john.doe@test.com");
        testUserEntity.setPassword("encodedPassword");
        testUserEntity.setRole("STUDENT");
        testUserEntity.setPhone("1234567890");
        testUserEntity.setGender("MALE");
        testUserEntity.setAge(25);
    }

    @Nested
    @DisplayName("User Creation Tests")
    class UserCreationTests {

        @Test
        @DisplayName("Should create user successfully with valid data")
        void shouldCreateUserSuccessfully() {
            // Given
            when(userRepository.existsByEmail(testUserDTO.getEmail())).thenReturn(false);
            when(userMapper.toEntity(testUserDTO)).thenReturn(testUserEntity);
            when(passwordEncoder.encode(testUserDTO.getPassword())).thenReturn("encodedPassword");
            when(userRepository.save(any(UserEntity.class))).thenReturn(testUserEntity);
            when(userMapper.toDTO(testUserEntity)).thenReturn(testUserDTO);

            // When
            UserDTO result = userService.createUser(testUserDTO);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo(testUserDTO.getEmail());
            assertThat(result.getFirstName()).isEqualTo(testUserDTO.getFirstName());

            verify(userRepository).existsByEmail(testUserDTO.getEmail());
            verify(passwordEncoder).encode(testUserDTO.getPassword());
            verify(userRepository).save(any(UserEntity.class));
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void shouldThrowExceptionWhenEmailExists() {
            // Given
            when(userRepository.existsByEmail(testUserDTO.getEmail())).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> userService.createUser(testUserDTO))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Email already exists");

            verify(userRepository).existsByEmail(testUserDTO.getEmail());
            verify(userRepository, never()).save(any(UserEntity.class));
        }

        @Test
        @DisplayName("Should handle null password gracefully")
        void shouldHandleNullPassword() {
            // Given
            testUserDTO.setPassword(null);
            when(userRepository.existsByEmail(testUserDTO.getEmail())).thenReturn(false);
            when(userMapper.toEntity(testUserDTO)).thenReturn(testUserEntity);
            when(userRepository.save(any(UserEntity.class))).thenReturn(testUserEntity);
            when(userMapper.toDTO(testUserEntity)).thenReturn(testUserDTO);

            // When
            UserDTO result = userService.createUser(testUserDTO);

            // Then
            assertThat(result).isNotNull();
            verify(passwordEncoder, never()).encode(any());
        }
    }

    @Nested
    @DisplayName("User Retrieval Tests")
    class UserRetrievalTests {

        @Test
        @DisplayName("Should get all users successfully")
        void shouldGetAllUsersSuccessfully() {
            // Given
            List<UserEntity> userEntities = Arrays.asList(testUserEntity);
            List<UserDTO> expectedUsers = Arrays.asList(testUserDTO);
            
            when(userRepository.findAll()).thenReturn(userEntities);
            when(userMapper.toDTO(testUserEntity)).thenReturn(testUserDTO);

            // When
            List<UserDTO> result = userService.getAllUsers();

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getEmail()).isEqualTo(testUserDTO.getEmail());
            verify(userRepository).findAll();
        }

        @Test
        @DisplayName("Should get user by ID successfully")
        void shouldGetUserByIdSuccessfully() {
            // Given
            when(userRepository.findById("TEST001")).thenReturn(Optional.of(testUserEntity));
            when(userMapper.toDTO(testUserEntity)).thenReturn(testUserDTO);

            // When
            UserDTO result = userService.getUserById("TEST001");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getSchoolId()).isEqualTo("TEST001");
            verify(userRepository).findById("TEST001");
        }

        @Test
        @DisplayName("Should return null when user not found by ID")
        void shouldReturnNullWhenUserNotFound() {
            // Given
            when(userRepository.findById("NONEXISTENT")).thenReturn(Optional.empty());
            when(userMapper.toDTO(null)).thenReturn(null);

            // When
            UserDTO result = userService.getUserById("NONEXISTENT");

            // Then
            assertThat(result).isNull();
            verify(userRepository).findById("NONEXISTENT");
        }

        @Test
        @DisplayName("Should get user by email successfully")
        void shouldGetUserByEmailSuccessfully() {
            // Given
            when(userRepository.findByEmail("john.doe@test.com")).thenReturn(testUserEntity);
            when(userMapper.toDTO(testUserEntity)).thenReturn(testUserDTO);

            // When
            UserDTO result = userService.getUserByEmail("john.doe@test.com");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo("john.doe@test.com");
            verify(userRepository).findByEmail("john.doe@test.com");
        }
    }

    @Nested
    @DisplayName("User Authentication Tests")
    class UserAuthenticationTests {

        @Test
        @DisplayName("Should authenticate user with valid credentials")
        void shouldAuthenticateUserWithValidCredentials() {
            // Given
            String email = "john.doe@test.com";
            String password = "password123";
            String encodedPassword = "encodedPassword";

            testUserEntity.setPassword(encodedPassword);
            
            when(userRepository.findByEmail(email)).thenReturn(testUserEntity);
            when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);
            when(userMapper.toDTO(testUserEntity)).thenReturn(testUserDTO);

            // When
            UserDTO result = userService.authenticateUser(email, password);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo(email);
            verify(userRepository).findByEmail(email);
            verify(passwordEncoder).matches(password, encodedPassword);
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            // Given
            String email = "nonexistent@test.com";
            String password = "password123";
            
            when(userRepository.findByEmail(email)).thenReturn(null);

            // When & Then
            assertThatThrownBy(() -> userService.authenticateUser(email, password))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("User not found with email");

            verify(userRepository).findByEmail(email);
            verify(passwordEncoder, never()).matches(any(), any());
        }

        @Test
        @DisplayName("Should throw exception when password is invalid")
        void shouldThrowExceptionWhenPasswordInvalid() {
            // Given
            String email = "john.doe@test.com";
            String password = "wrongpassword";
            String encodedPassword = "encodedPassword";

            testUserEntity.setPassword(encodedPassword);
            
            when(userRepository.findByEmail(email)).thenReturn(testUserEntity);
            when(passwordEncoder.matches(password, encodedPassword)).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> userService.authenticateUser(email, password))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Invalid password");

            verify(userRepository).findByEmail(email);
            verify(passwordEncoder).matches(password, encodedPassword);
        }
    }

    @Nested
    @DisplayName("User Update Tests")
    class UserUpdateTests {

        @Test
        @DisplayName("Should update user by school ID successfully")
        void shouldUpdateUserBySchoolIdSuccessfully() {
            // Given
            UserDTO updateDTO = new UserDTO();
            updateDTO.setFirstName("Jane");
            updateDTO.setLastName("Smith");

            when(userRepository.findBySchoolId("TEST001")).thenReturn(testUserEntity);
            when(userRepository.save(testUserEntity)).thenReturn(testUserEntity);
            when(userMapper.toDTO(testUserEntity)).thenReturn(testUserDTO);

            // When
            UserDTO result = userService.updateUserBySchoolId("TEST001", updateDTO);

            // Then
            assertThat(result).isNotNull();
            verify(userRepository).findBySchoolId("TEST001");
            verify(userMapper).updateEntityFromDTO(updateDTO, testUserEntity);
            verify(userRepository).save(testUserEntity);
        }

        @Test
        @DisplayName("Should throw exception when updating non-existent user")
        void shouldThrowExceptionWhenUpdatingNonExistentUser() {
            // Given
            UserDTO updateDTO = new UserDTO();
            when(userRepository.findBySchoolId("NONEXISTENT")).thenReturn(null);

            // When & Then
            assertThatThrownBy(() -> userService.updateUserBySchoolId("NONEXISTENT", updateDTO))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("User not found with schoolId");

            verify(userRepository).findBySchoolId("NONEXISTENT");
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Password Management Tests")
    class PasswordManagementTests {

        @Test
        @DisplayName("Should change password successfully")
        void shouldChangePasswordSuccessfully() {
            // Given
            String schoolId = "TEST001";
            String currentPassword = "oldpassword";
            String newPassword = "newpassword";
            String encodedOldPassword = "encodedOldPassword";
            String encodedNewPassword = "encodedNewPassword";

            testUserEntity.setPassword(encodedOldPassword);

            when(userRepository.findBySchoolId(schoolId)).thenReturn(testUserEntity);
            when(passwordEncoder.matches(currentPassword, encodedOldPassword)).thenReturn(true);
            when(passwordEncoder.encode(newPassword)).thenReturn(encodedNewPassword);
            when(userRepository.save(testUserEntity)).thenReturn(testUserEntity);
            when(userMapper.toDTO(testUserEntity)).thenReturn(testUserDTO);

            // When
            UserDTO result = userService.changePassword(schoolId, currentPassword, newPassword);

            // Then
            assertThat(result).isNotNull();
            verify(userRepository).findBySchoolId(schoolId);
            verify(passwordEncoder).matches(currentPassword, encodedOldPassword);
            verify(passwordEncoder).encode(newPassword);
            verify(userRepository).save(testUserEntity);
        }

        @Test
        @DisplayName("Should throw exception when current password is incorrect")
        void shouldThrowExceptionWhenCurrentPasswordIncorrect() {
            // Given
            String schoolId = "TEST001";
            String currentPassword = "wrongpassword";
            String newPassword = "newpassword";
            String encodedPassword = "encodedPassword";

            testUserEntity.setPassword(encodedPassword);

            when(userRepository.findBySchoolId(schoolId)).thenReturn(testUserEntity);
            when(passwordEncoder.matches(currentPassword, encodedPassword)).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> userService.changePassword(schoolId, currentPassword, newPassword))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Current password is incorrect");

            verify(passwordEncoder, never()).encode(newPassword);
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("User Search and Utility Tests")
    class UserSearchAndUtilityTests {

        @Test
        @DisplayName("Should search users successfully")
        void shouldSearchUsersSuccessfully() {
            // Given
            String searchTerm = "John";
            List<UserEntity> searchResults = Arrays.asList(testUserEntity);
            
            when(userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                    searchTerm, searchTerm, searchTerm)).thenReturn(searchResults);
            when(userMapper.toDTO(testUserEntity)).thenReturn(testUserDTO);

            // When
            List<UserDTO> result = userService.searchUsers(searchTerm);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getFirstName()).containsIgnoringCase(searchTerm);
        }

        @Test
        @DisplayName("Should check if user exists by school ID")
        void shouldCheckIfUserExistsBySchoolId() {
            // Given
            when(userRepository.findBySchoolId("TEST001")).thenReturn(testUserEntity);

            // When
            boolean exists = userService.userExists("TEST001");

            // Then
            assertThat(exists).isTrue();
            verify(userRepository).findBySchoolId("TEST001");
        }

        @Test
        @DisplayName("Should check if email exists")
        void shouldCheckIfEmailExists() {
            // Given
            when(userRepository.existsByEmail("john.doe@test.com")).thenReturn(true);

            // When
            boolean exists = userService.existsByEmail("john.doe@test.com");

            // Then
            assertThat(exists).isTrue();
            verify(userRepository).existsByEmail("john.doe@test.com");
        }

        @Test
        @DisplayName("Should get user count")
        void shouldGetUserCount() {
            // Given
            when(userRepository.count()).thenReturn(5L);

            // When
            long count = userService.getUserCount();

            // Then
            assertThat(count).isEqualTo(5L);
            verify(userRepository).count();
        }
    }
}