package edu.cit.bayonas.citmedconnect.data.repository

import edu.cit.bayonas.citmedconnect.data.api.RetrofitClient
import edu.cit.bayonas.citmedconnect.data.model.AppointmentResponse
import edu.cit.bayonas.citmedconnect.data.model.BookAppointmentRequest
import edu.cit.bayonas.citmedconnect.data.model.CreateTimeSlotRequest
import edu.cit.bayonas.citmedconnect.data.model.TimeSlotData

class AppointmentRepository {
    private val service = RetrofitClient.appointmentService

    suspend fun getMyAppointments(): List<AppointmentResponse> =
        service.getMyAppointments()

    suspend fun getAllAppointments(): List<AppointmentResponse> =
        service.getAllAppointments()

    suspend fun getAvailableTimeSlots(): List<TimeSlotData> =
        service.getAvailableTimeSlots()

    suspend fun getAllTimeSlots(): List<TimeSlotData> =
        service.getAllTimeSlots()

    suspend fun bookAppointment(slotId: Long, studentId: String, reason: String, notes: String): AppointmentResponse =
        service.bookAppointment(slotId, BookAppointmentRequest(studentId, reason, notes))

    suspend fun cancelAppointment(id: Long): AppointmentResponse =
        service.cancelAppointment(id)

    suspend fun completeAppointment(id: Long): AppointmentResponse =
        service.completeAppointment(id)

    suspend fun createTimeSlot(request: CreateTimeSlotRequest): TimeSlotData =
        service.createTimeSlot(request)

    suspend fun deleteTimeSlot(slotId: Long) =
        service.deleteTimeSlot(slotId)
}
