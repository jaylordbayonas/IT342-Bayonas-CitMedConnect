package edu.cit.bayonas.citmedconnect.mapper;

import edu.cit.bayonas.citmedconnect.dto.MedicalRecordDTO;
import edu.cit.bayonas.citmedconnect.entity.MedicalRecordEntity;
import edu.cit.bayonas.citmedconnect.entity.UserEntity;
import edu.cit.bayonas.citmedconnect.entity.AppointmentEntity;
import edu.cit.bayonas.citmedconnect.repository.UserRepository;
import edu.cit.bayonas.citmedconnect.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MedicalRecordMapper {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AppointmentRepository appointmentRepository;

    public MedicalRecordDTO toDTO(MedicalRecordEntity entity) {
        if (entity == null) {
            return null;
        }
        
        MedicalRecordDTO dto = new MedicalRecordDTO();
        dto.setRecordId(entity.getRecordId());
        
        if (entity.getUser() != null) {
            dto.setUserId(entity.getUser().getSchoolId());
            dto.setUserName(entity.getUser().getFirstName() + " " + entity.getUser().getLastName());
        }
        
        if (entity.getAppointment() != null) {
            dto.setAppointmentId(entity.getAppointment().getAppointmentId());
        }
    
        dto.setDiagnosis(entity.getDiagnosis());
        dto.setSymptoms(entity.getSymptoms());
        dto.setTreatment(entity.getTreatment());
        dto.setPrescription(entity.getPrescription());
        dto.setVitalSigns(entity.getVitalSigns());
        dto.setAllergies(entity.getAllergies());
        dto.setMedicalHistory(entity.getMedicalHistory());
        dto.setNotes(entity.getNotes());
        dto.setRecordDate(entity.getRecordDate());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        
        return dto;
    }
    
    public MedicalRecordEntity toEntity(MedicalRecordDTO dto) {
        if (dto == null) {
            return null;
        }
        
        MedicalRecordEntity entity = new MedicalRecordEntity();
        
        if (dto.getUserId() != null && !dto.getUserId().trim().isEmpty()) {
            UserEntity user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with school ID: " + dto.getUserId()));
            entity.setUser(user);
        } else {
            throw new RuntimeException("User ID (schoolId) is required to create a medical record");
        }
        
        if (dto.getAppointmentId() != null) {
            AppointmentEntity appointment = appointmentRepository.findById(dto.getAppointmentId())
                .orElseThrow(() -> new RuntimeException("Appointment not found with ID: " + dto.getAppointmentId()));
            entity.setAppointment(appointment);
        }
        
        entity.setDiagnosis(dto.getDiagnosis());
        entity.setSymptoms(dto.getSymptoms());
        entity.setTreatment(dto.getTreatment());
        entity.setPrescription(dto.getPrescription());
        entity.setVitalSigns(dto.getVitalSigns());
        entity.setAllergies(dto.getAllergies());
        entity.setMedicalHistory(dto.getMedicalHistory());
        entity.setNotes(dto.getNotes());
        entity.setRecordDate(dto.getRecordDate());
        entity.setCreatedBy(dto.getCreatedBy());
        
        return entity;
    }
    
    public void updateEntityFromDTO(MedicalRecordDTO dto, MedicalRecordEntity entity) {
        if (dto == null || entity == null) {
            return;
        }
        
        if (dto.getUserId() != null && !dto.getUserId().trim().isEmpty()) {
            UserEntity user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with school ID: " + dto.getUserId()));
            entity.setUser(user);
        }
        
        if (dto.getAppointmentId() != null) {
            AppointmentEntity appointment = appointmentRepository.findById(dto.getAppointmentId())
                .orElseThrow(() -> new RuntimeException("Appointment not found with ID: " + dto.getAppointmentId()));
            entity.setAppointment(appointment);
        }
        
        if (dto.getDiagnosis() != null) {
            entity.setDiagnosis(dto.getDiagnosis());
        }
        if (dto.getSymptoms() != null) {
            entity.setSymptoms(dto.getSymptoms());
        }
        if (dto.getTreatment() != null) {
            entity.setTreatment(dto.getTreatment());
        }
        if (dto.getPrescription() != null) {
            entity.setPrescription(dto.getPrescription());
        }
        if (dto.getVitalSigns() != null) {
            entity.setVitalSigns(dto.getVitalSigns());
        }
        if (dto.getAllergies() != null) {
            entity.setAllergies(dto.getAllergies());
        }
        if (dto.getMedicalHistory() != null) {
            entity.setMedicalHistory(dto.getMedicalHistory());
        }
        if (dto.getNotes() != null) {
            entity.setNotes(dto.getNotes());
        }
        if (dto.getRecordDate() != null) {
            entity.setRecordDate(dto.getRecordDate());
        }
        if (dto.getCreatedBy() != null) {
            entity.setCreatedBy(dto.getCreatedBy());
        }
    }
}