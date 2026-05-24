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

    suspend fun register(
        schoolId: String,
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
        gender: String,
        age: Int,
        password: String,
        role: String
    ): RegisterResponse {
        return authService.register(
            RegisterRequest(schoolId, firstName, lastName, email, phone, gender, age, password, role)
        )
    }
}
