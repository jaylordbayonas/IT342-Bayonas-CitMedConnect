package edu.cit.bayonas.citmedconnect.features.notifications.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SendNotificationDTO {
    
    @NotBlank(message = "Recipient ID is required")
    @Size(max = 20, message = "Recipient ID must not exceed 20 characters")
    private String recipientId;
    
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;
    
    @NotBlank(message = "Message is required")
    @Size(max = 2000, message = "Message must not exceed 2000 characters")
    private String message;
    
    @Size(max = 50, message = "Type must not exceed 50 characters")
    private String type = "info";
    
    // Constructors
    public SendNotificationDTO() {}
    
    public SendNotificationDTO(String recipientId, String title, String message, String type) {
        this.recipientId = recipientId;
        this.title = title;
        this.message = message;
        this.type = type != null ? type : "info";
    }
    
    // Getters and Setters
    public String getRecipientId() {
        return recipientId;
    }
    
    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
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
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    @Override
    public String toString() {
        return "SendNotificationDTO{" +
                "recipientId='" + recipientId + '\'' +
                ", title='" + title + '\'' +
                ", message='" + message + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}