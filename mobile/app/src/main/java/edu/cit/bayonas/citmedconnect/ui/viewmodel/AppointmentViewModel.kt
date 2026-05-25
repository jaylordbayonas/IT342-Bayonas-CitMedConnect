package edu.cit.bayonas.citmedconnect.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.cit.bayonas.citmedconnect.data.model.AppointmentResponse
import edu.cit.bayonas.citmedconnect.data.model.CreateTimeSlotRequest
import edu.cit.bayonas.citmedconnect.data.model.TimeSlotData
import edu.cit.bayonas.citmedconnect.data.repository.AppointmentRepository
import kotlinx.coroutines.launch

class AppointmentViewModel : ViewModel() {

    private val repo = AppointmentRepository()

    // No initial value — observers only fire when real data arrives, preventing empty-state flicker
    private val _appointments = MutableLiveData<List<AppointmentResponse>>()
    val appointments: LiveData<List<AppointmentResponse>> = _appointments

    private val _slots = MutableLiveData<List<TimeSlotData>>()
    val slots: LiveData<List<TimeSlotData>> = _slots

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _actionSuccess = MutableLiveData<String?>(null)
    val actionSuccess: LiveData<String?> = _actionSuccess

    fun fetchMyAppointments() {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            runCatching { repo.getMyAppointments() }
                .onSuccess { _appointments.value = it.sortedByDescending { a -> a.appointmentId ?: 0L } }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun fetchAllAppointments() {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            runCatching { repo.getAllAppointments() }
                .onSuccess { _appointments.value = it.sortedByDescending { a -> a.appointmentId ?: 0L } }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun fetchAvailableSlots() {
        viewModelScope.launch {
            runCatching { repo.getAllTimeSlots() }
                .onSuccess { _slots.value = it.sortedByDescending { s -> s.timeSlotId ?: 0L } }
                .onFailure { _slots.value = emptyList() }
        }
    }

    fun fetchAllSlots() {
        viewModelScope.launch {
            runCatching { repo.getAllTimeSlots() }
                .onSuccess { _slots.value = it.sortedByDescending { s -> s.timeSlotId ?: 0L } }
                .onFailure { _slots.value = emptyList() }
        }
    }

    fun bookAppointment(slotId: Long, studentId: String, reason: String, notes: String = "") {
        _isLoading.value = true
        viewModelScope.launch {
            runCatching { repo.bookAppointment(slotId, studentId, reason, notes) }
                .onSuccess {
                    _actionSuccess.value = "Appointment booked successfully"
                    fetchMyAppointments()
                }
                .onFailure { _error.value = "Failed to book: ${it.message}" }
            _isLoading.value = false
        }
    }

    fun cancelAppointment(id: Long, isAdmin: Boolean = false) {
        viewModelScope.launch {
            runCatching { repo.cancelAppointment(id) }
                .onSuccess {
                    _actionSuccess.value = "Appointment cancelled"
                    if (isAdmin) fetchAllAppointments() else fetchMyAppointments()
                }
                .onFailure { _error.value = "Failed to cancel: ${it.message}" }
        }
    }

    fun completeAppointment(id: Long, isAdmin: Boolean) {
        viewModelScope.launch {
            runCatching { repo.completeAppointment(id) }
                .onSuccess {
                    _actionSuccess.value = "Appointment marked as complete"
                    if (isAdmin) fetchAllAppointments() else fetchMyAppointments()
                }
                .onFailure { _error.value = "Failed to update: ${it.message}" }
        }
    }

    fun createTimeSlot(date: String, startTime: String, endTime: String, maxBookings: Int, staffId: String) {
        viewModelScope.launch {
            runCatching {
                repo.createTimeSlot(
                    CreateTimeSlotRequest(
                        slotDate    = date,
                        startTime   = if (startTime.length == 5) "$startTime:00" else startTime,
                        endTime     = if (endTime.length == 5) "$endTime:00" else endTime,
                        maxBookings = maxBookings,
                        staffId     = staffId,
                        isAvailable = true
                    )
                )
            }
                .onSuccess {
                    _actionSuccess.value = "Time slot created"
                    fetchAllSlots()
                }
                .onFailure { _error.value = "Failed to create slot: ${it.message}" }
        }
    }

    fun deleteTimeSlot(slotId: Long) {
        viewModelScope.launch {
            runCatching { repo.deleteTimeSlot(slotId) }
                .onSuccess {
                    _actionSuccess.value = "Time slot deleted"
                    fetchAllSlots()
                }
                .onFailure { _error.value = "Failed to delete slot: ${it.message}" }
        }
    }

    fun clearError()         { _error.value = null }
    fun clearActionSuccess() { _actionSuccess.value = null }
}
