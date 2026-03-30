package edu.cit.bayonas.citmedconnect.data.api

import edu.cit.bayonas.citmedconnect.data.model.LoginRequest
import edu.cit.bayonas.citmedconnect.data.model.LoginResponse
import edu.cit.bayonas.citmedconnect.data.model.RegisterRequest
import edu.cit.bayonas.citmedconnect.data.model.RegisterResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse
}
