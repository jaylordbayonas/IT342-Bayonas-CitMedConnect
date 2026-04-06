package edu.cit.bayonas.citmedconnect.controller;

import edu.cit.bayonas.citmedconnect.entity.TimeSlot;
import edu.cit.bayonas.citmedconnect.entity.AppointmentEntity;
import edu.cit.bayonas.citmedconnect.entity.UserEntity;
import edu.cit.bayonas.citmedconnect.repository.UserRepository;
import edu.cit.bayonas.citmedconnect.service.TimeSlotService;
import edu.cit.bayonas.citmedconnect.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/timeslots")
@CrossOrigin(origins = "*")
public class TimeSlotController {

    @Autowired
    private TimeSlotService timeSlotService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<TimeSlot> createSlot(
            @RequestBody TimeSlot slot,
            @RequestHeader(value = "X-User-ID", required = false) String requesterId,
            @RequestHeader(value = "X-User-Role", required = false) String requesterRole) {
        if (!isAdminRequest(requesterId, requesterRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (slot.getMaxBookings() <= 0) {
            slot.setMaxBookings(1);
        }
        if (slot.getCurrentBookings() == 0) {
            slot.setCurrentBookings(0);
        }
        if (slot.isAvailable() == false) {
            slot.setAvailable(true);
        }
        if (slot.getStartTime() != null && slot.getEndTime() != null) {
            LocalTime start = slot.getStartTime();
            LocalTime end = slot.getEndTime();
            boolean isBusinessHours = !start.isBefore(LocalTime.of(8, 0)) && 
                                     !end.isAfter(LocalTime.of(18, 0));
            slot.setWithinBusinessHours(isBusinessHours);
        } else {
            slot.setWithinBusinessHours(true);
        }
        
        TimeSlot createdSlot = timeSlotService.createSlot(slot);
        return ResponseEntity.ok(createdSlot);
    }

    @GetMapping
    public List<TimeSlot> getAllSlots() {
        return timeSlotService.getAllSlots();
    }

    @GetMapping("/available")
    public ResponseEntity<List<TimeSlot>> getAvailableSlotsByDate(
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
            LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        List<TimeSlot> availableSlots = timeSlotService.findAvailableSlots(date);
        return ResponseEntity.ok(availableSlots);
    }

    @GetMapping("/staff/{staffId}")
    public List<TimeSlot> getSlotsByStaff(@PathVariable String staffId) {
        return timeSlotService.getStaffSlots(staffId);
    }

    @GetMapping("/{timeSlotId}")
    public ResponseEntity<TimeSlot> getSlotById(@PathVariable Long timeSlotId) {
        TimeSlot slot = timeSlotService.getSlotById(timeSlotId);
        return ResponseEntity.ofNullable(slot);
    }

    @PutMapping("/{timeSlotId}")
    public ResponseEntity<TimeSlot> updateSlot(
            @PathVariable Long timeSlotId, 
            @RequestBody TimeSlot slot,
            @RequestHeader(value = "X-User-ID", required = false) String requesterId,
            @RequestHeader(value = "X-User-Role", required = false) String requesterRole) {
        if (!isAdminRequest(requesterId, requesterRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        TimeSlot updated = timeSlotService.updateSlot(timeSlotId, slot);
        return updated != null ? 
               ResponseEntity.ok(updated) : 
               ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{timeSlotId}")
    public ResponseEntity<Void> deleteSlot(
            @PathVariable Long timeSlotId,
            @RequestHeader(value = "X-User-ID", required = false) String requesterId,
            @RequestHeader(value = "X-User-Role", required = false) String requesterRole) {
        if (!isAdminRequest(requesterId, requesterRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (timeSlotService.deleteSlot(timeSlotId)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{timeSlotId}/book")
    public ResponseEntity<AppointmentEntity> bookTimeSlot(
            @PathVariable Long timeSlotId,
            @RequestBody Map<String, String> bookingDetails) {
        try {
            String studentId = bookingDetails.get("studentId");
            String reason = bookingDetails.get("reason");
            String notes = bookingDetails.get("notes");
            
            if (studentId == null || studentId.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            AppointmentEntity appointment = appointmentService.bookAppointment(
                timeSlotId, studentId, reason, notes);
            return ResponseEntity.ok(appointment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private boolean isAdminRequest(String requesterId, String requesterRole) {
        if (requesterRole == null || !"ADMIN".equalsIgnoreCase(requesterRole)) {
            return false;
        }

        UserEntity requester = resolveUser(requesterId);
        return requester != null && "ADMIN".equalsIgnoreCase(requester.getRole());
    }

    private UserEntity resolveUser(String requesterId) {
        if (requesterId == null || requesterId.isBlank()) {
            return null;
        }

        UserEntity user = userRepository.findById(requesterId).orElse(null);
        if (user != null) {
            return user;
        }

        user = userRepository.findBySchoolId(requesterId);
        if (user != null) {
            return user;
        }

        return userRepository.findByEmail(requesterId);
    }
}