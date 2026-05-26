package edu.cit.bayonas.citmedconnect.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import edu.cit.bayonas.citmedconnect.R
import edu.cit.bayonas.citmedconnect.data.SessionManager
import edu.cit.bayonas.citmedconnect.data.api.RetrofitClient
import edu.cit.bayonas.citmedconnect.ui.viewmodel.NotificationViewModel
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var notifViewModel: NotificationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = getColor(R.color.primary_red)
        setContentView(R.layout.activity_profile)

        notifViewModel = ViewModelProvider(this)[NotificationViewModel::class.java]

        populateFromSession()
        setupNavigation()
        setupLogout()
        fetchFullProfile()
        observeNotifBadge()
    }

    private fun populateFromSession() {
        val name     = SessionManager.userName(this) ?: "User"
        val schoolId = SessionManager.schoolId(this) ?: "—"
        val role     = SessionManager.role(this) ?: "student"
        val email    = SessionManager.email(this) ?: "—"

        val initials = name.split(" ")
            .mapNotNull { it.firstOrNull()?.uppercaseChar()?.toString() }
            .take(2).joinToString("")
            .ifEmpty { name.take(2).uppercase() }

        findViewById<TextView>(R.id.avatarInitials).text  = initials
        findViewById<TextView>(R.id.profileName).text     = name
        findViewById<TextView>(R.id.profileSchoolId).text = schoolId
        findViewById<TextView>(R.id.profileStudentId).text = schoolId
        if (email.isNotBlank() && email != "—") {
            findViewById<TextView>(R.id.profileEmail).text = email
        }

        val displayRole = when (role.lowercase()) {
            "admin"  -> "Admin"
            "staff"  -> "Staff"
            else     -> "Student"
        }
        findViewById<TextView>(R.id.profileRole).text      = displayRole
        val roleBadge = findViewById<TextView>(R.id.profileRoleBadge)
        roleBadge.text = displayRole
        if (role.lowercase() == "admin" || role.lowercase() == "staff") {
            roleBadge.setBackgroundResource(R.drawable.badge_completed_bg)
            roleBadge.setTextColor(0xFF4CAF50.toInt())
        }
    }

    private fun fetchFullProfile() {
        val schoolId = SessionManager.schoolId(this) ?: return
        lifecycleScope.launch {
            runCatching { RetrofitClient.authService.getUserProfile(schoolId) }
                .onSuccess { profile ->
                    val fullName = profile.fullName.ifBlank { SessionManager.userName(this@ProfileActivity) ?: "User" }
                    val initials = profile.initials.ifEmpty {
                        fullName.split(" ").mapNotNull { it.firstOrNull()?.uppercaseChar()?.toString() }.take(2).joinToString("")
                    }

                    findViewById<TextView>(R.id.avatarInitials).text   = initials
                    findViewById<TextView>(R.id.profileName).text      = fullName
                    findViewById<TextView>(R.id.profileEmail).text     = profile.email ?: SessionManager.email(this@ProfileActivity) ?: "—"
                    findViewById<TextView>(R.id.profilePhone).text     = profile.displayPhone
                    findViewById<TextView>(R.id.profileGender).text    = profile.displayGender
                    findViewById<TextView>(R.id.profileAge).text       = profile.displayAge

                    val displayRole = profile.displayRole
                    findViewById<TextView>(R.id.profileRole).text      = displayRole
                    val roleBadge = findViewById<TextView>(R.id.profileRoleBadge)
                    roleBadge.text = displayRole
                    if (displayRole == "Admin" || displayRole == "Staff") {
                        roleBadge.setBackgroundResource(R.drawable.badge_completed_bg)
                        roleBadge.setTextColor(0xFF4CAF50.toInt())
                    }
                }
                .onFailure { /* session data already displayed, fail silently */ }
        }
    }

    private fun setupLogout() {
        findViewById<LinearLayout>(R.id.logoutBtn).setOnClickListener {
            SessionManager.clear(this)
            RetrofitClient.authToken = null
            RetrofitClient.userId    = null
            RetrofitClient.userRole  = null
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }
    }

    private fun setupNavigation() {
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }
        val isAdmin = SessionManager.isAdmin(this)
        findViewById<LinearLayout>(R.id.navAppointments).setOnClickListener {
            startActivity(Intent(this, if (isAdmin) AdminAppointmentsActivity::class.java else AppointmentsActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }
        findViewById<LinearLayout>(R.id.navMedicalRecords).setOnClickListener {
            startActivity(Intent(this, MedicalRecordsActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }
        findViewById<LinearLayout>(R.id.navNotifications).setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }
        findViewById<LinearLayout>(R.id.navProfile).setOnClickListener { }
    }

    private fun observeNotifBadge() {
        val schoolId = SessionManager.schoolId(this) ?: return
        notifViewModel.fetchUnreadCount(schoolId)
        notifViewModel.unreadCount.observe(this) { count ->
            val badge = findViewById<View>(R.id.navNotifBadge)
            badge?.visibility = if (count > 0) View.VISIBLE else View.GONE
        }
    }
}
