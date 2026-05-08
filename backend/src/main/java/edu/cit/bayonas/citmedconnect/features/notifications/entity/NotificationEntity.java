package edu.cit.bayonas.citmedconnect.features.notifications.entity;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.PrePersist;

@Entity
@Table(name = "notification")
public class NotificationEntity {
    @Id
    @Column(name = "Notification_Id")
    private String notificationId;
    
    @Column(name = "School_Id", nullable = false)
    private String schoolId;
    
    @Column(nullable = false)
    private String title;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;
    
    @Column(name = "Notification_Type", nullable = false)
    private String notificationType;
    
    @Column(name = "is_global")
    private Boolean isGlobal = false;
    
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;
    
    @Column(name = "Created_At", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Constructors
    public NotificationEntity() {}
    
    public NotificationEntity(String schoolId, String title, String message, String notificationType) {
        this.schoolId = schoolId;
        this.title = title;
        this.message = message;
        this.notificationType = notificationType;
        this.isGlobal = false;
        this.isRead = false;
    }

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.isGlobal == null) {
            this.isGlobal = false;
        }
        if (this.isRead == null) {
            this.isRead = false;
        }
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
        return "NotificationEntity{" +
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