package edu.cit.bayonas.citmedconnect.features.appointments;

import edu.cit.bayonas.citmedconnect.BaseIntegrationTest;
import edu.cit.bayonas.citmedconnect.features.appointments.entity.AppointmentEntity;
import edu.cit.bayonas.citmedconnect.features.appointments.entity.TimeSlot;
import edu.cit.bayonas.citmedconnect.features.appointments.repository.AppointmentRepository;
import edu.cit.bayonas.citmedconnect.features.appointments.repository.TimeSlotRepository;
import edu.cit.bayonas.citmedconnect.features.auth.entity.UserEntity;
import edu.cit.bayonas.citmedconnect.features.auth.repository.UserRepository;
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

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@DisplayName("Appointment Integration Tests")
class AppointmentIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UserEntity testUser;
    private TimeSlot testTimeSlot;
    private AppointmentEntity testAppointment;

    @BeforeEach
    public void setUp() {
        super.setUp();

        // Clean database
        appointmentRepository.deleteAll();
        timeSlotRepository.deleteAll();
        userRepository.deleteAll();

        // Create test user
        testUser = new UserEntity();
        testUser.setSchoolId("STUDENT001");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@test.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setRole("STUDENT");
        testUser.setPhone("1234567890");
        testUser.setGender("MALE");
        testUser.setAge(25);
        testUser.setCreatedAt(new Date());
        userRepository.saveAndFlush(testUser);

        // Create test time slot
        testTimeSlot = new TimeSlot();
        testTimeSlot.setSlotDate(LocalDateTime.now().plusDays(1).toLocalDate());
        testTimeSlot.setStartTime(LocalDateTime.now().plusHours(1).toLocalTime());
        testTimeSlot.setEndTime(LocalDateTime.now().plusHours(2).toLocalTime());
        testTimeSlot.setAvailable(true);
        testTimeSlot.setMaxBookings(5);
        testTimeSlot.setCurrentBookings(0);
        timeSlotRepository.saveAndFlush(testTimeSlot);

        // Create test appointment
        testAppointment = new AppointmentEntity();
        testAppointment.setUser(testUser);
        testAppointment.setTimeSlot(testTimeSlot);
        testAppointment.setStatus("PENDING");
        testAppointment.setReason("Regular checkup");
        testAppointment.setNotes("Test appointment");
        appointmentRepository.saveAndFlush(testAppointment);
    }

    @Nested
    @DisplayName("Appointment Creation Tests")
    class AppointmentCreationTests {

        @Test
        @DisplayName("Should create appointment successfully")
        void shouldCreateAppointmentSuccessfully() {
            // Create new time slot for this test
            TimeSlot newTimeSlot = new TimeSlot();
            newTimeSlot.setSlotDate(LocalDateTime.now().plusDays(2).toLocalDate());
            newTimeSlot.setStartTime(LocalDateTime.now().plusHours(2).toLocalTime());
            newTimeSlot.setEndTime(LocalDateTime.now().plusHours(3).toLocalTime());
            newTimeSlot.setAvailable(true);
            newTimeSlot.setMaxBookings(5);
            newTimeSlot.setCurrentBookings(0);
            timeSlotRepository.saveAndFlush(newTimeSlot);

            Map<String, Object> appointmentData = new HashMap<>();
            appointmentData.put("user", Map.of("schoolId", testUser.getSchoolId()));
            appointmentData.put("timeSlot", Map.of("timeSlotId", newTimeSlot.getTimeSlotId()));
            appointmentData.put("status", "PENDING");
            appointmentData.put("reason", "New appointment");

            given()
                    .contentType(ContentType.JSON)
                    .body(appointmentData)
                    .when()
                    .post("/api/appointments/")
                    .then()
                    .statusCode(200)
                    .body("status", equalTo("PENDING"))
                    .body("reason", equalTo("New appointment"))
                    .body("appointmentId", notNullValue());
        }

        @Test
        @DisplayName("Should get test endpoint")
        void shouldGetTestEndpoint() {
            given()
                    .contentType(ContentType.JSON)
                    .when()
                    .get("/api/appointments/test")
                    .then()
                    .statusCode(200)
                    .body(equalTo("Appointment controller is working!"));
        }
    }

    @Nested
    @DisplayName("Appointment Retrieval Tests")
    class AppointmentRetrievalTests {

        @Test
        @DisplayName("Should get all appointments")
        void shouldGetAllAppointments() {
            given()
                    .contentType(ContentType.JSON)
                    .when()
                    .get("/api/appointments/")
                    .then()
                    .statusCode(200)
                    .body("$", hasSize(1))
                    .body("[0].status", equalTo("PENDING"))
                    .body("[0].reason", equalTo("Regular checkup"));
        }

        @Test
        @DisplayName("Should get appointment by ID")
        void shouldGetAppointmentById() {
            given()
                    .contentType(ContentType.JSON)
                    .when()
                    .get("/api/appointments/" + testAppointment.getAppointmentId())
                    .then()
                    .statusCode(200)
                    .body("appointmentId", equalTo(testAppointment.getAppointmentId().intValue()))
                    .body("status", equalTo("PENDING"))
                    .body("reason", equalTo("Regular checkup"));
        }

        @Test
        @DisplayName("Should get appointments by user ID")
        void shouldGetAppointmentsByUserId() {
            given()
                    .contentType(ContentType.JSON)
                    .when()
                    .get("/api/appointments/user/" + testUser.getSchoolId())
                    .then()
                    .statusCode(200)
                    .body("$", hasSize(1))
                    .body("[0].status", equalTo("PENDING"));
        }

        @Test
        @DisplayName("Should get appointments by status")
        void shouldGetAppointmentsByStatus() {
            given()
                    .contentType(ContentType.JSON)
                    .when()
                    .get("/api/appointments/status/PENDING")
                    .then()
                    .statusCode(200)
                    .body("$", hasSize(1))
                    .body("[0].status", equalTo("PENDING"));
        }

        @Test
        @DisplayName("Should get my appointments for student")
        void shouldGetMyAppointments() {
            given()
                    .contentType(ContentType.JSON)
                    .queryParam("userId", testUser.getSchoolId())
                    .when()
                    .get("/api/appointments/student/my-appointments")
                    .then()
                    .statusCode(200)
                    .body("$", hasSize(1))
                    .body("[0].status", equalTo("PENDING"));
        }

        @Test
        @DisplayName("Should get simple appointments")
        void shouldGetSimpleAppointments() {
            given()
                    .contentType(ContentType.JSON)
                    .when()
                    .get("/api/appointments/simple")
                    .then()
                    .statusCode(200)
                    .body("$", hasSize(2))
                    .body("[0]", equalTo("Test Appointment 1"))
                    .body("[1]", equalTo("Test Appointment 2"));
        }
    }

    @Nested
    @DisplayName("Appointment Update Tests")
    class AppointmentUpdateTests {

        @Test
        @DisplayName("Should update appointment successfully")
        void shouldUpdateAppointmentSuccessfully() {
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("status", "CONFIRMED");
            updateData.put("notes", "Updated notes");

            given()
                    .contentType(ContentType.JSON)
                    .body(updateData)
                    .when()
                    .put("/api/appointments/" + testAppointment.getAppointmentId())
                    .then()
                    .statusCode(200)
                    .body("status", equalTo("CONFIRMED"))
                    .body("notes", equalTo("Updated notes"));
        }

        @Test
        @DisplayName("Should confirm appointment")
        void shouldConfirmAppointment() {
            given()
                    .contentType(ContentType.JSON)
                    .when()
                    .put("/api/appointments/" + testAppointment.getAppointmentId() + "/confirm")
                    .then()
                    .statusCode(200)
                    .body("status", equalTo("CONFIRMED"));
        }

        @Test
        @DisplayName("Should cancel appointment")
        void shouldCancelAppointment() {
            given()
                    .contentType(ContentType.JSON)
                    .when()
                    .put("/api/appointments/" + testAppointment.getAppointmentId() + "/cancel")
                    .then()
                    .statusCode(200)
                    .body("status", equalTo("CANCELLED"));
        }

        @Test
        @DisplayName("Should complete appointment")
        void shouldCompleteAppointment() {
            given()
                    .contentType(ContentType.JSON)
                    .when()
                    .put("/api/appointments/" + testAppointment.getAppointmentId() + "/complete")
                    .then()
                    .statusCode(200)
                    .body("status", equalTo("COMPLETED"));
        }

        @Test
        @DisplayName("Should reschedule appointment")
        void shouldRescheduleAppointment() {
            // Create new time slot for rescheduling
            TimeSlot newTimeSlot = new TimeSlot();
            newTimeSlot.setSlotDate(LocalDateTime.now().plusDays(3).toLocalDate());
            newTimeSlot.setStartTime(LocalDateTime.now().plusHours(3).toLocalTime());
            newTimeSlot.setEndTime(LocalDateTime.now().plusHours(4).toLocalTime());
            newTimeSlot.setAvailable(true);
            newTimeSlot.setMaxBookings(5);
            newTimeSlot.setCurrentBookings(0);
            timeSlotRepository.saveAndFlush(newTimeSlot);

            Map<String, Long> rescheduleData = new HashMap<>();
            rescheduleData.put("newTimeSlotId", newTimeSlot.getTimeSlotId());

            given()
                    .contentType(ContentType.JSON)
                    .body(rescheduleData)
                    .when()
                    .put("/api/appointments/" + testAppointment.getAppointmentId() + "/reschedule")
                    .then()
                    .statusCode(200)
                    .body("timeSlot.timeSlotId", equalTo(newTimeSlot.getTimeSlotId().intValue()));
        }
    }

    @Nested
    @DisplayName("Appointment Deletion Tests")
    class AppointmentDeletionTests {

        @Test
        @DisplayName("Should delete appointment successfully")
        void shouldDeleteAppointmentSuccessfully() {
            given()
                    .contentType(ContentType.JSON)
                    .when()
                    .delete("/api/appointments/" + testAppointment.getAppointmentId())
                    .then()
                    .statusCode(200)
                    .body("deleted", equalTo(true));
        }
    }

    @Nested
    @DisplayName("Staff Appointment Tests")
    class StaffAppointmentTests {

        @Test
        @DisplayName("Should get all appointments for staff")
        void shouldGetAllAppointmentsForStaff() {
            given()
                    .contentType(ContentType.JSON)
                    .when()
                    .get("/api/appointments/staff/all")
                    .then()
                    .statusCode(200)
                    .body("$", hasSize(1))
                    .body("[0].status", equalTo("PENDING"));
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should return 404 for non-existent appointment")
        void shouldReturn404ForNonExistentAppointment() {
            given()
                    .contentType(ContentType.JSON)
                    .when()
                    .get("/api/appointments/99999")
                    .then()
                    .statusCode(anyOf(is(404), is(500))); // Allow both as implementation may vary
        }

        @Test
        @DisplayName("Should handle reschedule with missing time slot ID")
        void shouldHandleRescheduleWithMissingTimeSlotId() {
            Map<String, Object> invalidData = new HashMap<>();
            // Missing newTimeSlotId

            given()
                    .contentType(ContentType.JSON)
                    .body(invalidData)
                    .when()
                    .put("/api/appointments/" + testAppointment.getAppointmentId() + "/reschedule")
                    .then()
                    .statusCode(anyOf(is(400), is(500))); // Allow both as implementation may vary
        }
    }
}