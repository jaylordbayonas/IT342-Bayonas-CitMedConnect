package edu.cit.bayonas.citmedconnect.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.cit.bayonas.citmedconnect.data.model.UserData
import edu.cit.bayonas.citmedconnect.data.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _loginResult = MutableLiveData<AuthResult>()
    val loginResult: LiveData<AuthResult> = _loginResult

    private val _registerResult = MutableLiveData<AuthResult>()
    val registerResult: LiveData<AuthResult> = _registerResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun login(email: String, password: String) {
        if (!isValidEmail(email)) {
            _loginResult.value = AuthResult(success = false, message = "Please enter a valid email address")
            return
        }
        if (password.isBlank()) {
            _loginResult.value = AuthResult(success = false, message = "Please enter your password")
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = repository.login(email, password)
                _loginResult.value = if (response.success) {
                    AuthResult(success = true, message = response.message, token = response.token, user = response.user)
                } else {
                    AuthResult(success = false, message = response.message ?: "Login failed")
                }
            } catch (e: Exception) {
                _loginResult.value = AuthResult(success = false, message = "Network error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun register(
        schoolId: String,
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
        gender: String,
        age: String,
        password: String,
        confirmPassword: String
    ) {
        val role = "student"
        val ageInt = age.trim().toIntOrNull()

        when {
            schoolId.isBlank() -> {
                _registerResult.value = AuthResult(success = false, message = "Please enter your Student ID")
                return
            }
            !isValidSchoolId(schoolId.trim()) -> {
                _registerResult.value = AuthResult(success = false, message = "Student ID must follow the format: ##-####-### (e.g. 21-1234-567)")
                return
            }
            firstName.isBlank() -> {
                _registerResult.value = AuthResult(success = false, message = "Please enter your first name")
                return
            }
            lastName.isBlank() -> {
                _registerResult.value = AuthResult(success = false, message = "Please enter your last name")
                return
            }
            gender == "Select Gender" || gender.isBlank() -> {
                _registerResult.value = AuthResult(success = false, message = "Please select your gender")
                return
            }
            ageInt == null || ageInt < 15 || ageInt > 100 -> {
                _registerResult.value = AuthResult(success = false, message = "Please enter a valid age (15–100)")
                return
            }
            !isValidCitEmail(email) -> {
                _registerResult.value = AuthResult(success = false, message = "Email must be in the format: firstname.lastname@cit.edu")
                return
            }
            phone.isBlank() -> {
                _registerResult.value = AuthResult(success = false, message = "Please enter your phone number")
                return
            }
            password.length < 6 -> {
                _registerResult.value = AuthResult(success = false, message = "Password must be at least 6 characters")
                return
            }
            password != confirmPassword -> {
                _registerResult.value = AuthResult(success = false, message = "Passwords do not match")
                return
            }
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = repository.register(
                    schoolId = schoolId.trim(),
                    firstName = firstName.trim(),
                    lastName = lastName.trim(),
                    email = email.trim().lowercase(),
                    phone = phone.trim(),
                    gender = gender,
                    age = ageInt!!,
                    password = password,
                    role = role.lowercase()
                )
                _registerResult.value = if (response.success) {
                    AuthResult(success = true, message = response.message, user = response.user)
                } else {
                    AuthResult(success = false, message = response.message ?: "Registration failed")
                }
            } catch (e: Exception) {
                _registerResult.value = AuthResult(success = false, message = "Network error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidCitEmail(email: String): Boolean {
        return email.trim().matches(Regex("^[a-zA-Z]+\\.[a-zA-Z]+@cit\\.edu$"))
    }

    private fun isValidSchoolId(id: String): Boolean {
        return id.matches(Regex("^\\d{2}-\\d{4}-\\d{3}$"))
    }
}

data class AuthResult(
    val success: Boolean,
    val message: String,
    val token: String? = null,
    val user: UserData? = null
)
