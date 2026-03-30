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
            _loginResult.value = AuthResult(success = false, message = "Please enter a valid email")
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
                    AuthResult(success = true, message = response.message, user = response.user)
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

    fun register(name: String, email: String, password: String, confirmPassword: String) {
        when {
            name.isBlank() -> {
                _registerResult.value = AuthResult(success = false, message = "Please enter your name")
                return
            }
            !isValidEmail(email) -> {
                _registerResult.value = AuthResult(success = false, message = "Please enter a valid email")
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
                val response = repository.register(name, email, password)
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
}

data class AuthResult(
    val success: Boolean,
    val message: String,
    val user: UserData? = null
)
