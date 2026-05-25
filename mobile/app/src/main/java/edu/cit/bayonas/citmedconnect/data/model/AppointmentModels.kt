package edu.cit.bayonas.citmedconnect.data.model

import com.google.gson.annotations.SerializedName

data class AppointmentResponse(
    val appointmentId: Long? = null,
    val user: AppointmentUser? = null,
    val timeSlot: TimeSlotData? = null,
    val status: String? = null,
    val reason: String? = null,
    val notes: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    val displayStatus: String get() = when (status?.lowercase()) {
        "scheduled", "confirmed", "pending" -> "Scheduled"
        "completed" -> "Completed"
        "success"   -> "Success"
        "cancelled" -> "Cancelled"
        else        -> status?.replaceFirstChar { it.uppercase() } ?: "Unknown"
    }

    val displayDateTime: String get() {
        val time = timeSlot?.startTime?.take(5) ?: ""
        val date = timeSlot?.slotDate ?: ""
        return if (time.isNotEmpty() && date.isNotEmpty()) "$time · $date" else date
    }
}

data class AppointmentUser(
    val schoolId: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null
) {
    val fullName: String get() = listOfNotNull(firstName, lastName).joinToString(" ")
}

data class TimeSlotData(
    val timeSlotId: Long? = null,
    val slotDate: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    @SerializedName("available") val isAvailable: Boolean? = null,
    val maxBookings: Int? = null,
    val currentBookings: Int? = null,
    val staffId: String? = null,
    val location: String? = "Main Clinic"
) {
    val displayTime: String get() = startTime?.take(5) ?: ""
    val displayLocation: String get() = location ?: "Main Clinic"
    val isBookable: Boolean get() =
        (isAvailable == true) && ((currentBookings ?: 0) < (maxBookings ?: 1))
    val bookedStatus: String get() =
        if ((currentBookings ?: 0) >= (maxBookings ?: 1)) "Fully Booked" else "Available"
}

data class BookAppointmentRequest(
    val studentId: String,
    val reason: String,
    val notes: String = ""
)

data class CreateTimeSlotRequest(
    val slotDate: String,
    val startTime: String,
    val endTime: String,
    val maxBookings: Int = 1,
    val currentBookings: Int = 0,
    val staffId: String = "",
    val isAvailable: Boolean = true
)
