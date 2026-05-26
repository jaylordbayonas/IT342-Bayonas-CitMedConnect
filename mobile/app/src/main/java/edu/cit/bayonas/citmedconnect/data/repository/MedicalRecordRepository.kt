package edu.cit.bayonas.citmedconnect.data.repository

import edu.cit.bayonas.citmedconnect.data.api.RetrofitClient
import edu.cit.bayonas.citmedconnect.data.model.CreateMedicalRecordRequest
import edu.cit.bayonas.citmedconnect.data.model.MedicalRecordResponse

class MedicalRecordRepository {
    private val service = RetrofitClient.medicalRecordService

    suspend fun getMyRecords(userId: String): List<MedicalRecordResponse> =
        service.getMyMedicalRecords(userId)

    suspend fun getAllRecords(): List<MedicalRecordResponse> =
        service.getAllMedicalRecords()

    suspend fun createRecord(request: CreateMedicalRecordRequest): MedicalRecordResponse =
        service.createMedicalRecord(request)

    suspend fun deleteRecord(id: Long) =
        service.deleteMedicalRecord(id)
}
