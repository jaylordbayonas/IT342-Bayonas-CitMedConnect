package edu.cit.bayonas.citmedconnect.data.api

import edu.cit.bayonas.citmedconnect.data.model.BroadcastNotificationRequest
import edu.cit.bayonas.citmedconnect.data.model.NotificationResponse
import edu.cit.bayonas.citmedconnect.data.model.UnreadCountResponse
import retrofit2.http.*

interface NotificationService {

    @GET("api/notifications/user/{schoolId}/role/{role}")
    suspend fun getNotificationsForUser(
        @Path("schoolId") schoolId: String,
        @Path("role") role: String
    ): List<NotificationResponse>

    @GET("api/notifications/user/{schoolId}/unread-count")
    suspend fun getUnreadCount(
        @Path("schoolId") schoolId: String
    ): UnreadCountResponse

    @PUT("api/notifications/{id}/read")
    suspend fun markAsRead(
        @Path("id") id: String
    ): NotificationResponse

    @DELETE("api/notifications/{id}")
    suspend fun deleteNotification(
        @Path("id") id: String
    )

    @POST("api/notifications/broadcast/students")
    suspend fun broadcastToStudents(
        @Body request: BroadcastNotificationRequest
    ): List<NotificationResponse>

    @POST("api/notifications/broadcast/staff")
    suspend fun broadcastToStaff(
        @Body request: BroadcastNotificationRequest
    ): List<NotificationResponse>

    @POST("api/notifications/broadcast/all")
    suspend fun broadcastToAll(
        @Body request: BroadcastNotificationRequest
    ): List<NotificationResponse>
}
