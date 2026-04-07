package edu.cit.bayonas.citmedconnect.service.notification;

import edu.cit.bayonas.citmedconnect.entity.NotificationEntity;

public interface NotificationFactory {
    NotificationEntity create(String recipientSchoolId, String title, String message, String type, boolean isGlobal);
}
