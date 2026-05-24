package edu.cit.bayonas.citmedconnect.ui

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import edu.cit.bayonas.citmedconnect.R
import edu.cit.bayonas.citmedconnect.ui.viewmodel.AuthViewModel

class LoginActivity : AppCompatActivity() {
    private lateinit var viewModel: AuthViewModel
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button
    private lateinit var registerLink: TextView
    private lateinit var loadingProgress: ProgressBar
    private lateinit var errorMessage: TextView
    private lateinit var togglePassword: ImageButton
    private var passwordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = getColor(R.color.status_bar_auth)
        setContentView(R.layout.activity_login)

        viewModel = ViewModelProvider(this).get(AuthViewModel::class.java)

        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        loginButton = findViewById(R.id.loginButton)
        registerLink = findViewById(R.id.registerLink)
        loadingProgress = findViewById(R.id.loadingProgress)
        errorMessage = findViewById(R.id.errorMessage)
        togglePassword = findViewById(R.id.togglePassword)

        togglePassword.setOnClickListener {
            passwordVisible = !passwordVisible
            if (passwordVisible) {
                passwordInput.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                togglePassword.setImageResource(R.drawable.ic_eye)
            } else {
                passwordInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                togglePassword.setImageResource(R.drawable.ic_eye_off)
            }
            passwordInput.setSelection(passwordInput.text.length)
        }

        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()
            viewModel.login(email, password)
        }

        registerLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }

        findViewById<LinearLayout>(R.id.googleLogin).setOnClickListener {
            Toast.makeText(this, "Google sign-in coming soon", Toast.LENGTH_SHORT).show()
        }

        findViewById<LinearLayout>(R.id.githubLogin).setOnClickListener {
            Toast.makeText(this, "GitHub sign-in coming soon", Toast.LENGTH_SHORT).show()
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            loadingProgress.visibility = if (isLoading) View.VISIBLE else View.GONE
            loginButton.isEnabled = !isLoading
        }

        viewModel.loginResult.observe(this) { result ->
            if (result.success) {
                Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                val intent = Intent(this, DashboardActivity::class.java)
                intent.putExtra("USER_NAME", result.user?.name ?: "User")
                startActivity(intent)
                finish()
            } else {
                errorMessage.text = result.message
                errorMessage.visibility = View.VISIBLE
            }
        }
    }
}
