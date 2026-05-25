package edu.cit.bayonas.citmedconnect.data.api

import edu.cit.bayonas.citmedconnect.data.model.LoginRequest
import edu.cit.bayonas.citmedconnect.data.model.LoginResponse
import edu.cit.bayonas.citmedconnect.data.model.RegisterRequest
import edu.cit.bayonas.citmedconnect.data.model.RegisterResponse
import edu.cit.bayonas.citmedconnect.data.model.UserProfileData
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AuthService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse

    @GET("api/auth/users/{schoolId}")
    suspend fun getUserProfile(@Path("schoolId") schoolId: String): UserProfileData
}
