package edu.cit.bayonas.citmedconnect.features.medicalrecords.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public class MedicalRecordDTO {
    
    private Long recordId;
    
    @NotBlank(message = "User ID is required")
    @Size(max = 20, message = "User ID must not exceed 20 characters")
    private String userId;  // This is schoolId
    
    private String userName;
    private Long appointmentId;
    
    @NotBlank(message = "Diagnosis is required")
    @Size(max = 1000, message = "Diagnosis must not exceed 1000 characters")
    private String diagnosis;
    
    @Size(max = 1000, message = "Symptoms must not exceed 1000 characters")
    private String symptoms;
    
    @Size(max = 1000, message = "Treatment must not exceed 1000 characters")
    private String treatment;
    
    @Size(max = 500, message = "Prescription must not exceed 500 characters")
    private String prescription;
    
    @Size(max = 500, message = "Vital signs must not exceed 500 characters")
    private String vitalSigns;  // JSON string
    
    @Size(max = 500, message = "Allergies must not exceed 500 characters")
    private String allergies;
    
    @Size(max = 1000, message = "Medical history must not exceed 1000 characters")
    private String medicalHistory;
    
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime recordDate;
    
    private String createdBy;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    // Constructors
    public MedicalRecordDTO() {}
    
    public MedicalRecordDTO(Long recordId, String userId, String userName, String diagnosis) {
        this.recordId = recordId;
        this.userId = userId;
        this.userName = userName;
        this.diagnosis = diagnosis;
    }
    
    // Getters and Setters
    public Long getRecordId() {
        return recordId;
    }
    
    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public Long getAppointmentId() {
        return appointmentId;
    }
    
    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }
    
    public String getDiagnosis() {
        return diagnosis;
    }
    
    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }
    
    public String getSymptoms() {
        return symptoms;
    }
    
    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }
    
    public String getTreatment() {
        return treatment;
    }
    
    public void setTreatment(String treatment) {
        this.treatment = treatment;
    }
    
    public String getPrescription() {
        return prescription;
    }
    
    public void setPrescription(String prescription) {
        this.prescription = prescription;
    }
    
    public String getVitalSigns() {
        return vitalSigns;
    }
    
    public void setVitalSigns(String vitalSigns) {
        this.vitalSigns = vitalSigns;
    }
    
    public String getAllergies() {
        return allergies;
    }
    
    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }
    
    public String getMedicalHistory() {
        return medicalHistory;
    }
    
    public void setMedicalHistory(String medicalHistory) {
        this.medicalHistory = medicalHistory;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public LocalDateTime getRecordDate() {
        return recordDate;
    }
    
    public void setRecordDate(LocalDateTime recordDate) {
        this.recordDate = recordDate;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public String toString() {
        return "MedicalRecordDTO{" +
                "recordId=" + recordId +
                ", userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                ", appointmentId=" + appointmentId +
                ", diagnosis='" + diagnosis + '\'' +
                ", symptoms='" + symptoms + '\'' +
                ", treatment='" + treatment + '\'' +
                ", prescription='" + prescription + '\'' +
                ", vitalSigns='" + vitalSigns + '\'' +
                ", allergies='" + allergies + '\'' +
                ", medicalHistory='" + medicalHistory + '\'' +
                ", notes='" + notes + '\'' +
                ", recordDate=" + recordDate +
                ", createdBy='" + createdBy + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}