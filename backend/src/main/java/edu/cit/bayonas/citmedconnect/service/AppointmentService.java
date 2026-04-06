package edu.cit.bayonas.citmedconnect.service;

import edu.cit.bayonas.citmedconnect.entity.AppointmentEntity;
import edu.cit.bayonas.citmedconnect.entity.UserEntity;
import edu.cit.bayonas.citmedconnect.entity.TimeSlot;
import edu.cit.bayonas.citmedconnect.repository.AppointmentRepository;
import edu.cit.bayonas.citmedconnect.repository.UserRepository;
import edu.cit.bayonas.citmedconnect.repository.TimeSlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AppointmentService {
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TimeSlotRepository timeSlotRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    public AppointmentEntity createAppointment(AppointmentEntity appointment) {
        appointment.setStatus(normalizeStatusValue(appointment.getStatus()));

        if (appointment.getUser() != null) {
            UserEntity user = resolveUser(appointment.getUser().getSchoolId());
            if (user == null) {
                throw new RuntimeException("User not found");
            }
            appointment.setUser(user);
        }
        
        if (appointment.getTimeSlot() != null) {
            TimeSlot timeSlot = timeSlotRepository.findById(appointment.getTimeSlot().getTimeSlotId()).orElse(null);
            if (timeSlot == null) {
                throw new RuntimeException("Time slot not found");
            }
            if (!timeSlot.isAvailable()) {
                throw new RuntimeException("Time slot is not available");
            }
            appointment.setTimeSlot(timeSlot);
            
            timeSlot.setCurrentBookings(timeSlot.getCurrentBookings() + 1);
            if (timeSlot.getCurrentBookings() >= timeSlot.getMaxBookings()) {
                timeSlot.setAvailable(false);
            }
            timeSlotRepository.save(timeSlot);
        }
        
        appointment.setCreatedAt(LocalDateTime.now());
        appointment.setUpdatedAt(LocalDateTime.now());
        AppointmentEntity savedAppointment = appointmentRepository.save(appointment);
        
        // Send notification to all staff/admin about new appointment
        try {
            String studentName = appointment.getUser() != null 
                ? appointment.getUser().getFirstName() + " " + appointment.getUser().getLastName()
                : "A student";
            String notificationTitle = "New Appointment Booked";
            String notificationMessage = studentName + " has booked a new appointment. Reason: " 
                + (appointment.getReason() != null ? appointment.getReason() : "Not specified");
            
            notificationService.sendNotificationToAllStaff(
                notificationTitle,
                notificationMessage,
                "appointment"
            );
            
            System.out.println("Notification sent to all staff/admin about new appointment");
        } catch (Exception e) {
            System.err.println("Failed to send notification to staff: " + e.getMessage());
            // Don't fail the entire operation if notification fails
        }
        
        return savedAppointment;
    }
    
    public List<AppointmentEntity> getAllAppointments() {
        return appointmentRepository.findAll();
    }
    
    public AppointmentEntity getAppointmentById(Long id) {
        return appointmentRepository.findById(id).orElse(null);
    }
    
    public List<AppointmentEntity> getAppointmentsByUserId(String userId) {
        return appointmentRepository.findByUser_SchoolId(userId);
    }
    
    public List<AppointmentEntity> getAppointmentsByStatus(String status) {
        return appointmentRepository.findByStatus(normalizeStatusValue(status));
    }
    
    public List<AppointmentEntity> getAppointmentsByTimeSlotId(Long timeSlotId) {
        return appointmentRepository.findByTimeSlot_TimeSlotId(timeSlotId);
    }
    
    public AppointmentEntity updateAppointment(Long id, AppointmentEntity appointmentDetails) {
        return appointmentRepository.findById(id).map(existingAppointment -> {
            if (appointmentDetails.getStatus() != null) {
                String oldStatus = existingAppointment.getStatus();
                String nextStatus = normalizeStatusValue(appointmentDetails.getStatus());
                existingAppointment.setStatus(nextStatus);
                
                if ("CANCELLED".equals(nextStatus) && !"CANCELLED".equals(oldStatus)) {
                    TimeSlot timeSlot = existingAppointment.getTimeSlot();
                    if (timeSlot != null) {
                        timeSlot.setCurrentBookings(Math.max(0, timeSlot.getCurrentBookings() - 1));
                        timeSlot.setAvailable(true);
                        timeSlotRepository.save(timeSlot);
                    }
                }
            }
            if (appointmentDetails.getReason() != null) {
                existingAppointment.setReason(appointmentDetails.getReason());
            }
            if (appointmentDetails.getNotes() != null) {
                existingAppointment.setNotes(appointmentDetails.getNotes());
            }
            
            existingAppointment.setUpdatedAt(LocalDateTime.now());
            return appointmentRepository.save(existingAppointment);
        }).orElse(null);
    }
    
    public ResponseEntity<Map<String, Boolean>> deleteAppointment(Long id) {
        return appointmentRepository.findById(id).map(appointment -> {
            TimeSlot timeSlot = appointment.getTimeSlot();
            if (timeSlot != null && !"CANCELLED".equalsIgnoreCase(appointment.getStatus())) {
                timeSlot.setCurrentBookings(Math.max(0, timeSlot.getCurrentBookings() - 1));
                timeSlot.setAvailable(true);
                timeSlotRepository.save(timeSlot);
            }
            
            appointmentRepository.delete(appointment);
            Map<String, Boolean> response = new HashMap<>();
            response.put("deleted", Boolean.TRUE);
            return ResponseEntity.ok(response);
        }).orElse(ResponseEntity.notFound().build());
    }
    
    public AppointmentEntity cancelAppointment(Long id) {
        return appointmentRepository.findById(id).map(appointment -> {
            if (!"CANCELLED".equalsIgnoreCase(appointment.getStatus())) {
                appointment.setStatus("CANCELLED");
                
                TimeSlot timeSlot = appointment.getTimeSlot();
                if (timeSlot != null) {
                    timeSlot.setCurrentBookings(Math.max(0, timeSlot.getCurrentBookings() - 1));
                    timeSlot.setAvailable(true);
                    timeSlotRepository.save(timeSlot);
                }
                
                appointment.setUpdatedAt(LocalDateTime.now());
                return appointmentRepository.save(appointment);
            }
            return appointment;
        }).orElse(null);
    }
    
    public AppointmentEntity confirmAppointment(Long id) {
        return appointmentRepository.findById(id).map(appointment -> {
            appointment.setStatus("SCHEDULED");
            appointment.setUpdatedAt(LocalDateTime.now());
            return appointmentRepository.save(appointment);
        }).orElse(null);
    }
    
    public AppointmentEntity completeAppointment(Long id) {
        return appointmentRepository.findById(id).map(appointment -> {
            appointment.setStatus("COMPLETED");
            appointment.setUpdatedAt(LocalDateTime.now());
            return appointmentRepository.save(appointment);
        }).orElse(null);
    }
    
    public AppointmentEntity bookAppointment(Long timeSlotId, String studentId, String reason, String notes) {
        TimeSlot timeSlot = timeSlotRepository.findById(timeSlotId)
            .orElseThrow(() -> new IllegalArgumentException("Time slot not found"));
        
        if (!timeSlot.isAvailable()) {
            throw new IllegalStateException("Time slot is not available");
        }
        
        if (timeSlot.getCurrentBookings() >= timeSlot.getMaxBookings()) {
            throw new IllegalStateException("Time slot is fully booked");
        }
        
        UserEntity user = resolveUser(studentId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        
        AppointmentEntity appointment = new AppointmentEntity();
        appointment.setUser(user);
        appointment.setTimeSlot(timeSlot);
        appointment.setStatus("SCHEDULED");
        appointment.setReason(reason);
        appointment.setNotes(notes);
        appointment.setCreatedAt(LocalDateTime.now());
        appointment.setUpdatedAt(LocalDateTime.now());
        
        timeSlot.setCurrentBookings(timeSlot.getCurrentBookings() + 1);
        if (timeSlot.getCurrentBookings() >= timeSlot.getMaxBookings()) {
            timeSlot.setAvailable(false);
        }
        timeSlotRepository.save(timeSlot);
        
        return appointmentRepository.save(appointment);
    }
    
    public AppointmentEntity rescheduleAppointment(Long appointmentId, Long newTimeSlotId) {
        AppointmentEntity appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
        
        TimeSlot newTimeSlot = timeSlotRepository.findById(newTimeSlotId)
            .orElseThrow(() -> new IllegalArgumentException("New time slot not found"));
        
        if (!newTimeSlot.isAvailable()) {
            throw new IllegalStateException("New time slot is not available");
        }
        
        if (newTimeSlot.getCurrentBookings() >= newTimeSlot.getMaxBookings()) {
            throw new IllegalStateException("New time slot is fully booked");
        }
        
        TimeSlot oldTimeSlot = appointment.getTimeSlot();
        
        if (oldTimeSlot != null) {
            oldTimeSlot.setCurrentBookings(Math.max(0, oldTimeSlot.getCurrentBookings() - 1));
            oldTimeSlot.setAvailable(true);
            timeSlotRepository.save(oldTimeSlot);
        }
        
        appointment.setTimeSlot(newTimeSlot);
        appointment.setStatus("RESCHEDULED");
        appointment.setUpdatedAt(LocalDateTime.now());
        
        newTimeSlot.setCurrentBookings(newTimeSlot.getCurrentBookings() + 1);
        if (newTimeSlot.getCurrentBookings() >= newTimeSlot.getMaxBookings()) {
            newTimeSlot.setAvailable(false);
        }
        timeSlotRepository.save(newTimeSlot);
        
        return appointmentRepository.save(appointment);
    }

    private UserEntity resolveUser(String userId) {
        if (userId == null || userId.isBlank()) {
            return null;
        }

        UserEntity user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            return user;
        }

        user = userRepository.findBySchoolId(userId);
        if (user != null) {
            return user;
        }

        return userRepository.findByEmail(userId);
    }

    private String normalizeStatusValue(String rawStatus) {
        if (rawStatus == null || rawStatus.isBlank()) {
            return "SCHEDULED";
        }

        String normalized = rawStatus.trim().toLowerCase();
        return switch (normalized) {
            case "pending", "confirmed", "scheduled" -> "SCHEDULED";
            case "completed" -> "COMPLETED";
            case "cancelled" -> "CANCELLED";
            case "rescheduled" -> "RESCHEDULED";
            case "success" -> "SUCCESS";
            default -> "SCHEDULED";
        };
    }
}
