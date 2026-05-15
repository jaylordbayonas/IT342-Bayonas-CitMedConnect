package edu.cit.bayonas.citmedconnect.features.auth;

import edu.cit.bayonas.citmedconnect.BaseIntegrationTest;
import edu.cit.bayonas.citmedconnect.features.auth.dto.LoginRequest;
import edu.cit.bayonas.citmedconnect.features.auth.dto.RegisterRequest;
import edu.cit.bayonas.citmedconnect.features.auth.entity.UserEntity;
import edu.cit.bayonas.citmedconnect.features.auth.repository.UserRepository;
import edu.cit.bayonas.citmedconnect.features.appointments.repository.AppointmentRepository;
import edu.cit.bayonas.citmedconnect.features.appointments.repository.TimeSlotRepository;
import edu.cit.bayonas.citmedconnect.features.medicalrecords.repository.MedicalRecordRepository;
import edu.cit.bayonas.citmedconnect.features.notifications.repository.NotificationRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Rollback;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@DisplayName("Auth Integration Tests")
class AuthIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private RegisterRequest validRegisterRequest;
    private LoginRequest validLoginRequest;
    private UserEntity existingUser;

    @BeforeEach
    public void setUp() {
        super.setUp();

        // Clean database in correct order (child tables first)
        medicalRecordRepository.deleteAll();
        notificationRepository.deleteAll();
        appointmentRepository.deleteAll();
        timeSlotRepository.deleteAll();
        userRepository.deleteAll();
        userRepository.flush(); // Ensure deletion is committed

        // Setup test data
        validRegisterRequest = new RegisterRequest();
        validRegisterRequest.setName("John Doe");
        validRegisterRequest.setEmail("john.doe@test.com");
        validRegisterRequest.setPassword("password123");

        validLoginRequest = new LoginRequest();
        validLoginRequest.setEmail("existing@test.com");
        validLoginRequest.setPassword("password123");

        // Create existing user for login tests
        existingUser = new UserEntity();
        existingUser.setSchoolId("EXISTING001");
        existingUser.setFirstName("Existing");
        existingUser.setLastName("User");
        existingUser.setEmail("existing@test.com");
        existingUser.setPassword(passwordEncoder.encode("password123"));
        existingUser.setRole("STUDENT");
        existingUser.setPhone("1234567890");
        existingUser.setGender("MALE");
        existingUser.setAge(25);
        existingUser.setCreatedAt(new Date());
        userRepository.saveAndFlush(existingUser); // Ensure save is committed
    }

    @Nested
    @DisplayName("Registration Integration Tests")
    class RegistrationIntegrationTests {

        @Test
        @DisplayName("Should register new user successfully")
        void shouldRegisterNewUserSuccessfully() {
            given()
                    .contentType(ContentType.JSON)
                    .body(validRegisterRequest)
                    .when()
                    .post("/api/auth/register")
                    .then()
                    .statusCode(201)
                    .body("success", equalTo(true))
                    .body("message", equalTo("Registration successful"))
                    .body("token", notNullValue())
                    .body("user.name", equalTo("John Doe"))
                    .body("user.email", equalTo("john.doe@test.com"))
                    .body("user.schoolId", notNullValue());

            // Verify user was created in database
            UserEntity createdUser = userRepository.findByEmail("john.doe@test.com");
            assert createdUser != null;
            assert createdUser.getFirstName().equals("John");
            assert createdUser.getLastName().equals("Doe");
        }

        @Test
        @DisplayName("Should reject registration with duplicate email")
        void shouldRejectRegistrationWithDuplicateEmail() {
            // Use existing user's email
            validRegisterRequest.setEmail("existing@test.com");

            given()
                    .contentType(ContentType.JSON)
                    .body(validRegisterRequest)
                    .when()
                    .post("/api/auth/register")
                    .then()
                    .statusCode(409)
                    .body("success", equalTo(false))
                    .body("message", equalTo("Email already registered"));
        }

        @Test
        @DisplayName("Should validate required fields")
        void shouldValidateRequiredFields() {
            RegisterRequest invalidRequest = new RegisterRequest();
            invalidRequest.setName(""); // Empty name
            invalidRequest.setEmail("john.doe@test.com");
            invalidRequest.setPassword("password123");

            given()
                    .contentType(ContentType.JSON)
                    .body(invalidRequest)
                    .when()
                    .post("/api/auth/register")
                    .then()
                    .statusCode(400)
                    .body("success", equalTo(false))
                    .body("message", equalTo("Name is required"));
        }

        @Test
        @DisplayName("Should validate password length")
        void shouldValidatePasswordLength() {
            validRegisterRequest.setPassword("123"); // Too short

            given()
                    .contentType(ContentType.JSON)
                    .body(validRegisterRequest)
                    .when()
                    .post("/api/auth/register")
                    .then()
                    .statusCode(400)
                    .body("success", equalTo(false))
                    .body("message", equalTo("Password must be at least 6 characters"));
        }
    }

    @Nested
    @DisplayName("Login Integration Tests")
    class LoginIntegrationTests {

        @Test
        @DisplayName("Should login with valid credentials")
        void shouldLoginWithValidCredentials() {
            given()
                    .contentType(ContentType.JSON)
                    .body(validLoginRequest)
                    .when()
                    .post("/api/auth/login")
                    .then()
                    .statusCode(200)
                    .body("success", equalTo(true))
                    .body("message", equalTo("Login successful"))
                    .body("token", notNullValue())
                    .body("user.name", equalTo("Existing User"))
                    .body("user.email", equalTo("existing@test.com"))
                    .body("user.schoolId", equalTo("EXISTING001"));
        }

        @Test
        @DisplayName("Should reject login with invalid email")
        void shouldRejectLoginWithInvalidEmail() {
            validLoginRequest.setEmail("nonexistent@test.com");

            given()
                    .contentType(ContentType.JSON)
                    .body(validLoginRequest)
                    .when()
                    .post("/api/auth/login")
                    .then()
                    .statusCode(401)
                    .body("success", equalTo(false))
                    .body("message", containsString("User not found"));
        }

        @Test
        @DisplayName("Should reject login with invalid password")
        void shouldRejectLoginWithInvalidPassword() {
            validLoginRequest.setPassword("wrongpassword");

            given()
                    .contentType(ContentType.JSON)
                    .body(validLoginRequest)
                    .when()
                    .post("/api/auth/login")
                    .then()
                    .statusCode(401)
                    .body("success", equalTo(false))
                    .body("message", containsString("Invalid password"));
        }

        @Test
        @DisplayName("Should validate login request fields")
        void shouldValidateLoginRequestFields() {
            LoginRequest invalidRequest = new LoginRequest();
            invalidRequest.setEmail(""); // Empty email
            invalidRequest.setPassword("password123");

            given()
                    .contentType(ContentType.JSON)
                    .body(invalidRequest)
                    .when()
                    .post("/api/auth/login")
                    .then()
                    .statusCode(400)
                    .body("success", equalTo(false))
                    .body("message", equalTo("Email is required"));
        }
    }

    @Nested
    @DisplayName("User Management Integration Tests")
    class UserManagementIntegrationTests {

        @Test
        @DisplayName("Should get all users")
        void shouldGetAllUsers() {
            given()
                    .contentType(ContentType.JSON)
                    .when()
                    .get("/api/auth/users")
                    .then()
                    .statusCode(200)
                    .body("$", hasSize(1))
                    .body("[0].email", equalTo("existing@test.com"))
                    .body("[0].schoolId", equalTo("EXISTING001"));
        }

        @Test
        @DisplayName("Should get user by school ID")
        void shouldGetUserBySchoolId() {
            given()
                    .contentType(ContentType.JSON)
                    .when()
                    .get("/api/auth/users/EXISTING001")
                    .then()
                    .statusCode(200)
                    .body("schoolId", equalTo("EXISTING001"))
                    .body("email", equalTo("existing@test.com"))
                    .body("firstName", equalTo("Existing"))
                    .body("lastName", equalTo("User"));
        }

        @Test
        @DisplayName("Should return 404 for non-existent user")
        void shouldReturn404ForNonExistentUser() {
            given()
                    .contentType(ContentType.JSON)
                    .when()
                    .get("/api/auth/users/NONEXISTENT")
                    .then()
                    .statusCode(404)
                    .body("error", containsString("User not found"));
        }

        @Test
        @DisplayName("Should update user information")
        void shouldUpdateUserInformation() {
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("firstName", "Updated");
            updateData.put("lastName", "Name");
            updateData.put("phone", "9876543210");

            given()
                    .contentType(ContentType.JSON)
                    .body(updateData)
                    .when()
                    .put("/api/auth/users/EXISTING001")
                    .then()
                    .statusCode(200)
                    .body("firstName", equalTo("Updated"))
                    .body("lastName", equalTo("Name"))
                    .body("phone", equalTo("9876543210"));

            // Verify in database
            UserEntity updatedUser = userRepository.findBySchoolId("EXISTING001");
            assert updatedUser.getFirstName().equals("Updated");
            assert updatedUser.getLastName().equals("Name");
        }

        @Test
        @DisplayName("Should change user password")
        void shouldChangeUserPassword() {
            Map<String, String> passwordData = new HashMap<>();
            passwordData.put("currentPassword", "password123");
            passwordData.put("newPassword", "newpassword123");

            given()
                    .contentType(ContentType.JSON)
                    .body(passwordData)
                    .when()
                    .put("/api/auth/users/EXISTING001/password")
                    .then()
                    .statusCode(200)
                    .body("message", equalTo("Password changed successfully"));

            // Verify old password no longer works
            validLoginRequest.setPassword("password123");
            given()
                    .contentType(ContentType.JSON)
                    .body(validLoginRequest)
                    .when()
                    .post("/api/auth/login")
                    .then()
                    .statusCode(401);

            // Verify new password works
            validLoginRequest.setPassword("newpassword123");
            given()
                    .contentType(ContentType.JSON)
                    .body(validLoginRequest)
                    .when()
                    .post("/api/auth/login")
                    .then()
                    .statusCode(200);
        }

        @Test
        @DisplayName("Should search users")
        void shouldSearchUsers() {
            given()
                    .contentType(ContentType.JSON)
                    .queryParam("query", "Existing")
                    .when()
                    .get("/api/auth/users/search")
                    .then()
                    .statusCode(200)
                    .body("$", hasSize(1))
                    .body("[0].firstName", equalTo("Existing"));
        }

        @Test
        @DisplayName("Should check if email exists")
        void shouldCheckIfEmailExists() {
            given()
                    .contentType(ContentType.JSON)
                    .queryParam("email", "existing@test.com")
                    .when()
                    .get("/api/auth/users/email-exists")
                    .then()
                    .statusCode(200)
                    .body("exists", equalTo(true));

            given()
                    .contentType(ContentType.JSON)
                    .queryParam("email", "nonexistent@test.com")
                    .when()
                    .get("/api/auth/users/email-exists")
                    .then()
                    .statusCode(200)
                    .body("exists", equalTo(false));
        }

        @Test
        @DisplayName("Should get user count")
        void shouldGetUserCount() {
            given()
                    .contentType(ContentType.JSON)
                    .when()
                    .get("/api/auth/users/count")
                    .then()
                    .statusCode(200)
                    .body("totalUsers", equalTo(1));
        }

        @Test
        @DisplayName("Should delete user")
        void shouldDeleteUser() {
            given()
                    .contentType(ContentType.JSON)
                    .when()
                    .delete("/api/auth/users/EXISTING001")
                    .then()
                    .statusCode(200)
                    .body("message", equalTo("User deleted successfully"));

            // Verify user is deleted
            given()
                    .contentType(ContentType.JSON)
                    .when()
                    .get("/api/auth/users/EXISTING001")
                    .then()
                    .statusCode(404);
        }
    }

    @Nested
    @DisplayName("End-to-End Workflow Tests")
    class EndToEndWorkflowTests {

        @Test
        @DisplayName("Should complete full user lifecycle")
        void shouldCompleteFullUserLifecycle() {
            // 1. Register new user
            String registrationResponse = given()
                    .contentType(ContentType.JSON)
                    .body(validRegisterRequest)
                    .when()
                    .post("/api/auth/register")
                    .then()
                    .statusCode(201)
                    .extract().asString();

            // 2. Login with new user
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setEmail("john.doe@test.com");
            loginRequest.setPassword("password123");

            given()
                    .contentType(ContentType.JSON)
                    .body(loginRequest)
                    .when()
                    .post("/api/auth/login")
                    .then()
                    .statusCode(200)
                    .body("success", equalTo(true));

            // 3. Get user by email
            given()
                    .contentType(ContentType.JSON)
                    .when()
                    .get("/api/auth/users/email/john.doe@test.com")
                    .then()
                    .statusCode(200)
                    .body("email", equalTo("john.doe@test.com"));

            // 4. Update user information
            UserEntity createdUser = userRepository.findByEmail("john.doe@test.com");
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("phone", "5555555555");

            given()
                    .contentType(ContentType.JSON)
                    .body(updateData)
                    .when()
                    .put("/api/auth/users/" + createdUser.getSchoolId())
                    .then()
                    .statusCode(200)
                    .body("phone", equalTo("5555555555"));

            // 5. Change password
            Map<String, String> passwordData = new HashMap<>();
            passwordData.put("currentPassword", "password123");
            passwordData.put("newPassword", "newpassword456");

            given()
                    .contentType(ContentType.JSON)
                    .body(passwordData)
                    .when()
                    .put("/api/auth/users/" + createdUser.getSchoolId() + "/password")
                    .then()
                    .statusCode(200);

            // 6. Login with new password
            loginRequest.setPassword("newpassword456");
            given()
                    .contentType(ContentType.JSON)
                    .body(loginRequest)
                    .when()
                    .post("/api/auth/login")
                    .then()
                    .statusCode(200);
        }
    }
}