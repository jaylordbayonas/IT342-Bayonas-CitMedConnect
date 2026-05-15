package edu.cit.bayonas.citmedconnect.features.notifications.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public class NotificationDTO {
    
    private String notificationId;
    
    @NotBlank(message = "School ID is required")
    @Size(max = 20, message = "School ID must not exceed 20 characters")
    private String schoolId;
    
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;
    
    @NotBlank(message = "Message is required")
    @Size(max = 2000, message = "Message must not exceed 2000 characters")
    private String message;
    
    @NotBlank(message = "Notification type is required")
    @Size(max = 50, message = "Notification type must not exceed 50 characters")
    private String notificationType;
    
    private Boolean isGlobal = false;
    private Boolean isRead = false;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    // Constructors
    public NotificationDTO() {}
    
    public NotificationDTO(String schoolId, String title, String message, String notificationType) {
        this.schoolId = schoolId;
        this.title = title;
        this.message = message;
        this.notificationType = notificationType;
        this.isGlobal = false;
        this.isRead = false;
    }
    
    // Getters and Setters
    public String getNotificationId() {
        return notificationId;
    }
    
    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }
    
    public String getSchoolId() {
        return schoolId;
    }
    
    public void setSchoolId(String schoolId) {
        this.schoolId = schoolId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getNotificationType() {
        return notificationType;
    }
    
    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }
    
    public Boolean getIsGlobal() {
        return isGlobal;
    }
    
    public void setIsGlobal(Boolean isGlobal) {
        this.isGlobal = isGlobal;
    }
    
    public Boolean getIsRead() {
        return isRead;
    }
    
    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public String toString() {
        return "NotificationDTO{" +
                "notificationId='" + notificationId + '\'' +
                ", schoolId='" + schoolId + '\'' +
                ", title='" + title + '\'' +
                ", message='" + message + '\'' +
                ", notificationType='" + notificationType + '\'' +
                ", isGlobal=" + isGlobal +
                ", isRead=" + isRead +
                ", createdAt=" + createdAt +
                '}';
    }
}