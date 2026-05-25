package edu.cit.bayonas.citmedconnect.data.model

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val token: String?,
    val user: UserData?
)

data class RegisterRequest(
    val schoolId: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val gender: String,
    val age: Int,
    val password: String,
    val role: String = "student"
)

data class RegisterResponse(
    val success: Boolean,
    val message: String,
    val token: String?,
    val user: UserData?
)

data class UserData(
    val id: String,
    val name: String,
    val email: String,
    val schoolId: String? = null,
    val role: String? = null
)
