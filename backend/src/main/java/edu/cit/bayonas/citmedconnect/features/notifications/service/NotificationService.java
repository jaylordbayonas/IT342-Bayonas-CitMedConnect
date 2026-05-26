package edu.cit.bayonas.citmedconnect.features.notifications.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import edu.cit.bayonas.citmedconnect.features.notifications.dto.NotificationDTO;
import edu.cit.bayonas.citmedconnect.features.notifications.entity.NotificationEntity;
import edu.cit.bayonas.citmedconnect.features.notifications.mapper.NotificationMapper;
import edu.cit.bayonas.citmedconnect.features.notifications.repository.NotificationRepository;
import edu.cit.bayonas.citmedconnect.features.auth.entity.UserEntity;
import edu.cit.bayonas.citmedconnect.features.auth.repository.UserRepository;

@Service
public class NotificationService {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private NotificationMapper mapper;
    
    public NotificationDTO createNotification(NotificationDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Notification data cannot be null");
        }
        
        if (dto.getNotificationType() == null || dto.getNotificationType().isEmpty()) {
            throw new IllegalArgumentException("Notification type cannot be null or empty");
        }
        
        if (dto.getSchoolId() == null || dto.getSchoolId().isEmpty()) {
            throw new IllegalArgumentException("School ID cannot be null or empty");
        }
        
        // Validate user exists
        String schoolId = dto.getSchoolId();
        userRepository.findById(schoolId)
            .orElseThrow(() -> new RuntimeException("User not found with school_id: " + schoolId));
        
        // Create entity from DTO
        NotificationEntity entity = mapper.toEntity(dto);
        entity.setNotificationId(UUID.randomUUID().toString());
        entity.setCreatedAt(LocalDateTime.now());
        
        if (entity.getIsGlobal() == null) {
            entity.setIsGlobal(false);
        }
        if (entity.getIsRead() == null) {
            entity.setIsRead(false);
        }
        
        NotificationEntity saved = notificationRepository.save(entity);
        return mapper.toDTO(saved);
    }
    
    public List<NotificationDTO> getAllNotifications() {
        List<NotificationEntity> entities = notificationRepository.findAll();
        return mapper.toDTOList(entities);
    }
    
    public NotificationDTO getNotificationById(String id) {
        NotificationEntity entity = notificationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Notification not found with id: " + id));
        return mapper.toDTO(entity);
    }
    
    public List<NotificationDTO> getNotificationsByUserId(String schoolId) {
        List<NotificationEntity> entities = notificationRepository.findBySchoolId(schoolId);
        return mapper.toDTOList(entities);
    }
    
    public NotificationDTO updateNotification(String id, NotificationDTO dto) {
        NotificationEntity existing = notificationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Notification not found with id: " + id));
        
        // Validate user if schoolId is being updated
        if (dto.getSchoolId() != null && !dto.getSchoolId().isEmpty()) {
            String schoolId = dto.getSchoolId();
            userRepository.findById(schoolId)
                .orElseThrow(() -> new RuntimeException("User not found with school_id: " + schoolId));
        }
        
        mapper.updateEntityFromDTO(dto, existing);
        NotificationEntity updated = notificationRepository.save(existing);
        return mapper.toDTO(updated);
    }
    
    public boolean deleteNotification(String id) {
        if (notificationRepository.existsById(id)) {
            notificationRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    private String getCurrentUserSchoolId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getName() != null) {
            String username = authentication.getName();
            try {
                UserEntity user = userRepository.findByEmail(username);
                if (user != null) {
                    return user.getSchoolId();
                }
                user = userRepository.findBySchoolId(username);
                if (user != null) {
                    return user.getSchoolId();
                }
                user = userRepository.findById(username).orElse(null);
                if (user != null) {
                    return user.getSchoolId();
                }
            } catch (Exception e) {
                System.err.println("Error finding current user: " + e.getMessage());
            }
        }
        return null;
    }
    
    public NotificationDTO markNotificationAsRead(String id) {
        NotificationEntity entity = notificationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Notification not found with id: " + id));
        entity.setIsRead(true);
        NotificationEntity updated = notificationRepository.save(entity);
        return mapper.toDTO(updated);
    }
    
    public List<NotificationDTO> sendNotificationToAllStudents(String title, String message, String type) {
        List<UserEntity> students = userRepository.findByRole("STUDENT");
        
        if (students == null || students.isEmpty()) {
            throw new RuntimeException("No students found in the system. Cannot send broadcast notification.");
        }
        
        List<NotificationEntity> notifications = new ArrayList<>();
        String currentUserSchoolId = getCurrentUserSchoolId();
        
        System.out.println("DEBUG: Current user schoolId: " + currentUserSchoolId);
        System.out.println("DEBUG: Total students found: " + students.size());
        System.out.println("DEBUG: Notification type: " + type);
        
        for (UserEntity student : students) {
            if (currentUserSchoolId == null || !student.getSchoolId().equals(currentUserSchoolId)) {
                NotificationEntity notification = new NotificationEntity();
                notification.setNotificationId(UUID.randomUUID().toString());
                notification.setSchoolId(student.getSchoolId());
                notification.setTitle(title);
                notification.setMessage(message);
                notification.setNotificationType(type);
                notification.setIsGlobal(false);
                notification.setCreatedAt(LocalDateTime.now());
                
                notifications.add(notificationRepository.save(notification));
                System.out.println("DEBUG: Sent notification to: " + student.getSchoolId() + " with type: " + type);
            } else {
                System.out.println("DEBUG: SKIPPED notification to: " + student.getSchoolId() + " (current user)");
            }
        }
        
        System.out.println("DEBUG: Total notifications sent: " + notifications.size());
        return mapper.toDTOList(notifications);
    }
    
    public List<NotificationDTO> sendNotificationToEveryone(String title, String message, String type) {
        List<UserEntity> allUsers = userRepository.findAll();
        
        if (allUsers == null || allUsers.isEmpty()) {
            throw new RuntimeException("No users found in the system. Cannot send broadcast notification.");
        }
        
        List<NotificationEntity> notifications = new ArrayList<>();
        String currentUserSchoolId = getCurrentUserSchoolId();
        
        System.out.println("DEBUG: Current user schoolId: " + currentUserSchoolId);
        System.out.println("DEBUG: Total users found: " + allUsers.size());
        System.out.println("DEBUG: Notification type: " + type);
        
        for (UserEntity user : allUsers) {
            if (currentUserSchoolId == null || !user.getSchoolId().equals(currentUserSchoolId)) {
                NotificationEntity notification = new NotificationEntity();
                notification.setNotificationId(UUID.randomUUID().toString());
                notification.setSchoolId(user.getSchoolId());
                notification.setTitle(title);
                notification.setMessage(message);
                notification.setNotificationType(type);
                notification.setIsGlobal(true);
                notification.setCreatedAt(LocalDateTime.now());
                
                notifications.add(notificationRepository.save(notification));
                System.out.println("DEBUG: Sent notification to: " + user.getSchoolId() + " with type: " + type);
            } else {
                System.out.println("DEBUG: SKIPPED notification to: " + user.getSchoolId() + " (current user)");
            }
        }
        
        System.out.println("DEBUG: Total notifications sent: " + notifications.size());
        return mapper.toDTOList(notifications);
    }
    
    @SuppressWarnings("java:S1172")
    public List<NotificationDTO> getNotificationsForUser(String schoolId, String userRole) {
        // Broadcasts are already saved as per-user records (each recipient has their own row
        // with their schoolId), so a single findBySchoolId query is sufficient.
        // Querying by broadcast type on top of this caused every broadcast to appear twice.
        List<NotificationEntity> userNotifications = notificationRepository.findBySchoolId(schoolId);

        List<NotificationEntity> sortedNotifications = userNotifications.stream()
            .sorted((n1, n2) -> n2.getCreatedAt().compareTo(n1.getCreatedAt()))
            .collect(Collectors.toList());

        return mapper.toDTOList(sortedNotifications);
    }
    
    public NotificationDTO sendNotificationToUser(String recipientId, String title, String message, String type) {
        userRepository.findById(recipientId)
            .orElseThrow(() -> new RuntimeException("User not found with school_id: " + recipientId));
        
        NotificationEntity notification = new NotificationEntity();
        notification.setNotificationId(UUID.randomUUID().toString());
        notification.setSchoolId(recipientId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setNotificationType(type);
        notification.setIsGlobal(false);
        notification.setCreatedAt(LocalDateTime.now());
        
        NotificationEntity saved = notificationRepository.save(notification);
        System.out.println("Notification sent to user: " + recipientId + " with type: " + type);
        
        return mapper.toDTO(saved);
    }
    
    public List<NotificationDTO> sendNotificationToAllStaff(String title, String message, String type) {
        List<UserEntity> staffUsers = userRepository.findByRole("STAFF");
        List<UserEntity> adminUsers = userRepository.findByRole("ADMIN");
        
        List<UserEntity> allStaff = new ArrayList<>();
        if (staffUsers != null) {
            allStaff.addAll(staffUsers);
        }
        if (adminUsers != null) {
            allStaff.addAll(adminUsers);
        }
        
        if (allStaff.isEmpty()) {
            throw new RuntimeException("No staff or admin users found in the system. Cannot send broadcast notification.");
        }
        
        List<NotificationEntity> notifications = new ArrayList<>();
        String currentUserSchoolId = getCurrentUserSchoolId();
        
        System.out.println("DEBUG: Current user schoolId: " + currentUserSchoolId);
        System.out.println("DEBUG: Total staff/admin found: " + allStaff.size());
        System.out.println("DEBUG: Notification type: " + type);
        
        for (UserEntity staff : allStaff) {
            // DON'T send to yourself!
            if (currentUserSchoolId == null || !staff.getSchoolId().equals(currentUserSchoolId)) {
                NotificationEntity notification = new NotificationEntity();
                notification.setNotificationId(UUID.randomUUID().toString());
                notification.setSchoolId(staff.getSchoolId());
                notification.setTitle(title);
                notification.setMessage(message);
                notification.setNotificationType(type);
                notification.setIsGlobal(false);
                notification.setCreatedAt(LocalDateTime.now());
                
                notifications.add(notificationRepository.save(notification));
                System.out.println("DEBUG: Sent notification to: " + staff.getSchoolId() + " with type: " + type);
            } else {
                System.out.println("DEBUG: SKIPPED notification to: " + staff.getSchoolId() + " (current user)");
            }
        }
        
        System.out.println("DEBUG: Total notifications sent: " + notifications.size());
        return mapper.toDTOList(notifications);
    }
    
    public long getUnreadNotificationCount(String schoolId) {
        return notificationRepository.countUnreadBySchoolId(schoolId);
    }
    
    public List<NotificationDTO> getAllNotificationsForUser(String schoolId) {
        List<NotificationEntity> entities = notificationRepository.findAllNotificationsForUser(schoolId);
        return mapper.toDTOList(entities);
    }
}