package edu.cit.bayonas.citmedconnect.controller;

import edu.cit.bayonas.citmedconnect.entity.NotificationEntity;
import edu.cit.bayonas.citmedconnect.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/")
    public ResponseEntity<NotificationEntity> createNotification(@RequestBody NotificationEntity notification) {
        try {
            NotificationEntity created = notificationService.createNotification(notification);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/send")
    public ResponseEntity<NotificationEntity> sendNotification(@RequestBody Map<String, String> request) {
        try {
            String recipientId = request.get("recipientId");
            String title = request.get("title");
            String message = request.get("message");
            String type = request.getOrDefault("type", "info");
            
            if (recipientId == null || recipientId.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            if (title == null || title.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            NotificationEntity notification = notificationService.sendNotificationToUser(recipientId, title, message, type);
            return ResponseEntity.status(HttpStatus.CREATED).body(notification);
        } catch (RuntimeException e) {
            System.err.println("Error in sendNotification: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            System.err.println("Unexpected error in sendNotification: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/")
    public ResponseEntity<List<NotificationEntity>> getAllNotifications() {
        List<NotificationEntity> notifications = notificationService.getAllNotifications();
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationEntity> getNotificationById(@PathVariable String id) {
        try {
            NotificationEntity notification = notificationService.getNotificationById(id);
            return ResponseEntity.ok(notification);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/user/{schoolId}")
    public ResponseEntity<List<NotificationEntity>> getNotificationsBySchoolId(@PathVariable String schoolId) {
        List<NotificationEntity> notifications = notificationService.getNotificationsByUserId(schoolId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/user/{schoolId}/role/{userRole}")
    public ResponseEntity<List<NotificationEntity>> getNotificationsForUser(
            @PathVariable String schoolId, 
            @PathVariable String userRole) {
        try {
            List<NotificationEntity> notifications = notificationService.getNotificationsForUser(schoolId, userRole);
            return ResponseEntity.ok(notifications);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<NotificationEntity> updateNotification(
            @PathVariable String id,
            @RequestBody NotificationEntity notification) {
        try {
            NotificationEntity updated = notificationService.updateNotification(id, notification);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteNotification(@PathVariable String id) {
        boolean deleted = notificationService.deleteNotification(id);
        if (deleted) {
            return ResponseEntity.ok("Notification deleted successfully");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Notification not found");
    }
    
    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationEntity> markNotificationAsRead(@PathVariable String id) {
        try {
            NotificationEntity notification = notificationService.markNotificationAsRead(id);
            return ResponseEntity.ok(notification);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/broadcast/students")
    public ResponseEntity<List<NotificationEntity>> sendNotificationToAllStudents(
            @RequestBody Map<String, String> request) {
        try {
            String title = request.get("title");
            String message = request.get("message");
            String type = request.getOrDefault("type", "info");
            
            if (title == null || title.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            List<NotificationEntity> notifications = notificationService.sendNotificationToAllStudents(title, message, "STUDENT_BROADCAST");
            return ResponseEntity.status(HttpStatus.CREATED).body(notifications);
        } catch (RuntimeException e) {
            System.err.println("Error in sendNotificationToAllStudents: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            System.err.println("Unexpected error in sendNotificationToAllStudents: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping("/broadcast/staff")
    public ResponseEntity<List<NotificationEntity>> sendNotificationToAllStaff(
            @RequestBody Map<String, String> request) {
        try {
            String title = request.get("title");
            String message = request.get("message");
            String type = request.getOrDefault("type", "info");
            
            if (title == null || title.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            List<NotificationEntity> notifications = notificationService.sendNotificationToAllStaff(title, message, "STAFF_BROADCAST");
            return ResponseEntity.status(HttpStatus.CREATED).body(notifications);
        } catch (RuntimeException e) {
            System.err.println("Error in sendNotificationToAllStaff: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            System.err.println("Unexpected error in sendNotificationToAllStaff: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping("/broadcast/all")
    public ResponseEntity<List<NotificationEntity>> sendNotificationToEveryone(
            @RequestBody Map<String, String> request) {
        try {
            String title = request.get("title");
            String message = request.get("message");
            String type = request.getOrDefault("type", "info");
            
            if (title == null || title.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            List<NotificationEntity> notifications = notificationService.sendNotificationToEveryone(title, message, "GLOBAL_BROADCAST");
            return ResponseEntity.status(HttpStatus.CREATED).body(notifications);
        } catch (RuntimeException e) {
            System.err.println("Error in sendNotificationToEveryone: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            System.err.println("Unexpected error in sendNotificationToEveryone: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}