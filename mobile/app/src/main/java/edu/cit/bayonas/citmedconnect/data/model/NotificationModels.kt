package edu.cit.bayonas.citmedconnect.data.model

import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

data class NotificationResponse(
    val notificationId: String? = null,
    val schoolId: String? = null,
    val title: String? = null,
    val message: String? = null,
    val notificationType: String? = null,
    @SerializedName("isGlobal") val isGlobal: Boolean? = false,
    @SerializedName("isRead") val isRead: Boolean? = false,
    val createdAt: String? = null
) {
    val read: Boolean get() = isRead == true

    val displayType: String get() = when (notificationType?.lowercase()) {
        "appointment"                       -> "Appointment"
        "medical_record"                    -> "Medical Record"
        "student_broadcast"                 -> "Announcement"
        "staff_broadcast"                   -> "Announcement"
        "global_broadcast"                  -> "Announcement"
        else                                -> "Info"
    }

    val displayTime: String get() {
        if (createdAt.isNullOrBlank()) return ""
        return try {
            val dt = LocalDateTime.parse(createdAt.take(19),
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
            val now = LocalDateTime.now()
            val mins  = ChronoUnit.MINUTES.between(dt, now)
            val hours = ChronoUnit.HOURS.between(dt, now)
            val days  = ChronoUnit.DAYS.between(dt, now)
            when {
                mins  < 1   -> "Just now"
                mins  < 60  -> "$mins min ago"
                hours < 24  -> "$hours hr ago"
                days  == 1L -> "Yesterday"
                days  < 7   -> "$days days ago"
                else        -> createdAt.take(10)
            }
        } catch (_: Exception) { createdAt.take(10) }
    }
}

data class BroadcastNotificationRequest(
    val title: String,
    val message: String,
    val type: String = "info"
)

data class SendNotificationRequest(
    val recipientId: String,
    val title: String,
    val message: String,
    val type: String = "info"
)

data class UnreadCountResponse(
    val unreadCount: Long = 0
)
