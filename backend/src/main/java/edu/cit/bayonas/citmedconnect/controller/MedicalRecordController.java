package edu.cit.bayonas.citmedconnect.controller;

import edu.cit.bayonas.citmedconnect.dto.MedicalRecordDTO;
import edu.cit.bayonas.citmedconnect.service.MedicalRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/medical-records")
@CrossOrigin(origins = "*")
public class MedicalRecordController {
    
    @Autowired
    private MedicalRecordService medicalRecordService;
    
    // DIAGNOSTIC ENDPOINT - Test if controller is working
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> test() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "Controller is working!");
        response.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/")
    public ResponseEntity<?> createMedicalRecord(@RequestBody MedicalRecordDTO medicalRecordDTO) {
        try {
            System.out.println("=== CONTROLLER RECEIVED CREATE REQUEST ===");
            System.out.println("Request body: " + medicalRecordDTO.toString());
            System.out.println("UserId from DTO: " + medicalRecordDTO.getUserId());
            System.out.println("Diagnosis: " + medicalRecordDTO.getDiagnosis());
            
            // Validate required fields
            if (medicalRecordDTO.getUserId() == null || medicalRecordDTO.getUserId().trim().isEmpty()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "User ID is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            
            if (medicalRecordDTO.getDiagnosis() == null || medicalRecordDTO.getDiagnosis().trim().isEmpty()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Diagnosis is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            
            MedicalRecordDTO createdRecord = medicalRecordService.createMedicalRecord(medicalRecordDTO);
            System.out.println("Record created successfully: " + createdRecord.getRecordId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(createdRecord);
        } catch (RuntimeException e) {
            System.err.println("Error creating medical record: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @GetMapping("/")
    public ResponseEntity<?> getAllMedicalRecords() {
        try {
            System.out.println("=== CONTROLLER RECEIVED GET ALL REQUEST ===");
            List<MedicalRecordDTO> records = medicalRecordService.getAllMedicalRecords();
            System.out.println("Found " + records.size() + " records");
            
            if (records.isEmpty()) {
                System.out.println("WARNING: No records found in database!");
            } else {
                System.out.println("First record: " + records.get(0).toString());
            }
            
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            System.err.println("Error fetching all records: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch records: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getMedicalRecordById(@PathVariable Long id) {
        try {
            System.out.println("=== FETCHING RECORD BY ID: " + id + " ===");
            MedicalRecordDTO record = medicalRecordService.getMedicalRecordById(id);
            if (record != null) {
                System.out.println("Found record: " + record.toString());
                return ResponseEntity.ok(record);
            }
            System.out.println("Record not found with ID: " + id);
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Record not found with ID: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            System.err.println("Error fetching record by ID: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getMedicalRecordsByUserId(@PathVariable String userId) {
        try {
            System.out.println("=== FETCHING RECORDS FOR USER: " + userId + " ===");
            List<MedicalRecordDTO> records = medicalRecordService.getMedicalRecordsByUserId(userId);
            System.out.println("Found " + records.size() + " records for user " + userId);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            System.err.println("Error fetching records for user: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @GetMapping("/user/{userId}/sorted")
    public ResponseEntity<?> getMedicalRecordsByUserIdSorted(@PathVariable String userId) {
        try {
            System.out.println("=== FETCHING SORTED RECORDS FOR USER: " + userId + " ===");
            List<MedicalRecordDTO> records = medicalRecordService.getMedicalRecordsByUserIdSorted(userId);
            System.out.println("Found " + records.size() + " sorted records for user " + userId);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            System.err.println("Error fetching sorted records: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<?> getMedicalRecordByAppointmentId(@PathVariable Long appointmentId) {
        try {
            System.out.println("=== FETCHING RECORD FOR APPOINTMENT: " + appointmentId + " ===");
            MedicalRecordDTO record = medicalRecordService.getMedicalRecordByAppointmentId(appointmentId);
            if (record != null) {
                return ResponseEntity.ok(record);
            }
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "No record found for appointment: " + appointmentId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            System.err.println("Error fetching record by appointment: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @GetMapping("/date-range")
    public ResponseEntity<?> getMedicalRecordsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            List<MedicalRecordDTO> records = medicalRecordService.getMedicalRecordsByDateRange(startDate, endDate);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @GetMapping("/user/{userId}/date-range")
    public ResponseEntity<?> getMedicalRecordsByUserAndDateRange(
            @PathVariable String userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            List<MedicalRecordDTO> records = medicalRecordService.getMedicalRecordsByUserAndDateRange(userId, startDate, endDate);
            return ResponseEntity.ok(records);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }
    
    @GetMapping("/created-by/{createdBy}")
    public ResponseEntity<?> getMedicalRecordsByCreatedBy(@PathVariable String createdBy) {
        try {
            List<MedicalRecordDTO> records = medicalRecordService.getMedicalRecordsByCreatedBy(createdBy);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateMedicalRecord(
            @PathVariable Long id, 
            @RequestBody MedicalRecordDTO recordDetails) {
        try {
            System.out.println("=== UPDATING MEDICAL RECORD ===");
            System.out.println("Record ID: " + id);
            System.out.println("Update data: " + recordDetails.toString());
            
            MedicalRecordDTO updatedRecord = medicalRecordService.updateMedicalRecord(id, recordDetails);
            return ResponseEntity.ok(updatedRecord);
        } catch (RuntimeException e) {
            System.err.println("Error updating medical record: " + e.getMessage());
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Boolean>> deleteMedicalRecord(@PathVariable Long id) {
        return medicalRecordService.deleteMedicalRecord(id);
    }
    
    @GetMapping("/user/{userId}/count")
    public ResponseEntity<?> getRecordCountByUserId(@PathVariable String userId) {
        try {
            long count = medicalRecordService.getRecordCountByUserId(userId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}