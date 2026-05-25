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
import edu.cit.bayonas.citmedconnect.data.SessionManager
import edu.cit.bayonas.citmedconnect.data.api.RetrofitClient
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

        emailInput      = findViewById(R.id.emailInput)
        passwordInput   = findViewById(R.id.passwordInput)
        loginButton     = findViewById(R.id.loginButton)
        registerLink    = findViewById(R.id.registerLink)
        loadingProgress = findViewById(R.id.loadingProgress)
        errorMessage    = findViewById(R.id.errorMessage)
        togglePassword  = findViewById(R.id.togglePassword)

        togglePassword.setOnClickListener {
            passwordVisible = !passwordVisible
            passwordInput.inputType = if (passwordVisible)
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            else
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            togglePassword.setImageResource(if (passwordVisible) R.drawable.ic_eye else R.drawable.ic_eye_off)
            passwordInput.setSelection(passwordInput.text.length)
        }

        loginButton.setOnClickListener {
            errorMessage.visibility = View.GONE
            viewModel.login(emailInput.text.toString().trim(), passwordInput.text.toString())
        }

        registerLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            overridePendingTransition(0, 0)
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
        viewModel.isLoading.observe(this) { loading ->
            loadingProgress.visibility = if (loading) View.VISIBLE else View.GONE
            loginButton.isEnabled = !loading
        }

        viewModel.loginResult.observe(this) { result ->
            if (result.success) {
                val user  = result.user
                val token = result.token ?: ""
                val role  = user?.role?.lowercase() ?: "student"

                // Persist session
                SessionManager.save(
                    ctx      = this,
                    token    = token,
                    userId   = user?.id ?: "",
                    userName = user?.name ?: "User",
                    role     = role,
                    schoolId = user?.schoolId ?: "",
                    email    = user?.email ?: ""
                )

                // Seed auth headers for all future API calls
                RetrofitClient.authToken = token
                RetrofitClient.userId    = user?.id ?: ""
                RetrofitClient.userRole  = role

                // Route all roles to DashboardActivity; role-based UI is handled inside
                val intent = Intent(this, DashboardActivity::class.java)
                intent.putExtra("USER_NAME", user?.name ?: "User")
                startActivity(intent)
                overridePendingTransition(0, 0)
                finish()
            } else {
                errorMessage.text = result.message
                errorMessage.visibility = View.VISIBLE
            }
        }
    }
}
