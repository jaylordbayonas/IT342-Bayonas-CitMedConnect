package edu.cit.bayonas.citmedconnect.features.appointments.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.time.LocalDate;
import java.time.LocalTime;

public class TimeSlotDTO {
    private Long timeSlotId;
    
    @NotNull(message = "Slot date is required")
    private LocalDate slotDate;
    
    @NotNull(message = "Start time is required")
    private LocalTime startTime;
    
    @NotNull(message = "End time is required")
    private LocalTime endTime;
    
    private boolean isAvailable;
    
    @Min(value = 1, message = "Max bookings must be at least 1")
    @Max(value = 10, message = "Max bookings cannot exceed 10")
    private int maxBookings;
    
    @Min(value = 0, message = "Current bookings cannot be negative")
    private int currentBookings;
    
    private String staffId;
    private boolean isWithinBusinessHours;

    public Long getTimeSlotId() {
        return timeSlotId;
    }

    public void setTimeSlotId(Long timeSlotId) {
        this.timeSlotId = timeSlotId;
    }

    public LocalDate getSlotDate() {
        return slotDate;
    }

    public void setSlotDate(LocalDate slotDate) {
        this.slotDate = slotDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public int getMaxBookings() {
        return maxBookings;
    }

    public void setMaxBookings(int maxBookings) {
        this.maxBookings = maxBookings;
    }

    public int getCurrentBookings() {
        return currentBookings;
    }

    public void setCurrentBookings(int currentBookings) {
        this.currentBookings = currentBookings;
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public boolean isWithinBusinessHours() {
        return isWithinBusinessHours;
    }

    public void setWithinBusinessHours(boolean withinBusinessHours) {
        isWithinBusinessHours = withinBusinessHours;
    }
}