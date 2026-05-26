package edu.cit.bayonas.citmedconnect.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.cit.bayonas.citmedconnect.data.model.NotificationResponse
import edu.cit.bayonas.citmedconnect.data.repository.NotificationRepository
import kotlinx.coroutines.launch

class NotificationViewModel : ViewModel() {

    private val repo = NotificationRepository()

    private val _notifications = MutableLiveData<List<NotificationResponse>>()
    val notifications: LiveData<List<NotificationResponse>> = _notifications

    private val _unreadCount = MutableLiveData(0L)
    val unreadCount: LiveData<Long> = _unreadCount

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _actionSuccess = MutableLiveData<String?>(null)
    val actionSuccess: LiveData<String?> = _actionSuccess

    fun fetchNotifications(schoolId: String, role: String) {
        _isLoading.value = true
        viewModelScope.launch {
            runCatching { repo.getNotifications(schoolId, role.uppercase()) }
                .onSuccess { list ->
                    _notifications.value = list.sortedByDescending { it.createdAt }
                    _unreadCount.value = list.count { !it.read }.toLong()
                }
                .onFailure { _error.value = "Failed to load notifications: ${it.message}" }
            _isLoading.value = false
        }
    }

    fun markAsRead(id: String) {
        viewModelScope.launch {
            runCatching { repo.markAsRead(id) }
                .onSuccess { updated ->
                    _notifications.value = _notifications.value?.map {
                        if (it.notificationId == id) updated else it
                    }
                    _unreadCount.value = (_unreadCount.value ?: 1) - 1
                }
                .onFailure { /* fail silently — UI already updates optimistically */ }
        }
    }

    fun delete(id: String) {
        viewModelScope.launch {
            runCatching { repo.delete(id) }
                .onSuccess {
                    _notifications.value = _notifications.value?.filter { it.notificationId != id }
                    _actionSuccess.value = "Notification deleted"
                }
                .onFailure { _error.value = "Failed to delete: ${it.message}" }
        }
    }

    fun sendBroadcast(audience: String, title: String, message: String, type: String) {
        _isLoading.value = true
        viewModelScope.launch {
            runCatching {
                when (audience) {
                    "students" -> repo.broadcastToStudents(title, message, type)
                    "staff"    -> repo.broadcastToStaff(title, message, type)
                    else       -> repo.broadcastToAll(title, message, type)
                }
            }
                .onSuccess { _actionSuccess.value = "Notification sent successfully" }
                .onFailure { _error.value = "Failed to send: ${it.message}" }
            _isLoading.value = false
        }
    }

    fun fetchUnreadCount(schoolId: String) {
        viewModelScope.launch {
            runCatching { repo.getUnreadCount(schoolId) }
                .onSuccess { _unreadCount.value = it.unreadCount }
                .onFailure { /* fail silently */ }
        }
    }

    fun clearError()         { _error.value = null }
    fun clearActionSuccess() { _actionSuccess.value = null }
}
