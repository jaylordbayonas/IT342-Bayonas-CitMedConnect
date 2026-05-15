package edu.cit.bayonas.citmedconnect.features.appointments.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AppointmentDTO {
    @NotNull(message = "Slot ID is required")
    private Long slotId;
    
    @NotNull(message = "Student ID is required")
    private Long studentId;
    
    @Size(max = 500, message = "Reason must not exceed 500 characters")
    private String reason;
    
    @Size(max = 1000, message = "Symptoms must not exceed 1000 characters")
    private String symptoms;

    // Getters and Setters
    public Long getSlotId() {
        return slotId;
    }

    public void setSlotId(Long slotId) {
        this.slotId = slotId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }
}