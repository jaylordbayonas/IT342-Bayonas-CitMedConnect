package edu.cit.bayonas.citmedconnect.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "time_slots")
public class TimeSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long timeSlotId;

    private LocalDate slotDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean isAvailable;
    private int maxBookings;
    private int currentBookings;
    
    @Column(name = "staff_id")
    private String staffId;
    
    @Column(name = "is_within_business_hours")
    private boolean isWithinBusinessHours;
    
    @ManyToOne
    @JoinColumn(name = "staff_id", insertable = false, updatable = false)
    private UserEntity staff;

   
    public TimeSlot() {}

   
    public Long getTimeSlotId() { return timeSlotId; }
    public void setTimeSlotId(Long timeSlotId) { this.timeSlotId = timeSlotId; }

    public LocalDate getSlotDate() { return slotDate; }
    public void setSlotDate(LocalDate slotDate) { this.slotDate = slotDate; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }

    public int getMaxBookings() { return maxBookings; }
    public void setMaxBookings(int maxBookings) { this.maxBookings = maxBookings; }

    public int getCurrentBookings() { return currentBookings; }
    public void setCurrentBookings(int currentBookings) { this.currentBookings = currentBookings; }
    
    public String getStaffId() { return staffId; }
    public void setStaffId(String staffId) { this.staffId = staffId; }
    
    public boolean isWithinBusinessHours() {
        return isWithinBusinessHours;
    }
    
    public void setWithinBusinessHours(boolean withinBusinessHours) {
        isWithinBusinessHours = withinBusinessHours;
    }
    
    public UserEntity getStaff() {
        return staff;
    }
    
    public void setStaff(UserEntity staff) {
        this.staff = staff;
    }
}
