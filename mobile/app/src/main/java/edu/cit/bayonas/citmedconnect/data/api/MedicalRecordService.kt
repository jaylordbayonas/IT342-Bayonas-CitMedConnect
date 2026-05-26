package edu.cit.bayonas.citmedconnect.data.api

import edu.cit.bayonas.citmedconnect.data.model.CreateMedicalRecordRequest
import edu.cit.bayonas.citmedconnect.data.model.MedicalRecordResponse
import retrofit2.http.*

interface MedicalRecordService {

    @POST("api/medical-records/")
    suspend fun createMedicalRecord(@Body request: CreateMedicalRecordRequest): MedicalRecordResponse

    @GET("api/medical-records/")
    suspend fun getAllMedicalRecords(): List<MedicalRecordResponse>

    @GET("api/medical-records/user/{userId}/sorted")
    suspend fun getMyMedicalRecords(@Path("userId") userId: String): List<MedicalRecordResponse>

    @DELETE("api/medical-records/{id}")
    suspend fun deleteMedicalRecord(@Path("id") id: Long)
}
