package edu.cit.bayonas.citmedconnect.features.notifications;

import edu.cit.bayonas.citmedconnect.BaseIntegrationTest;
import edu.cit.bayonas.citmedconnect.features.auth.entity.UserEntity;
import edu.cit.bayonas.citmedconnect.features.auth.repository.UserRepository;
import edu.cit.bayonas.citmedconnect.features.appointments.repository.AppointmentRepository;
import edu.cit.bayonas.citmedconnect.features.appointments.repository.TimeSlotRepository;
import edu.cit.bayonas.citmedconnect.features.medicalrecords.repository.MedicalRecordRepository;
import edu.cit.bayonas.citmedconnect.features.notifications.entity.NotificationEntity;
import edu.cit.bayonas.citmedconnect.features.notifications.repository.NotificationRepository;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@DisplayName("Notification Integration Tests")
class NotificationIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UserEntity testStudent;
    private UserEntity testStaff;
    private NotificationEntity testNotification;

    @BeforeEach
    public void setUp() {
        super.setUp();

        // Clean database in correct order (child tables first)
        medicalRecordRepository.deleteAll();
        notificationRepository.deleteAll();
        appointmentRepository.deleteAll();
        timeSlotRepository.deleteAll();
        userRepository.deleteAll();

        // Create test student
        testStudent = new UserEntity();
        testStudent.setSchoolId("STUDENT001");
        testStudent.setFirstName("John");
        testStudent.setLastName("Doe");
        testStudent.setEmail("john.doe@test.com");
        testStudent.setPassword(passwordEncoder.encode("password123"));
        testStudent.setRole("STUDENT");
        testStudent.setPhone("1234567890");
        testStudent.setGender("MALE");
        testStudent.setAge(25);
        testStudent.setCreatedAt(new Date());
        userRepository.saveAndFlush(testStudent);

        // Create test staff
        testStaff = new UserEntity();
        testStaff.setSchoolId("STAFF001");
        testStaff.setFirstName("Dr. Jane");
        testStaff.setLastName("Smith");
        testStaff.setEmail("jane.smith@test.com");
        testStaff.setPassword(passwordEncoder.encode("password123"));
        testStaff.setRole("STAFF");
        testStaff.setPhone("0987654321");
        testStaff.setGender("FEMALE");
        testStaff.setAge(35);
        testStaff.setCreatedAt(new Date());
        userRepository.saveAndFlush(testStaff);

        // Create test notification
        testNotification = new NotificationEntity();
        testNotification.setNotificationId(UUID.randomUUID().toString());
        testNotification.setSchoolId(testStudent.getSchoolId());
        testNotification.setTitle("Test Notification");
        testNotification.setMessage("This is a test notification");
        testNotification.setNotificationType("INFO");
        testNotification.setIsGlobal(false);
        testNotification.setIsRead(false);
        testNotification.setCreatedAt(LocalDateTime.now());
        notificationRepository.saveAndFlush(testNotification);
    }

    @Nested
    @DisplayName("Notification Creation Tests")
    class NotificationCreationTests {

        @Test
        @DisplayName("Should create notification successfully")
        void shouldCreateNotificationSuccessfully() {
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("schoolId", testStudent.getSchoolId());
            notificationData.put("title", "New Notification");
            notificationData.put("message", "This is a new notification");
            notificationData.put("notificationType", "REMINDER");

            given()
                    .contentType(ContentType.JSON)
                    .body(notificationData)
                    .when()
                    .post("/api/notifications/")
                    .then()
                    .statusCode(201)
                    .body("title", equalTo("New Notification"))
                    .body("message", equalTo("This is a new notification"))
                    .body("notificationType", equalTo("REMINDER"))
                    .body("notificationId", notNullValue());
        }

        @Test
        @DisplayName("Should send notification to user")
        void shouldSendNotificationToUser() {
            Map<String, Object> sendData = new HashMap<>();
            sendData.put("recipientId", testStudent.getSchoolId());
            sendData.put("title", "Sent Notification");
            sendData.put("message", "This notification was sent");
            sendData.put("type", "ALERT");

            given()
                    .contentType(ContentType.JSON)
                    .body(sendData)
                    .when()
                    .post("/api/notifications/send")
                    .then()
                    .statusCode(201)
                    .body("title", equalTo("Sent Notification"))
                    .body("message", equalTo("This notification was sent"))
                    .body("notificationType", equalTo("ALERT"));
        }

        @Test
        @DisplayName("Should get test endpoint")
        void shouldGetTestEndpoint() {
            given()
                    .contentType(ContentType.JSON)
                    .when()
                    .get("/api/notifications/test")
                    .then()
                    .statusCode(200)
                    .body("status", equalTo("Notification Controller is working!"))
                    .body("feature", equalTo("notifications"));
        }

        @Test
        @DisplayName("Should validate required fields")
        void shouldValidateRequiredFields() {
            Map<String, Object> invalidData = new HashMap<>();
            // Missing required fields

            given()
                    .contentType(ContentType.JSON)
                    .body(invalidData)
                    .when()
                    .post("/api/notifications/")
                    .then()
                    .statusCode(400);
        }
    }

    @Nested
    @DisplayName("Notification Retrieval Tests")
    class NotificationRetrievalTests {

        @Test
        @DisplayName("Should get all notifications")
        void shouldGetAllNotifications() {
            given()
                    .contentType(ContentType.JSON)
                    .when()
                    .get("/api/notifications/")
                    .then()
                    .statusCode(200)
                    .body("$", hasSize(1))
                    .body("[0].title", equalTo("Test Notification"))
                    .body("[0].message", equalTo("This is a test notification"));
        }

        @Test
        @DisplayName("Should get notification by ID")
        void shouldGetNotificationById() {
            given()
                    .contentType(ContentType.JSON)
                    .when()
                    .get("/api/notifications/" + testNotification.getNotificationId())
                    .then()
                    .statusCode(200)
                    .body("notificationId", equalTo(testNotification.getNotificationId()))
                    .body("title", equalTo("Test Notification"))
                    .body("message", equalTo("This is a test notification"));
        }

        @Test
        @DisplayName("Should get notifications by school ID")
        void shouldGetNotificationsBySchoolId() {
            given()
                    .contentType(ContentType.JSON)
                    .when()
                    .get("/api/notifications/user/" + testStudent.getSchoolId())
                    .then()
                    .statusCode(200)
                    .body("$", hasSize(1))
                    .body("[0].schoolId", equalTo(testStudent.getSchoolId()));
        }

        @Test
        @DisplayName("Should get notifications for user with role")
        void shouldGetNotificationsForUserWithRole() {
            given()
                    .contentType(ContentType.JSON)
                    .when()
                    .get("/api/notifications/user/" + testStudent.getSchoolId() + "/role/" + testStudent.getRole())
                    .then()
                    .statusCode(200)
                    .body("$", hasSize(greaterThanOrEqualTo(0))); // May include global notifications
        }

        @Test
        @DisplayName("Should get all notifications for user")
        void shouldGetAllNotificationsForUser() {
            given()
                    .contentType(ContentType.JSON)
                    .when()
                    .get("/api/notifications/user/" + testStudent.getSchoolId() + "/all")
                    .then()
                    .statusCode(200)
                    .body("$", hasSize(greaterThanOrEqualTo(1)));
        }

        @Test
        @DisplayName("Should get unread notification count")
        void shouldGetUnreadNotificationCount() {
            given()
                    .contentType(ContentType.JSON)
                    .when()
                    .get("/api/notifications/user/" + testStudent.getSchoolId() + "/unread-count")
                    .then()
                    .statusCode(200)
                    .body("unreadCount", equalTo(1));
        }
    }

    @Nested
    @DisplayName("Notification Update Tests")
    class NotificationUpdateTests {

        @Test
        @DisplayName("Should update notification successfully")
        void shouldUpdateNotificationSuccessfully() {
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("title", "Updated Title");
            updateData.put("message", "Updated message");
            updateData.put("notificationType", "UPDATED");

            given()
                    .contentType(ContentType.JSON)
                    .body(updateData)
                    .when()
                    .put("/api/notifications/" + testNotification.getNotificationId())
                    .then()
                    .statusCode(200)
                    .body("title", equalTo("Updated Title"))
                    .body("message", equalTo("Updated message"))
                    .body("notificationType", equalTo("UPDATED"));
        }

        @Test
        @DisplayName("Should mark notification as read")
        void shouldMarkNotificationAsRead() {
            given()
                    .contentType(ContentType.JSON)
                    .when()
                    .put("/api/notifications/" + testNotification.getNotificationId() + "/read")
                    .then()
                    .statusCode(200)
                    .body("isRead", equalTo(true));
        }
    }

    @Nested
    @DisplayName("Notification Deletion Tests")
    class NotificationDeletionTests {

        @Test
        @DisplayName("Should delete notification successfully")
        void shouldDeleteNotificationSuccessfully() {
            given()
                    .contentType(ContentType.JSON)
                    .when()
                    .delete("/api/notifications/" + testNotification.getNotificationId())
                    .then()
                    .statusCode(200)
                    .body("message", equalTo("Notification deleted successfully"));
        }
    }

    @Nested
    @DisplayName("Broadcast Notification Tests")
    class BroadcastNotificationTests {

        @Test
        @DisplayName("Should send notification to all students")
        void shouldSendNotificationToAllStudents() {
            Map<String, Object> broadcastData = new HashMap<>();
            broadcastData.put("title", "Student Broadcast");
            broadcastData.put("message", "This is a broadcast to all students");

            given()
                    .contentType(ContentType.JSON)
                    .body(broadcastData)
                    .when()
                    .post("/api/notifications/broadcast/students")
                    .then()
                    .statusCode(201)
                    .body("$", hasSize(1)) // Should create notification for our test student
                    .body("[0].title", equalTo("Student Broadcast"))
                    .body("[0].message", equalTo("This is a broadcast to all students"));
        }

        @Test
        @DisplayName("Should send notification to all staff")
        void shouldSendNotificationToAllStaff() {
            Map<String, Object> broadcastData = new HashMap<>();
            broadcastData.put("title", "Staff Broadcast");
            broadcastData.put("message", "This is a broadcast to all staff");

            given()
                    .contentType(ContentType.JSON)
                    .body(broadcastData)
                    .when()
                    .post("/api/notifications/broadcast/staff")
                    .then()
                    .statusCode(201)
                    .body("$", hasSize(1)) // Should create notification for our test staff
                    .body("[0].title", equalTo("Staff Broadcast"))
                    .body("[0].message", equalTo("This is a broadcast to all staff"));
        }

        @Test
        @DisplayName("Should send notification to everyone")
        void shouldSendNotificationToEveryone() {
            Map<String, Object> broadcastData = new HashMap<>();
            broadcastData.put("title", "Global Broadcast");
            broadcastData.put("message", "This is a broadcast to everyone");

            given()
                    .contentType(ContentType.JSON)
                    .body(broadcastData)
                    .when()
                    .post("/api/notifications/broadcast/all")
                    .then()
                    .statusCode(201)
                    .body("$", hasSize(2)) // Should create notifications for both test users
                    .body("[0].title", equalTo("Global Broadcast"))
                    .body("[1].title", equalTo("Global Broadcast"));
        }

        @Test
        @DisplayName("Should validate broadcast notification fields")
        void shouldValidateBroadcastNotificationFields() {
            Map<String, Object> invalidData = new HashMap<>();
            // Missing required fields

            given()
                    .contentType(ContentType.JSON)
                    .body(invalidData)
                    .when()
                    .post("/api/notifications/broadcast/students")
                    .then()
                    .statusCode(400);
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should return 404 for non-existent notification")
        void shouldReturn404ForNonExistentNotification() {
            given()
                    .contentType(ContentType.JSON)
                    .when()
                    .get("/api/notifications/non-existent-id")
                    .then()
                    .statusCode(404)
                    .body("error", notNullValue());
        }

        @Test
        @DisplayName("Should return 404 when deleting non-existent notification")
        void shouldReturn404WhenDeletingNonExistentNotification() {
            given()
                    .contentType(ContentType.JSON)
                    .when()
                    .delete("/api/notifications/non-existent-id")
                    .then()
                    .statusCode(404)
                    .body("error", equalTo("Notification not found"));
        }

        @Test
        @DisplayName("Should handle update of non-existent notification")
        void shouldHandleUpdateOfNonExistentNotification() {
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("title", "Updated Title");

            given()
                    .contentType(ContentType.JSON)
                    .body(updateData)
                    .when()
                    .put("/api/notifications/non-existent-id")
                    .then()
                    .statusCode(404)
                    .body("error", notNullValue());
        }

        @Test
        @DisplayName("Should handle mark as read for non-existent notification")
        void shouldHandleMarkAsReadForNonExistentNotification() {
            given()
                    .contentType(ContentType.JSON)
                    .when()
                    .put("/api/notifications/non-existent-id/read")
                    .then()
                    .statusCode(404)
                    .body("error", notNullValue());
        }
    }
}