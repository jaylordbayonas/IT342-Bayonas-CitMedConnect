package edu.cit.bayonas.citmedconnect.features.auth.repository;

import edu.cit.bayonas.citmedconnect.features.auth.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserRepository Integration Tests")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private UserEntity testUser1;
    private UserEntity testUser2;

    @BeforeEach
    void setUp() {
        testUser1 = new UserEntity();
        testUser1.setSchoolId("TEST001");
        testUser1.setFirstName("John");
        testUser1.setLastName("Doe");
        testUser1.setEmail("john.doe@test.com");
        testUser1.setPassword("encodedPassword1");
        testUser1.setRole("STUDENT");
        testUser1.setPhone("1234567890");
        testUser1.setGender("MALE");
        testUser1.setAge(25);
        testUser1.setCreatedAt(new Date());

        testUser2 = new UserEntity();
        testUser2.setSchoolId("TEST002");
        testUser2.setFirstName("Jane");
        testUser2.setLastName("Smith");
        testUser2.setEmail("jane.smith@test.com");
        testUser2.setPassword("encodedPassword2");
        testUser2.setRole("DOCTOR");
        testUser2.setPhone("0987654321");
        testUser2.setGender("FEMALE");
        testUser2.setAge(30);
        testUser2.setCreatedAt(new Date());
    }

    @Nested
    @DisplayName("Basic CRUD Operations")
    class BasicCrudOperations {

        @Test
        @DisplayName("Should save and find user by ID")
        void shouldSaveAndFindUserById() {
            // Given
            UserEntity savedUser = entityManager.persistAndFlush(testUser1);

            // When
            Optional<UserEntity> foundUser = userRepository.findById(savedUser.getSchoolId());

            // Then
            assertThat(foundUser).isPresent();
            assertThat(foundUser.get().getEmail()).isEqualTo("john.doe@test.com");
            assertThat(foundUser.get().getFirstName()).isEqualTo("John");
        }

        @Test
        @DisplayName("Should find all users")
        void shouldFindAllUsers() {
            // Given
            entityManager.persistAndFlush(testUser1);
            entityManager.persistAndFlush(testUser2);

            // When
            List<UserEntity> allUsers = userRepository.findAll();

            // Then
            assertThat(allUsers).hasSize(2);
            assertThat(allUsers).extracting(UserEntity::getEmail)
                    .containsExactlyInAnyOrder("john.doe@test.com", "jane.smith@test.com");
        }

        @Test
        @DisplayName("Should delete user by ID")
        void shouldDeleteUserById() {
            // Given
            UserEntity savedUser = entityManager.persistAndFlush(testUser1);
            String userId = savedUser.getSchoolId();

            // When
            userRepository.deleteById(userId);
            entityManager.flush();

            // Then
            Optional<UserEntity> deletedUser = userRepository.findById(userId);
            assertThat(deletedUser).isEmpty();
        }
    }

    @Nested
    @DisplayName("Custom Query Methods")
    class CustomQueryMethods {

        @Test
        @DisplayName("Should find user by email")
        void shouldFindUserByEmail() {
            // Given
            entityManager.persistAndFlush(testUser1);

            // When
            UserEntity foundUser = userRepository.findByEmail("john.doe@test.com");

            // Then
            assertThat(foundUser).isNotNull();
            assertThat(foundUser.getFirstName()).isEqualTo("John");
            assertThat(foundUser.getSchoolId()).isEqualTo("TEST001");
        }

        @Test
        @DisplayName("Should return null when email not found")
        void shouldReturnNullWhenEmailNotFound() {
            // When
            UserEntity foundUser = userRepository.findByEmail("nonexistent@test.com");

            // Then
            assertThat(foundUser).isNull();
        }

        @Test
        @DisplayName("Should check if email exists")
        void shouldCheckIfEmailExists() {
            // Given
            entityManager.persistAndFlush(testUser1);

            // When
            boolean exists = userRepository.existsByEmail("john.doe@test.com");
            boolean notExists = userRepository.existsByEmail("nonexistent@test.com");

            // Then
            assertThat(exists).isTrue();
            assertThat(notExists).isFalse();
        }

        @Test
        @DisplayName("Should find user by school ID")
        void shouldFindUserBySchoolId() {
            // Given
            entityManager.persistAndFlush(testUser1);

            // When
            UserEntity foundUser = userRepository.findBySchoolId("TEST001");

            // Then
            assertThat(foundUser).isNotNull();
            assertThat(foundUser.getEmail()).isEqualTo("john.doe@test.com");
        }

        @Test
        @DisplayName("Should find users by role")
        void shouldFindUsersByRole() {
            // Given
            entityManager.persistAndFlush(testUser1);
            entityManager.persistAndFlush(testUser2);

            // When
            List<UserEntity> students = userRepository.findByRole("STUDENT");
            List<UserEntity> doctors = userRepository.findByRole("DOCTOR");

            // Then
            assertThat(students).hasSize(1);
            assertThat(students.get(0).getFirstName()).isEqualTo("John");
            
            assertThat(doctors).hasSize(1);
            assertThat(doctors.get(0).getFirstName()).isEqualTo("Jane");
        }

        @Test
        @DisplayName("Should return empty list when no users found for role")
        void shouldReturnEmptyListWhenNoUsersFoundForRole() {
            // Given
            entityManager.persistAndFlush(testUser1);

            // When
            List<UserEntity> admins = userRepository.findByRole("ADMIN");

            // Then
            assertThat(admins).isEmpty();
        }
    }

    @Nested
    @DisplayName("Search Functionality")
    class SearchFunctionality {

        @Test
        @DisplayName("Should search users by first name containing")
        void shouldSearchUsersByFirstNameContaining() {
            // Given
            entityManager.persistAndFlush(testUser1);
            entityManager.persistAndFlush(testUser2);

            // When
            List<UserEntity> results = userRepository
                    .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                            "john", "john", "john");

            // Then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getFirstName()).isEqualTo("John");
        }

        @Test
        @DisplayName("Should search users by last name containing")
        void shouldSearchUsersByLastNameContaining() {
            // Given
            entityManager.persistAndFlush(testUser1);
            entityManager.persistAndFlush(testUser2);

            // When
            List<UserEntity> results = userRepository
                    .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                            "smith", "smith", "smith");

            // Then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getLastName()).isEqualTo("Smith");
        }

        @Test
        @DisplayName("Should search users by email containing")
        void shouldSearchUsersByEmailContaining() {
            // Given
            entityManager.persistAndFlush(testUser1);
            entityManager.persistAndFlush(testUser2);

            // When
            List<UserEntity> results = userRepository
                    .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                            "jane.smith", "jane.smith", "jane.smith");

            // Then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getEmail()).contains("jane.smith");
        }

        @Test
        @DisplayName("Should perform case insensitive search")
        void shouldPerformCaseInsensitiveSearch() {
            // Given
            entityManager.persistAndFlush(testUser1);

            // When
            List<UserEntity> results = userRepository
                    .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                            "JOHN", "JOHN", "JOHN");

            // Then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getFirstName()).isEqualTo("John");
        }

        @Test
        @DisplayName("Should return empty list when no matches found")
        void shouldReturnEmptyListWhenNoMatchesFound() {
            // Given
            entityManager.persistAndFlush(testUser1);

            // When
            List<UserEntity> results = userRepository
                    .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                            "nonexistent", "nonexistent", "nonexistent");

            // Then
            assertThat(results).isEmpty();
        }
    }

    @Nested
    @DisplayName("OAuth Integration")
    class OAuthIntegration {

        @Test
        @DisplayName("Should find user by OAuth provider and ID")
        void shouldFindUserByOAuthProviderAndId() {
            // Given
            testUser1.setOauthProvider("github");
            testUser1.setOauthId("12345");
            entityManager.persistAndFlush(testUser1);

            // When
            UserEntity foundUser = userRepository.findByOauthProviderAndOauthId("github", "12345");

            // Then
            assertThat(foundUser).isNotNull();
            assertThat(foundUser.getOauthProvider()).isEqualTo("github");
            assertThat(foundUser.getOauthId()).isEqualTo("12345");
            assertThat(foundUser.getEmail()).isEqualTo("john.doe@test.com");
        }

        @Test
        @DisplayName("Should return null when OAuth user not found")
        void shouldReturnNullWhenOAuthUserNotFound() {
            // When
            UserEntity foundUser = userRepository.findByOauthProviderAndOauthId("github", "nonexistent");

            // Then
            assertThat(foundUser).isNull();
        }
    }

    @Nested
    @DisplayName("Data Integrity Tests")
    class DataIntegrityTests {

        @Test
        @DisplayName("Should enforce unique email constraint")
        void shouldEnforceUniqueEmailConstraint() {
            // Given
            entityManager.persistAndFlush(testUser1);
            
            UserEntity duplicateEmailUser = new UserEntity();
            duplicateEmailUser.setSchoolId("TEST003");
            duplicateEmailUser.setFirstName("Bob");
            duplicateEmailUser.setLastName("Wilson");
            duplicateEmailUser.setEmail("john.doe@test.com"); // Same email as testUser1
            duplicateEmailUser.setPassword("password");
            duplicateEmailUser.setRole("STUDENT");
            duplicateEmailUser.setPhone("5555555555");
            duplicateEmailUser.setGender("MALE");
            duplicateEmailUser.setAge(28);
            duplicateEmailUser.setCreatedAt(new Date());

            // When & Then
            assertThatThrownBy(() -> {
                entityManager.persistAndFlush(duplicateEmailUser);
            }).isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("Should handle null values appropriately")
        void shouldHandleNullValuesAppropriately() {
            // Given
            UserEntity userWithNulls = new UserEntity();
            userWithNulls.setSchoolId("TEST003");
            userWithNulls.setFirstName("Bob");
            userWithNulls.setLastName("Wilson");
            userWithNulls.setEmail("bob.wilson@test.com");
            userWithNulls.setPassword("password");
            userWithNulls.setRole("STUDENT");
            userWithNulls.setPhone("5555555555");
            userWithNulls.setGender("MALE");
            userWithNulls.setAge(28);
            userWithNulls.setOauthProvider(null); // Nullable field
            userWithNulls.setOauthId(null); // Nullable field
            userWithNulls.setCreatedAt(new Date());

            // When
            UserEntity savedUser = entityManager.persistAndFlush(userWithNulls);

            // Then
            assertThat(savedUser.getOauthProvider()).isNull();
            assertThat(savedUser.getOauthId()).isNull();
            assertThat(savedUser.getEmail()).isNotNull();
        }
    }
}