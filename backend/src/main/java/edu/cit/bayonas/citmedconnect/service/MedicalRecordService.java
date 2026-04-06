package edu.cit.bayonas.citmedconnect.service;

import edu.cit.bayonas.citmedconnect.dto.MedicalRecordDTO;
import edu.cit.bayonas.citmedconnect.entity.MedicalRecordEntity;
import edu.cit.bayonas.citmedconnect.entity.UserEntity;
import edu.cit.bayonas.citmedconnect.entity.AppointmentEntity;
import edu.cit.bayonas.citmedconnect.mapper.MedicalRecordMapper;
import edu.cit.bayonas.citmedconnect.repository.MedicalRecordRepository;
import edu.cit.bayonas.citmedconnect.repository.UserRepository;
import edu.cit.bayonas.citmedconnect.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MedicalRecordService {
    
    @Autowired
    private MedicalRecordRepository medicalRecordRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private MedicalRecordMapper mapper;
    
    @Autowired
    private NotificationService notificationService;
    
    public MedicalRecordDTO createMedicalRecord(MedicalRecordDTO dto) {
        // CRITICAL FIX: Validate userId first
        if (dto.getUserId() == null || dto.getUserId().trim().isEmpty()) {
            throw new RuntimeException("User ID is required");
        }
        
        System.out.println("=== CREATE MEDICAL RECORD SERVICE ===");
        System.out.println("DTO userId: " + dto.getUserId());
        System.out.println("DTO diagnosis: " + dto.getDiagnosis());
        System.out.println("DTO vitalSigns: " + dto.getVitalSigns());
        
        // Create entity from DTO
        MedicalRecordEntity entity = mapper.toEntity(dto);
        
        // CRITICAL FIX: Find user by schoolId (userId in DTO is actually schoolId)
        UserEntity user = userRepository.findById(dto.getUserId())
            .orElseThrow(() -> new RuntimeException("User not found with school ID: " + dto.getUserId()));
        
        System.out.println("Found user: " + user.getSchoolId() + " - " + user.getFirstName() + " " + user.getLastName());
        entity.setUser(user);
        
        // Handle appointment if provided
        if (dto.getAppointmentId() != null) {
            AppointmentEntity appointment = appointmentRepository.findById(dto.getAppointmentId())
                .orElseThrow(() -> new RuntimeException("Appointment not found with ID: " + dto.getAppointmentId()));
            entity.setAppointment(appointment);
            
            if (!"COMPLETED".equalsIgnoreCase(appointment.getStatus())) {
                appointment.setStatus("COMPLETED");
                appointmentRepository.save(appointment);
            }
        }
        
        // Set timestamps
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        if (entity.getRecordDate() == null) {
            entity.setRecordDate(LocalDateTime.now());
        }
        
        System.out.println("Entity before save:");
        System.out.println("- User: " + (entity.getUser() != null ? entity.getUser().getSchoolId() : "null"));
        System.out.println("- Diagnosis: " + entity.getDiagnosis());
        System.out.println("- VitalSigns: " + entity.getVitalSigns());
        
        // Save to database
        MedicalRecordEntity savedEntity = medicalRecordRepository.save(entity);
        
        System.out.println("Entity saved successfully with ID: " + savedEntity.getRecordId());
        
        // Send notification to the student
        try {
            String notificationTitle = "New Medical Record Created";
            String notificationMessage = "A new medical record has been created for you. Please check your medical records for details.";
            
            notificationService.sendNotificationToUser(
                user.getSchoolId(),
                notificationTitle,
                notificationMessage,
                "medical_record"
            );
            
            System.out.println("Notification sent to student: " + user.getSchoolId());
        } catch (Exception e) {
            System.err.println("Failed to send notification: " + e.getMessage());
            // Don't fail the entire operation if notification fails
        }
        
        return mapper.toDTO(savedEntity);
    }
    
    public List<MedicalRecordDTO> getAllMedicalRecords() {
        return medicalRecordRepository.findAll().stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }
    
    public MedicalRecordDTO getMedicalRecordById(Long id) {
        MedicalRecordEntity entity = medicalRecordRepository.findById(id).orElse(null);
        return mapper.toDTO(entity);
    }
    
    public List<MedicalRecordDTO> getMedicalRecordsByUserId(String userId) {
        return medicalRecordRepository.findByUser_SchoolId(userId).stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }
    
    public List<MedicalRecordDTO> getMedicalRecordsByUserIdSorted(String userId) {
        return medicalRecordRepository.findByUser_SchoolIdOrderByRecordDateDesc(userId).stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }
    
    public MedicalRecordDTO getMedicalRecordByAppointmentId(Long appointmentId) {
        MedicalRecordEntity entity = medicalRecordRepository.findByAppointment_AppointmentId(appointmentId);
        return mapper.toDTO(entity);
    }
    
    public List<MedicalRecordDTO> getMedicalRecordsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return medicalRecordRepository.findByRecordDateBetween(startDate, endDate).stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }
    
    public List<MedicalRecordDTO> getMedicalRecordsByUserAndDateRange(String userId, LocalDateTime startDate, LocalDateTime endDate) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        return medicalRecordRepository.findByUserAndRecordDateBetween(user, startDate, endDate).stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }
    
    public List<MedicalRecordDTO> getMedicalRecordsByCreatedBy(String createdBy) {
        return medicalRecordRepository.findByCreatedBy(createdBy).stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }
    
    public MedicalRecordDTO updateMedicalRecord(Long id, MedicalRecordDTO dto) {
        MedicalRecordEntity existingEntity = medicalRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medical record not found with ID: " + id));
        
        // Update fields using mapper
        mapper.updateEntityFromDTO(dto, existingEntity);
        
        // Update user if userId changed
        if (dto.getUserId() != null && !dto.getUserId().equals(existingEntity.getUser().getSchoolId())) {
            UserEntity user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + dto.getUserId()));
            existingEntity.setUser(user);
        }
        
        // Update appointment if provided
        if (dto.getAppointmentId() != null) {
            AppointmentEntity appointment = appointmentRepository.findById(dto.getAppointmentId())
                .orElseThrow(() -> new RuntimeException("Appointment not found with ID: " + dto.getAppointmentId()));
            existingEntity.setAppointment(appointment);
        }
        
        existingEntity.setUpdatedAt(LocalDateTime.now());
        
        MedicalRecordEntity updatedEntity = medicalRecordRepository.save(existingEntity);
        return mapper.toDTO(updatedEntity);
    }
    
    public ResponseEntity<Map<String, Boolean>> deleteMedicalRecord(Long id) {
        return medicalRecordRepository.findById(id).map(record -> {
            medicalRecordRepository.delete(record);
            Map<String, Boolean> response = new HashMap<>();
            response.put("deleted", Boolean.TRUE);
            return ResponseEntity.ok(response);
        }).orElse(ResponseEntity.notFound().build());
    }
    
    public long getRecordCountByUserId(String userId) {
        UserEntity user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return 0;
        }
        return medicalRecordRepository.countByUser(user);
    }
}