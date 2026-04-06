package edu.cit.bayonas.citmedconnect.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class TimeSlotDTO {
    private LocalDate date;
    private LocalTime time;
    private Long staffId;
    private String location;

    // Getters and Setters
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public Long getStaffId() {
        return staffId;
    }

    public void setStaffId(Long staffId) {
        this.staffId = staffId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
