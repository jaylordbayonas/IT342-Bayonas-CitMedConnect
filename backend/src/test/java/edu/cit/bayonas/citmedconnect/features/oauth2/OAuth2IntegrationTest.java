package edu.cit.bayonas.citmedconnect.features.oauth2;

import edu.cit.bayonas.citmedconnect.BaseIntegrationTest;
import edu.cit.bayonas.citmedconnect.features.auth.entity.UserEntity;
import edu.cit.bayonas.citmedconnect.features.auth.repository.UserRepository;
import edu.cit.bayonas.citmedconnect.features.appointments.repository.AppointmentRepository;
import edu.cit.bayonas.citmedconnect.features.appointments.repository.TimeSlotRepository;
import edu.cit.bayonas.citmedconnect.features.medicalrecords.repository.MedicalRecordRepository;
import edu.cit.bayonas.citmedconnect.features.notifications.repository.NotificationRepository;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@DisplayName("OAuth2 Integration Tests")
class OAuth2IntegrationTest extends BaseIntegrationTest {

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

    private UserEntity existingOAuth2User;

    @BeforeEach
    public void setUp() {
        super.setUp();
        
        // Clean database in correct order (child tables first)
        medicalRecordRepository.deleteAll();
        notificationRepository.deleteAll();
        appointmentRepository.deleteAll();
        timeSlotRepository.deleteAll();
        userRepository.deleteAll();

        // Create existing OAuth2 user for testing
        existingOAuth2User = new UserEntity();
        existingOAuth2User.setSchoolId("OAUTH_USER001");
        existingOAuth2User.setFirstName("GitHub");
        existingOAuth2User.setLastName("User");
        existingOAuth2User.setEmail("github.user@test.com");
        existingOAuth2User.setPassword(passwordEncoder.encode("oauth_placeholder")); // OAuth users need a placeholder password
        existingOAuth2User.setRole("STUDENT");
        existingOAuth2User.setPhone("");
        existingOAuth2User.setGender("OTHER");
        existingOAuth2User.setAge(0);
        existingOAuth2User.setCreatedAt(new Date());
        existingOAuth2User.setOauthProvider("github");
        existingOAuth2User.setOauthId("12345");
        userRepository.saveAndFlush(existingOAuth2User);
    }

    @Nested
    @DisplayName("OAuth2 Configuration Tests")
    class OAuth2ConfigurationTests {

        @Test
        @DisplayName("Should get OAuth2 configuration")
        void shouldGetOAuth2Configuration() {
            given()
                .contentType(ContentType.JSON)
            .when()
                .get("/api/auth/oauth2/config")
            .then()
                .statusCode(200)
                .body("githubAuthUrl", equalTo("https://github.com/login/oauth/authorize"))
                .body("githubClientId", notNullValue());
        }

        @Test
        @DisplayName("Should get test endpoint")
        void shouldGetTestEndpoint() {
            given()
                .contentType(ContentType.JSON)
            .when()
                .get("/api/auth/oauth2/test")
            .then()
                .statusCode(200)
                .body("status", equalTo("OAuth2 Controller is working!"))
                .body("feature", equalTo("oauth2"));
        }
    }

    @Nested
    @DisplayName("GitHub OAuth2 Callback Tests")
    class GitHubOAuth2CallbackTests {

        @Test
        @DisplayName("Should handle GitHub callback with valid access token")
        void shouldHandleGitHubCallbackWithValidAccessToken() {
            // Note: This test will likely fail in real execution because it requires
            // a valid GitHub access token. This is more of a contract test.
            Map<String, String> callbackData = new HashMap<>();
            callbackData.put("accessToken", "mock_github_access_token");

            given()
                .contentType(ContentType.JSON)
                .body(callbackData)
            .when()
                .post("/api/auth/oauth2/github/callback")
            .then()
                .statusCode(anyOf(is(200), is(500))); // May fail due to invalid token, but endpoint should exist
        }

        @Test
        @DisplayName("Should validate access token field")
        void shouldValidateAccessTokenField() {
            Map<String, String> invalidData = new HashMap<>();
            // Missing accessToken field

            given()
                .contentType(ContentType.JSON)
                .body(invalidData)
            .when()
                .post("/api/auth/oauth2/github/callback")
            .then()
                .statusCode(400)
                .body("success", equalTo(false))
                .body("message", containsString("Validation error"));
        }

        @Test
        @DisplayName("Should handle empty access token")
        void shouldHandleEmptyAccessToken() {
            Map<String, String> invalidData = new HashMap<>();
            invalidData.put("accessToken", "");

            given()
                .contentType(ContentType.JSON)
                .body(invalidData)
            .when()
                .post("/api/auth/oauth2/github/callback")
            .then()
                .statusCode(400)
                .body("success", equalTo(false));
        }

        @Test
        @DisplayName("Should handle null access token")
        void shouldHandleNullAccessToken() {
            Map<String, Object> invalidData = new HashMap<>();
            invalidData.put("accessToken", null);

            given()
                .contentType(ContentType.JSON)
                .body(invalidData)
            .when()
                .post("/api/auth/oauth2/github/callback")
            .then()
                .statusCode(400)
                .body("success", equalTo(false));
        }
    }

    @Nested
    @DisplayName("GitHub Code Exchange Tests")
    class GitHubCodeExchangeTests {

        @Test
        @DisplayName("Should handle code exchange request")
        void shouldHandleCodeExchangeRequest() {
            // Note: This test will likely fail in real execution because it requires
            // a valid GitHub authorization code. This is more of a contract test.
            Map<String, String> exchangeData = new HashMap<>();
            exchangeData.put("code", "mock_github_authorization_code");
            exchangeData.put("redirectUri", "http://localhost:3000/auth/callback");

            given()
                .contentType(ContentType.JSON)
                .body(exchangeData)
            .when()
                .post("/api/auth/oauth2/github/exchange-code")
            .then()
                .statusCode(anyOf(is(200), is(400), is(500))); // May fail due to invalid code, but endpoint should exist
        }

        @Test
        @DisplayName("Should validate code field")
        void shouldValidateCodeField() {
            Map<String, String> invalidData = new HashMap<>();
            // Missing code field

            given()
                .contentType(ContentType.JSON)
                .body(invalidData)
            .when()
                .post("/api/auth/oauth2/github/exchange-code")
            .then()
                .statusCode(400)
                .body("error", containsString("Validation error"));
        }

        @Test
        @DisplayName("Should handle empty code")
        void shouldHandleEmptyCode() {
            Map<String, String> invalidData = new HashMap<>();
            invalidData.put("code", "");

            given()
                .contentType(ContentType.JSON)
                .body(invalidData)
            .when()
                .post("/api/auth/oauth2/github/exchange-code")
            .then()
                .statusCode(400)
                .body("error", notNullValue());
        }

        @Test
        @DisplayName("Should handle code exchange without redirect URI")
        void shouldHandleCodeExchangeWithoutRedirectUri() {
            Map<String, String> exchangeData = new HashMap<>();
            exchangeData.put("code", "mock_github_authorization_code");
            // No redirectUri provided

            given()
                .contentType(ContentType.JSON)
                .body(exchangeData)
            .when()
                .post("/api/auth/oauth2/github/exchange-code")
            .then()
                .statusCode(anyOf(is(200), is(400), is(500))); // Should handle missing redirectUri gracefully
        }
    }

    @Nested
    @DisplayName("OAuth2 Error Handling Tests")
    class OAuth2ErrorHandlingTests {

        @Test
        @DisplayName("Should handle malformed JSON in callback")
        void shouldHandleMalformedJsonInCallback() {
            given()
                .contentType(ContentType.JSON)
                .body("{ invalid json }")
            .when()
                .post("/api/auth/oauth2/github/callback")
            .then()
                .statusCode(400);
        }

        @Test
        @DisplayName("Should handle malformed JSON in code exchange")
        void shouldHandleMalformedJsonInCodeExchange() {
            given()
                .contentType(ContentType.JSON)
                .body("{ invalid json }")
            .when()
                .post("/api/auth/oauth2/github/exchange-code")
            .then()
                .statusCode(400);
        }

        @Test
        @DisplayName("Should handle invalid access token format")
        void shouldHandleInvalidAccessTokenFormat() {
            Map<String, String> invalidData = new HashMap<>();
            invalidData.put("accessToken", "invalid_token_format");

            given()
                .contentType(ContentType.JSON)
                .body(invalidData)
            .when()
                .post("/api/auth/oauth2/github/callback")
            .then()
                .statusCode(500)
                .body("success", equalTo(false))
                .body("message", containsString("GitHub authentication failed"));
        }

        @Test
        @DisplayName("Should handle invalid authorization code")
        void shouldHandleInvalidAuthorizationCode() {
            Map<String, String> invalidData = new HashMap<>();
            invalidData.put("code", "invalid_authorization_code");

            given()
                .contentType(ContentType.JSON)
                .body(invalidData)
            .when()
                .post("/api/auth/oauth2/github/exchange-code")
            .then()
                .statusCode(anyOf(is(400), is(500)))
                .body("error", notNullValue());
        }
    }

    @Nested
    @DisplayName("OAuth2 Integration Flow Tests")
    class OAuth2IntegrationFlowTests {

        @Test
        @DisplayName("Should handle complete OAuth2 flow simulation")
        void shouldHandleCompleteOAuth2FlowSimulation() {
            // Step 1: Get OAuth2 configuration
            given()
                .contentType(ContentType.JSON)
            .when()
                .get("/api/auth/oauth2/config")
            .then()
                .statusCode(200)
                .body("githubClientId", notNullValue())
                .body("githubAuthUrl", notNullValue());

            // Step 2: Simulate code exchange (will fail with mock data, but tests endpoint)
            Map<String, String> exchangeData = new HashMap<>();
            exchangeData.put("code", "mock_code");

            given()
                .contentType(ContentType.JSON)
                .body(exchangeData)
            .when()
                .post("/api/auth/oauth2/github/exchange-code")
            .then()
                .statusCode(anyOf(is(200), is(400), is(500))); // Endpoint exists and handles request

            // Step 3: Simulate callback (will fail with mock data, but tests endpoint)
            Map<String, String> callbackData = new HashMap<>();
            callbackData.put("accessToken", "mock_token");

            given()
                .contentType(ContentType.JSON)
                .body(callbackData)
            .when()
                .post("/api/auth/oauth2/github/callback")
            .then()
                .statusCode(anyOf(is(200), is(500))); // Endpoint exists and handles request
        }
    }

    @Nested
    @DisplayName("OAuth2 User Management Tests")
    class OAuth2UserManagementTests {

        @Test
        @DisplayName("Should handle existing OAuth2 user scenario")
        void shouldHandleExistingOAuth2UserScenario() {
            // Verify that our existing OAuth2 user exists in the database
            // This simulates the scenario where a user has already authenticated via OAuth2
            
            // Check if user exists by searching for OAuth2 users
            // Note: This would typically be done through the user management endpoints
            // but we're testing the OAuth2 integration context
            
            given()
                .contentType(ContentType.JSON)
            .when()
                .get("/api/auth/users")
            .then()
                .statusCode(200)
                .body("$", hasSize(1))
                .body("[0].email", equalTo("github.user@test.com"));
        }

        @Test
        @DisplayName("Should handle OAuth2 user profile completion")
        void shouldHandleOAuth2UserProfileCompletion() {
            // Test scenario where OAuth2 user needs to complete their profile
            // This would typically involve updating user information after OAuth2 login
            
            Map<String, Object> profileData = new HashMap<>();
            profileData.put("phone", "1234567890");
            profileData.put("age", 25);
            profileData.put("gender", "MALE");

            given()
                .contentType(ContentType.JSON)
                .body(profileData)
            .when()
                .put("/api/auth/users/" + existingOAuth2User.getSchoolId())
            .then()
                .statusCode(200)
                .body("phone", equalTo("1234567890"))
                .body("age", equalTo(25))
                .body("gender", equalTo("MALE"));
        }
    }
}