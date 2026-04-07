package edu.cit.bayonas.citmedconnect.service.notification;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Component;

import edu.cit.bayonas.citmedconnect.entity.NotificationEntity;

@Component
public class DefaultNotificationFactory implements NotificationFactory {

    @Override
    public NotificationEntity create(String recipientSchoolId, String title, String message, String type, boolean isGlobal) {
        NotificationEntity notification = new NotificationEntity();
        notification.setNotificationId(UUID.randomUUID().toString());
        notification.setSchoolId(recipientSchoolId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setNotificationType(type);
        notification.setIsGlobal(isGlobal);
        notification.setCreatedAt(LocalDateTime.now());
        return notification;
    }
}
