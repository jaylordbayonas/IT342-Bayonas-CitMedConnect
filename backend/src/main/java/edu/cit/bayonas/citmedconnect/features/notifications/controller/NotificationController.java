package edu.cit.bayonas.citmedconnect.features.notifications.controller;

import edu.cit.bayonas.citmedconnect.features.notifications.dto.BroadcastNotificationDTO;
import edu.cit.bayonas.citmedconnect.features.notifications.dto.NotificationDTO;
import edu.cit.bayonas.citmedconnect.features.notifications.dto.SendNotificationDTO;
import edu.cit.bayonas.citmedconnect.features.notifications.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    // Test endpoint
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> test() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "Notification Controller is working!");
        response.put("feature", "notifications");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/")
    public ResponseEntity<?> createNotification(@Valid @RequestBody NotificationDTO notification, BindingResult bindingResult) {
        try {
            // Check for validation errors
            if (bindingResult.hasErrors()) {
                Map<String, String> errors = bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(
                        error -> error.getField(),
                        error -> error.getDefaultMessage(),
                        (existing, replacement) -> existing
                    ));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
            }
            
            NotificationDTO created = notificationService.createNotification(notification);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendNotification(@Valid @RequestBody SendNotificationDTO request, BindingResult bindingResult) {
        try {
            // Check for validation errors
            if (bindingResult.hasErrors()) {
                Map<String, String> errors = bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(
                        error -> error.getField(),
                        error -> error.getDefaultMessage(),
                        (existing, replacement) -> existing
                    ));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
            }
            
            NotificationDTO notification = notificationService.sendNotificationToUser(
                request.getRecipientId(), 
                request.getTitle(), 
                request.getMessage(), 
                request.getType()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(notification);
        } catch (RuntimeException e) {
            System.err.println("Error in sendNotification: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        } catch (Exception e) {
            System.err.println("Unexpected error in sendNotification: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/")
    public ResponseEntity<List<NotificationDTO>> getAllNotifications() {
        List<NotificationDTO> notifications = notificationService.getAllNotifications();
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getNotificationById(@PathVariable String id) {
        try {
            NotificationDTO notification = notificationService.getNotificationById(id);
            return ResponseEntity.ok(notification);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @GetMapping("/user/{schoolId}")
    public ResponseEntity<List<NotificationDTO>> getNotificationsBySchoolId(@PathVariable String schoolId) {
        List<NotificationDTO> notifications = notificationService.getNotificationsByUserId(schoolId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/user/{schoolId}/role/{userRole}")
    public ResponseEntity<?> getNotificationsForUser(
            @PathVariable String schoolId, 
            @PathVariable String userRole) {
        try {
            List<NotificationDTO> notifications = notificationService.getNotificationsForUser(schoolId, userRole);
            return ResponseEntity.ok(notifications);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateNotification(
            @PathVariable String id,
            @RequestBody NotificationDTO notification) {
        try {
            // Check if notification exists first (before validation)
            try {
                NotificationDTO existingNotification = notificationService.getNotificationById(id);
                if (existingNotification == null) {
                    Map<String, String> errorResponse = new HashMap<>();
                    errorResponse.put("error", "Notification not found with id: " + id);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
                }
            } catch (RuntimeException e) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Notification not found with id: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            NotificationDTO updated = notificationService.updateNotification(id, notification);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotification(@PathVariable String id) {
        boolean deleted = notificationService.deleteNotification(id);
        if (deleted) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Notification deleted successfully");
            return ResponseEntity.ok(response);
        }
        
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Notification not found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
    
    @PutMapping("/{id}/read")
    public ResponseEntity<?> markNotificationAsRead(@PathVariable String id) {
        try {
            NotificationDTO notification = notificationService.markNotificationAsRead(id);
            return ResponseEntity.ok(notification);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }
    
    @PostMapping("/broadcast/students")
    public ResponseEntity<?> sendNotificationToAllStudents(
            @Valid @RequestBody BroadcastNotificationDTO request, BindingResult bindingResult) {
        try {
            // Check for validation errors
            if (bindingResult.hasErrors()) {
                Map<String, String> errors = bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(
                        error -> error.getField(),
                        error -> error.getDefaultMessage(),
                        (existing, replacement) -> existing
                    ));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
            }
            
            List<NotificationDTO> notifications = notificationService.sendNotificationToAllStudents(
                request.getTitle(), 
                request.getMessage(), 
                "STUDENT_BROADCAST"
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(notifications);
        } catch (RuntimeException e) {
            System.err.println("Error in sendNotificationToAllStudents: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        } catch (Exception e) {
            System.err.println("Unexpected error in sendNotificationToAllStudents: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @PostMapping("/broadcast/staff")
    public ResponseEntity<?> sendNotificationToAllStaff(
            @Valid @RequestBody BroadcastNotificationDTO request, BindingResult bindingResult) {
        try {
            // Check for validation errors
            if (bindingResult.hasErrors()) {
                Map<String, String> errors = bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(
                        error -> error.getField(),
                        error -> error.getDefaultMessage(),
                        (existing, replacement) -> existing
                    ));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
            }
            
            List<NotificationDTO> notifications = notificationService.sendNotificationToAllStaff(
                request.getTitle(), 
                request.getMessage(), 
                "STAFF_BROADCAST"
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(notifications);
        } catch (RuntimeException e) {
            System.err.println("Error in sendNotificationToAllStaff: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        } catch (Exception e) {
            System.err.println("Unexpected error in sendNotificationToAllStaff: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @PostMapping("/broadcast/all")
    public ResponseEntity<?> sendNotificationToEveryone(
            @Valid @RequestBody BroadcastNotificationDTO request, BindingResult bindingResult) {
        try {
            // Check for validation errors
            if (bindingResult.hasErrors()) {
                Map<String, String> errors = bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(
                        error -> error.getField(),
                        error -> error.getDefaultMessage(),
                        (existing, replacement) -> existing
                    ));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
            }
            
            List<NotificationDTO> notifications = notificationService.sendNotificationToEveryone(
                request.getTitle(), 
                request.getMessage(), 
                "GLOBAL_BROADCAST"
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(notifications);
        } catch (RuntimeException e) {
            System.err.println("Error in sendNotificationToEveryone: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        } catch (Exception e) {
            System.err.println("Unexpected error in sendNotificationToEveryone: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @GetMapping("/user/{schoolId}/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadNotificationCount(@PathVariable String schoolId) {
        long count = notificationService.getUnreadNotificationCount(schoolId);
        Map<String, Long> response = new HashMap<>();
        response.put("unreadCount", count);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/user/{schoolId}/all")
    public ResponseEntity<List<NotificationDTO>> getAllNotificationsForUser(@PathVariable String schoolId) {
        List<NotificationDTO> notifications = notificationService.getAllNotificationsForUser(schoolId);
        return ResponseEntity.ok(notifications);
    }
}