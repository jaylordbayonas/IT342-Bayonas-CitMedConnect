package edu.cit.bayonas.citmedconnect.controller;

import edu.cit.bayonas.citmedconnect.entity.AppointmentEntity;
import edu.cit.bayonas.citmedconnect.entity.UserEntity;
import edu.cit.bayonas.citmedconnect.repository.UserRepository;
import edu.cit.bayonas.citmedconnect.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
@CrossOrigin(origins = "*")
public class AppointmentController {
    
    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private UserRepository userRepository;
    
    @GetMapping("/test")
    public String testEndpoint() {
        return "Appointment controller is working!";
    }
    
    @PostMapping("/")
    public AppointmentEntity createAppointment(@RequestBody AppointmentEntity appointment) {
        return appointmentService.createAppointment(appointment);
    }
    
    @GetMapping("/staff/all")
    public List<AppointmentEntity> getAllAppointmentsForStaff() {
        return appointmentService.getAllAppointments();
    }
    
    @GetMapping("/student/my-appointments")
    public List<AppointmentEntity> getMyAppointments(@RequestParam(required = false) String userId) {
        String resolvedUserId = resolveCurrentUserId(userId);
        if (resolvedUserId == null || resolvedUserId.isBlank()) {
            return List.of();
        }
        return appointmentService.getAppointmentsByUserId(resolvedUserId);
    }
    
    @GetMapping("/")
    public List<AppointmentEntity> getAllAppointments() {
        try {
            return appointmentService.getAllAppointments();
        } catch (Exception e) {
            return List.of();
        }
    }
    
    @GetMapping("/simple")
    public List<String> getSimpleAppointments() {
        return List.of("Test Appointment 1", "Test Appointment 2");
    }
    
    @GetMapping("/{id}")
    public AppointmentEntity getAppointmentById(@PathVariable Long id) {
        return appointmentService.getAppointmentById(id);
    }
    
    @GetMapping("/user/{userId}")
    public List<AppointmentEntity> getAppointmentsByUserId(@PathVariable String userId) {
        return appointmentService.getAppointmentsByUserId(userId);
    }
    
    @GetMapping("/status/{status}")
    public List<AppointmentEntity> getAppointmentsByStatus(@PathVariable String status) {
        return appointmentService.getAppointmentsByStatus(status);
    }
    
    @GetMapping("/timeslot/{timeSlotId}")
    public List<AppointmentEntity> getAppointmentsByTimeSlotId(@PathVariable Long timeSlotId) {
        return appointmentService.getAppointmentsByTimeSlotId(timeSlotId);
    }
    
    @PutMapping("/{id}")
    public AppointmentEntity updateAppointment(@PathVariable Long id, @RequestBody AppointmentEntity appointmentDetails) {
        return appointmentService.updateAppointment(id, appointmentDetails);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Boolean>> deleteAppointment(@PathVariable Long id) {
        return appointmentService.deleteAppointment(id);
    }
    
    @PutMapping("/{id}/cancel")
    public AppointmentEntity cancelAppointment(@PathVariable Long id) {
        return appointmentService.cancelAppointment(id);
    }
    
    @PutMapping("/{id}/confirm")
    public AppointmentEntity confirmAppointment(@PathVariable Long id) {
        return appointmentService.confirmAppointment(id);
    }
    
    @PutMapping("/{id}/complete")
    public AppointmentEntity completeAppointment(@PathVariable Long id) {
        return appointmentService.completeAppointment(id);
    }
    
    @PutMapping("/{id}/reschedule")
    public AppointmentEntity rescheduleAppointment(@PathVariable Long id, @RequestBody Map<String, Long> requestBody) {
        Long newTimeSlotId = requestBody.get("newTimeSlotId");
        if (newTimeSlotId == null) {
            throw new IllegalArgumentException("newTimeSlotId is required");
        }
        return appointmentService.rescheduleAppointment(id, newTimeSlotId);
    }

    private String resolveCurrentUserId(String userId) {
        if (userId != null && !userId.isBlank()) {
            return userId;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }

        String principal = authentication.getName();
        if (principal == null || principal.isBlank() || "anonymousUser".equalsIgnoreCase(principal)) {
            return null;
        }

        UserEntity user = userRepository.findBySchoolId(principal);
        if (user == null) {
            user = userRepository.findByEmail(principal);
        }

        return user != null ? user.getSchoolId() : principal;
    }
}
