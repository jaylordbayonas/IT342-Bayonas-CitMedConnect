package edu.cit.bayonas.citmedconnect.features.medicalrecords;

import edu.cit.bayonas.citmedconnect.BaseIntegrationTest;
import edu.cit.bayonas.citmedconnect.features.appointments.entity.AppointmentEntity;
import edu.cit.bayonas.citmedconnect.features.appointments.entity.TimeSlot;
import edu.cit.bayonas.citmedconnect.features.appointments.repository.AppointmentRepository;
import edu.cit.bayonas.citmedconnect.features.appointments.repository.TimeSlotRepository;
import edu.cit.bayonas.citmedconnect.features.auth.entity.UserEntity;
import edu.cit.bayonas.citmedconnect.features.auth.repository.UserRepository;
import edu.cit.bayonas.citmedconnect.features.medicalrecords.entity.MedicalRecordEntity;
import edu.cit.bayonas.citmedconnect.features.medicalrecords.repository.MedicalRecordRepository;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@DisplayName("Medical Record Integration Tests")
class MedicalRecordIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UserEntity testUser;
    private UserEntity staffUser;
    private AppointmentEntity testAppointment;
    private MedicalRecordEntity testMedicalRecord;

    @BeforeEach
    public void setUp() {
        super.setUp();
        
        // Clean database
        medicalRecordRepository.deleteAll();
        appointmentRepository.deleteAll();
        timeSlotRepository.deleteAll();
        userRepository.deleteAll();

        // Create test patient
        testUser = new UserEntity();
        testUser.setSchoolId("PATIENT001");
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

        // Create test staff
        staffUser = new UserEntity();
        staffUser.setSchoolId("STAFF001");
        staffUser.setFirstName("Dr. Jane");
        staffUser.setLastName("Smith");
        staffUser.setEmail("jane.smith@test.com");
        staffUser.setPassword(passwordEncoder.encode("password123"));
        staffUser.setRole("STAFF");
        staffUser.setPhone("0987654321");
        staffUser.setGender("FEMALE");
        staffUser.setAge(35);
        staffUser.setCreatedAt(new Date());
        userRepository.saveAndFlush(staffUser);

        // Create test time slot and appointment
        TimeSlot testTimeSlot = new TimeSlot();
        testTimeSlot.setSlotDate(LocalDateTime.now().toLocalDate());
        testTimeSlot.setStartTime(LocalDateTime.now().toLocalTime());
        testTimeSlot.setEndTime(LocalDateTime.now().plusHours(1).toLocalTime());
        testTimeSlot.setAvailable(false);
        testTimeSlot.setMaxBookings(1);
        testTimeSlot.setCurrentBookings(1);
        timeSlotRepository.saveAndFlush(testTimeSlot);

        testAppointment = new AppointmentEntity();
        testAppointment.setUser(testUser);
        testAppointment.setTimeSlot(testTimeSlot);
        testAppointment.setStatus("COMPLETED");
        testAppointment.setReason("Regular checkup");
        appointmentRepository.saveAndFlush(testAppointment);

        // Create test medical record
        testMedicalRecord = new MedicalRecordEntity();
        testMedicalRecord.setUser(testUser);
        testMedicalRecord.setAppointment(testAppointment);
        testMedicalRecord.setDiagnosis("Common cold");
        testMedicalRecord.setSymptoms("Cough, runny nose");
        testMedicalRecord.setTreatment("Rest and fluids");
        testMedicalRecord.setPrescription("Paracetamol 500mg");
        testMedicalRecord.setVitalSigns("BP: 120/80, Temp: 37.2°C");
        testMedicalRecord.setCreatedBy(staffUser.getSchoolId());
        testMedicalRecord.setNotes("Patient advised to rest");
        medicalRecordRepository.saveAndFlush(testMedicalRecord);
    }

    @Nested
    @DisplayName("Medical Record Creation Tests")
    class MedicalRecordCreationTests {

        @Test
        @DisplayName("Should create medical record successfully")
        void shouldCreateMedicalRecordSuccessfully() {
            Map<String, Object> recordData = new HashMap<>();
            recordData.put("userId", testUser.getSchoolId());
            recordData.put("appointmentId", testAppointment.getAppointmentId());
            recordData.put("diagnosis", "Hypertension");
            recordData.put("symptoms", "High blood pressure, headache");
            recordData.put("treatment", "Lifestyle changes, medication");
            recordData.put("prescription", "Amlodipine 5mg daily");
            recordData.put("vitalSigns", "BP: 150/90, Pulse: 80");
            recordData.put("createdBy", staffUser.getSchoolId());
            recordData.put("notes", "Follow up in 2 weeks");

            given()
                .contentType(ContentType.JSON)
                .body(recordData)
            .when()
                .post("/api/medical-records/")
            .then()
                .statusCode(201)
                .body("diagnosis", equalTo("Hypertension"))
                .body("symptoms", equalTo("High blood pressure, headache"))
                .body("treatment", equalTo("Lifestyle changes, medication"))
                .body("recordId", notNullValue());
        }

        @Test
        @DisplayName("Should get test endpoint")
        void shouldGetTestEndpoint() {
            given()
                .contentType(ContentType.JSON)
            .when()
                .get("/api/medical-records/test")
            .then()
                .statusCode(200)
                .body("status", equalTo("Controller is working!"));
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
                .post("/api/medical-records/")
            .then()
                .statusCode(400);
        }
    }

    @Nested
    @DisplayName("Medical Record Retrieval Tests")
    class MedicalRecordRetrievalTests {

        @Test
        @DisplayName("Should get all medical records")
        void shouldGetAllMedicalRecords() {
            given()
                .contentType(ContentType.JSON)
            .when()
                .get("/api/medical-records/")
            .then()
                .statusCode(200)
                .body("$", hasSize(1))
                .body("[0].diagnosis", equalTo("Common cold"))
                .body("[0].symptoms", equalTo("Cough, runny nose"));
        }

        @Test
        @DisplayName("Should get medical record by ID")
        void shouldGetMedicalRecordById() {
            given()
                .contentType(ContentType.JSON)
            .when()
                .get("/api/medical-records/" + testMedicalRecord.getRecordId())
            .then()
                .statusCode(200)
                .body("recordId", equalTo(testMedicalRecord.getRecordId().intValue()))
                .body("diagnosis", equalTo("Common cold"))
                .body("treatment", equalTo("Rest and fluids"));
        }

        @Test
        @DisplayName("Should get medical records by user ID")
        void shouldGetMedicalRecordsByUserId() {
            given()
                .contentType(ContentType.JSON)
            .when()
                .get("/api/medical-records/user/" + testUser.getSchoolId())
            .then()
                .statusCode(200)
                .body("$", hasSize(1))
                .body("[0].diagnosis", equalTo("Common cold"));
        }

        @Test
        @DisplayName("Should get medical records by user ID sorted")
        void shouldGetMedicalRecordsByUserIdSorted() {
            given()
                .contentType(ContentType.JSON)
            .when()
                .get("/api/medical-records/user/" + testUser.getSchoolId() + "/sorted")
            .then()
                .statusCode(200)
                .body("$", hasSize(1))
                .body("[0].diagnosis", equalTo("Common cold"));
        }

        @Test
        @DisplayName("Should get medical record by appointment ID")
        void shouldGetMedicalRecordByAppointmentId() {
            given()
                .contentType(ContentType.JSON)
            .when()
                .get("/api/medical-records/appointment/" + testAppointment.getAppointmentId())
            .then()
                .statusCode(200)
                .body("diagnosis", equalTo("Common cold"))
                .body("appointmentId", equalTo(testAppointment.getAppointmentId().intValue()));
        }

        @Test
        @DisplayName("Should get medical records by created by")
        void shouldGetMedicalRecordsByCreatedBy() {
            given()
                .contentType(ContentType.JSON)
            .when()
                .get("/api/medical-records/created-by/" + staffUser.getSchoolId())
            .then()
                .statusCode(200)
                .body("$", hasSize(1))
                .body("[0].createdBy", equalTo(staffUser.getSchoolId()));
        }

        @Test
        @DisplayName("Should get record count by user ID")
        void shouldGetRecordCountByUserId() {
            given()
                .contentType(ContentType.JSON)
            .when()
                .get("/api/medical-records/user/" + testUser.getSchoolId() + "/count")
            .then()
                .statusCode(200)
                .body(equalTo("1"));
        }
    }

    @Nested
    @DisplayName("Medical Record Update Tests")
    class MedicalRecordUpdateTests {

        @Test
        @DisplayName("Should update medical record successfully")
        void shouldUpdateMedicalRecordSuccessfully() {
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("diagnosis", "Updated diagnosis");
            updateData.put("treatment", "Updated treatment");
            updateData.put("notes", "Updated notes");

            given()
                .contentType(ContentType.JSON)
                .body(updateData)
            .when()
                .put("/api/medical-records/" + testMedicalRecord.getRecordId())
            .then()
                .statusCode(200)
                .body("diagnosis", equalTo("Updated diagnosis"))
                .body("treatment", equalTo("Updated treatment"))
                .body("notes", equalTo("Updated notes"));
        }
    }

    @Nested
    @DisplayName("Medical Record Deletion Tests")
    class MedicalRecordDeletionTests {

        @Test
        @DisplayName("Should delete medical record successfully")
        void shouldDeleteMedicalRecordSuccessfully() {
            given()
                .contentType(ContentType.JSON)
            .when()
                .delete("/api/medical-records/" + testMedicalRecord.getRecordId())
            .then()
                .statusCode(200)
                .body("deleted", equalTo(true));
        }
    }

    @Nested
    @DisplayName("Date Range Query Tests")
    class DateRangeQueryTests {

        @Test
        @DisplayName("Should get medical records by date range")
        void shouldGetMedicalRecordsByDateRange() {
            LocalDateTime startDate = LocalDateTime.now().minusDays(1);
            LocalDateTime endDate = LocalDateTime.now().plusDays(1);
            
            given()
                .contentType(ContentType.JSON)
                .queryParam("startDate", startDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .queryParam("endDate", endDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .when()
                .get("/api/medical-records/date-range")
            .then()
                .statusCode(200)
                .body("$", hasSize(1))
                .body("[0].diagnosis", equalTo("Common cold"));
        }

        @Test
        @DisplayName("Should get medical records by user and date range")
        void shouldGetMedicalRecordsByUserAndDateRange() {
            LocalDateTime startDate = LocalDateTime.now().minusDays(1);
            LocalDateTime endDate = LocalDateTime.now().plusDays(1);
            
            given()
                .contentType(ContentType.JSON)
                .queryParam("startDate", startDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .queryParam("endDate", endDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .when()
                .get("/api/medical-records/user/" + testUser.getSchoolId() + "/date-range")
            .then()
                .statusCode(200)
                .body("$", hasSize(1))
                .body("[0].diagnosis", equalTo("Common cold"));
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should return 404 for non-existent record")
        void shouldReturn404ForNonExistentRecord() {
            given()
                .contentType(ContentType.JSON)
            .when()
                .get("/api/medical-records/99999")
            .then()
                .statusCode(404)
                .body("error", containsString("Record not found"));
        }

        @Test
        @DisplayName("Should return 404 for non-existent appointment record")
        void shouldReturn404ForNonExistentAppointmentRecord() {
            given()
                .contentType(ContentType.JSON)
            .when()
                .get("/api/medical-records/appointment/99999")
            .then()
                .statusCode(404)
                .body("error", containsString("No record found"));
        }

        @Test
        @DisplayName("Should handle update of non-existent record")
        void shouldHandleUpdateOfNonExistentRecord() {
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("diagnosis", "Updated diagnosis");

            given()
                .contentType(ContentType.JSON)
                .body(updateData)
            .when()
                .put("/api/medical-records/99999")
            .then()
                .statusCode(404)
                .body("error", notNullValue());
        }
    }
}