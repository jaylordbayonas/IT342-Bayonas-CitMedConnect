package edu.cit.bayonas.citmedconnect.data.repository

import edu.cit.bayonas.citmedconnect.data.api.RetrofitClient
import edu.cit.bayonas.citmedconnect.data.model.LoginRequest
import edu.cit.bayonas.citmedconnect.data.model.LoginResponse
import edu.cit.bayonas.citmedconnect.data.model.RegisterRequest
import edu.cit.bayonas.citmedconnect.data.model.RegisterResponse

class AuthRepository {
    private val authService = RetrofitClient.authService

    suspend fun login(email: String, password: String): LoginResponse {
        return authService.login(LoginRequest(email, password))
    }

    suspend fun register(name: String, email: String, password: String): RegisterResponse {
        return authService.register(RegisterRequest(name, email, password))
    }
}
