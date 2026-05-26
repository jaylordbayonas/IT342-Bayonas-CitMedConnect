package edu.cit.bayonas.citmedconnect.data.repository

import edu.cit.bayonas.citmedconnect.data.api.RetrofitClient
import edu.cit.bayonas.citmedconnect.data.model.BroadcastNotificationRequest
import edu.cit.bayonas.citmedconnect.data.model.NotificationResponse
import edu.cit.bayonas.citmedconnect.data.model.UnreadCountResponse

class NotificationRepository {
    private val service = RetrofitClient.notificationService

    suspend fun getNotifications(schoolId: String, role: String): List<NotificationResponse> =
        service.getNotificationsForUser(schoolId, role)

    suspend fun getUnreadCount(schoolId: String): UnreadCountResponse =
        service.getUnreadCount(schoolId)

    suspend fun markAsRead(id: String): NotificationResponse =
        service.markAsRead(id)

    suspend fun delete(id: String) =
        service.deleteNotification(id)

    suspend fun broadcastToStudents(title: String, message: String, type: String): List<NotificationResponse> =
        service.broadcastToStudents(BroadcastNotificationRequest(title, message, type))

    suspend fun broadcastToStaff(title: String, message: String, type: String): List<NotificationResponse> =
        service.broadcastToStaff(BroadcastNotificationRequest(title, message, type))

    suspend fun broadcastToAll(title: String, message: String, type: String): List<NotificationResponse> =
        service.broadcastToAll(BroadcastNotificationRequest(title, message, type))
}
