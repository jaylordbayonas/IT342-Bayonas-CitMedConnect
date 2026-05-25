package edu.cit.bayonas.citmedconnect.data.api

import edu.cit.bayonas.citmedconnect.data.model.AppointmentResponse
import edu.cit.bayonas.citmedconnect.data.model.BookAppointmentRequest
import edu.cit.bayonas.citmedconnect.data.model.CreateTimeSlotRequest
import edu.cit.bayonas.citmedconnect.data.model.TimeSlotData
import retrofit2.http.*

interface AppointmentService {

    @GET("api/appointments/student/my-appointments")
    suspend fun getMyAppointments(): List<AppointmentResponse>

    @GET("api/appointments/staff/all")
    suspend fun getAllAppointments(): List<AppointmentResponse>

    @POST("api/timeslots/{slotId}/book")
    suspend fun bookAppointment(
        @Path("slotId") slotId: Long,
        @Body request: BookAppointmentRequest
    ): AppointmentResponse

    @PUT("api/appointments/{id}/cancel")
    suspend fun cancelAppointment(@Path("id") id: Long): AppointmentResponse

    @PUT("api/appointments/{id}/complete")
    suspend fun completeAppointment(@Path("id") id: Long): AppointmentResponse

    @GET("api/timeslots")
    suspend fun getAllTimeSlots(): List<TimeSlotData>

    @GET("api/timeslots/available")
    suspend fun getAvailableTimeSlots(): List<TimeSlotData>

    @POST("api/timeslots")
    suspend fun createTimeSlot(@Body request: CreateTimeSlotRequest): TimeSlotData

    @DELETE("api/timeslots/{slotId}")
    suspend fun deleteTimeSlot(@Path("slotId") slotId: Long)
}
