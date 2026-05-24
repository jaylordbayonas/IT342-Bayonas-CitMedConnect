package edu.cit.bayonas.citmedconnect.ui

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import edu.cit.bayonas.citmedconnect.R
import edu.cit.bayonas.citmedconnect.ui.viewmodel.AuthViewModel

class RegisterActivity : AppCompatActivity() {

    private lateinit var viewModel: AuthViewModel

    private lateinit var schoolIdInput: EditText
    private lateinit var firstNameInput: EditText
    private lateinit var lastNameInput: EditText
    private lateinit var genderSpinner: Spinner
    private lateinit var ageInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var phoneInput: EditText
    private lateinit var roleSpinner: Spinner
    private lateinit var passwordInput: EditText
    private lateinit var confirmPasswordInput: EditText
    private lateinit var registerButton: Button
    private lateinit var loginLink: TextView
    private lateinit var loadingProgress: ProgressBar
    private lateinit var errorMessage: TextView
    private lateinit var togglePassword: ImageButton
    private lateinit var toggleConfirmPassword: ImageButton

    private var passwordVisible = false
    private var confirmPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = getColor(R.color.status_bar_auth)
        setContentView(R.layout.activity_register)

        viewModel = ViewModelProvider(this).get(AuthViewModel::class.java)

        bindViews()
        setupSpinners()
        setupPasswordToggles()
        setupClickListeners()
        observeViewModel()
    }

    private fun bindViews() {
        schoolIdInput = findViewById(R.id.schoolIdInput)
        firstNameInput = findViewById(R.id.firstNameInput)
        lastNameInput = findViewById(R.id.lastNameInput)
        genderSpinner = findViewById(R.id.genderSpinner)
        ageInput = findViewById(R.id.ageInput)
        emailInput = findViewById(R.id.emailInput)
        phoneInput = findViewById(R.id.phoneInput)
        roleSpinner = findViewById(R.id.roleSpinner)
        passwordInput = findViewById(R.id.passwordInput)
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput)
        registerButton = findViewById(R.id.registerButton)
        loginLink = findViewById(R.id.loginLink)
        loadingProgress = findViewById(R.id.loadingProgress)
        errorMessage = findViewById(R.id.errorMessage)
        togglePassword = findViewById(R.id.togglePassword)
        toggleConfirmPassword = findViewById(R.id.toggleConfirmPassword)
    }

    private fun setupSpinners() {
        val genderAdapter = ArrayAdapter.createFromResource(
            this, R.array.gender_options, android.R.layout.simple_spinner_item
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        genderSpinner.adapter = genderAdapter

        val roleAdapter = ArrayAdapter.createFromResource(
            this, R.array.role_options, android.R.layout.simple_spinner_item
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        roleSpinner.adapter = roleAdapter
    }

    private fun setupPasswordToggles() {
        togglePassword.setOnClickListener {
            passwordVisible = !passwordVisible
            passwordInput.inputType = if (passwordVisible) {
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            togglePassword.setImageResource(if (passwordVisible) R.drawable.ic_eye else R.drawable.ic_eye_off)
            passwordInput.setSelection(passwordInput.text.length)
        }

        toggleConfirmPassword.setOnClickListener {
            confirmPasswordVisible = !confirmPasswordVisible
            confirmPasswordInput.inputType = if (confirmPasswordVisible) {
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            toggleConfirmPassword.setImageResource(if (confirmPasswordVisible) R.drawable.ic_eye else R.drawable.ic_eye_off)
            confirmPasswordInput.setSelection(confirmPasswordInput.text.length)
        }
    }

    private fun setupClickListeners() {
        registerButton.setOnClickListener {
            errorMessage.visibility = View.GONE
            viewModel.register(
                schoolId = schoolIdInput.text.toString(),
                firstName = firstNameInput.text.toString(),
                lastName = lastNameInput.text.toString(),
                email = emailInput.text.toString(),
                phone = phoneInput.text.toString(),
                gender = genderSpinner.selectedItem?.toString() ?: "",
                age = ageInput.text.toString(),
                password = passwordInput.text.toString(),
                confirmPassword = confirmPasswordInput.text.toString(),
                role = roleSpinner.selectedItem?.toString() ?: "Student"
            )
        }

        loginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        findViewById<LinearLayout>(R.id.googleRegister).setOnClickListener {
            Toast.makeText(this, "Google sign-up coming soon", Toast.LENGTH_SHORT).show()
        }

        findViewById<LinearLayout>(R.id.githubRegister).setOnClickListener {
            Toast.makeText(this, "GitHub sign-up coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            loadingProgress.visibility = if (isLoading) View.VISIBLE else View.GONE
            registerButton.isEnabled = !isLoading
        }

        viewModel.registerResult.observe(this) { result ->
            if (result.success) {
                Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            } else {
                errorMessage.text = result.message
                errorMessage.visibility = View.VISIBLE
            }
        }
    }
}
