package edu.cit.bayonas.citmedconnect.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.cit.bayonas.citmedconnect.data.model.CreateMedicalRecordRequest
import edu.cit.bayonas.citmedconnect.data.model.MedicalRecordResponse
import edu.cit.bayonas.citmedconnect.data.repository.MedicalRecordRepository
import kotlinx.coroutines.launch

class MedicalRecordViewModel : ViewModel() {

    private val repo = MedicalRecordRepository()

    private val _records = MutableLiveData<List<MedicalRecordResponse>>()
    val records: LiveData<List<MedicalRecordResponse>> = _records

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _actionSuccess = MutableLiveData<String?>(null)
    val actionSuccess: LiveData<String?> = _actionSuccess

    fun fetchMyRecords(userId: String) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            runCatching { repo.getMyRecords(userId) }
                .onSuccess { _records.value = it }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun fetchAllRecords() {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            runCatching { repo.getAllRecords() }
                .onSuccess { _records.value = it.sortedByDescending { r -> r.recordId ?: 0L } }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun createRecord(request: CreateMedicalRecordRequest) {
        _isLoading.value = true
        viewModelScope.launch {
            runCatching { repo.createRecord(request) }
                .onSuccess {
                    _actionSuccess.value = "Medical record created successfully"
                    fetchAllRecords()
                }
                .onFailure { _error.value = "Failed to create record: ${it.message}" }
            _isLoading.value = false
        }
    }

    fun deleteRecord(id: Long) {
        viewModelScope.launch {
            runCatching { repo.deleteRecord(id) }
                .onSuccess {
                    _actionSuccess.value = "Record deleted"
                    fetchAllRecords()
                }
                .onFailure { _error.value = "Failed to delete: ${it.message}" }
        }
    }

    fun clearError()         { _error.value = null }
    fun clearActionSuccess() { _actionSuccess.value = null }
}
