package edu.cit.bayonas.citmedconnect.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import edu.cit.bayonas.citmedconnect.R
import edu.cit.bayonas.citmedconnect.data.SessionManager
import edu.cit.bayonas.citmedconnect.data.model.AppointmentResponse
import edu.cit.bayonas.citmedconnect.ui.viewmodel.AppointmentViewModel
import edu.cit.bayonas.citmedconnect.ui.viewmodel.NotificationViewModel
import java.util.Calendar

class DashboardActivity : AppCompatActivity() {

    private lateinit var viewModel: AppointmentViewModel
    private lateinit var notifViewModel: NotificationViewModel

    private lateinit var greetingText: TextView
    private lateinit var userNameText: TextView
    private lateinit var heroSubtitleText: TextView
    private lateinit var bookAppointmentBtnText: TextView
    private lateinit var stat1Value: TextView
    private lateinit var stat2Value: TextView
    private lateinit var stat3Value: TextView
    private lateinit var stat4Value: TextView
    private lateinit var stat1Label: TextView
    private lateinit var stat2Label: TextView
    private lateinit var stat3Label: TextView
    private lateinit var stat4Label: TextView
    private lateinit var stat1Trend: TextView
    private lateinit var stat2Trend: TextView
    private lateinit var stat3Trend: TextView
    private lateinit var stat4Trend: TextView
    private lateinit var recentSectionLabel: TextView
    private lateinit var appointmentsContainer: LinearLayout
    private lateinit var emptyState: LinearLayout
    private lateinit var bookAppointmentBtn: LinearLayout
    private lateinit var viewAllAppointments: LinearLayout
    private lateinit var profileBtn: LinearLayout
    private lateinit var calendarBtn: LinearLayout
    private lateinit var navHome: LinearLayout
    private lateinit var navAppointments: LinearLayout
    private lateinit var navMedicalRecords: LinearLayout
    private lateinit var navNotifications: LinearLayout
    private lateinit var navProfile: LinearLayout
    private lateinit var navNotifBadge: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = getColor(R.color.primary_red)
        setContentView(R.layout.activity_dashboard)

        viewModel      = ViewModelProvider(this).get(AppointmentViewModel::class.java)
        notifViewModel = ViewModelProvider(this)[NotificationViewModel::class.java]
        val isAdmin = SessionManager.isAdmin(this)

        bindViews()
        setupGreeting()
        adaptToRole(isAdmin)
        setupNavigation(isAdmin)
        observeViewModel(isAdmin)
        observeNotifBadge()

        if (isAdmin) {
            viewModel.fetchAllAppointments()
            viewModel.fetchAllSlots()
        } else {
            viewModel.fetchMyAppointments()
        }
    }

    private fun bindViews() {
        greetingText           = findViewById(R.id.greetingText)
        userNameText           = findViewById(R.id.userNameText)
        heroSubtitleText       = findViewById(R.id.heroSubtitleText)
        bookAppointmentBtnText = findViewById(R.id.bookAppointmentBtnText)
        stat1Value             = findViewById(R.id.stat1Value)
        stat2Value             = findViewById(R.id.stat2Value)
        stat3Value             = findViewById(R.id.stat3Value)
        stat4Value             = findViewById(R.id.stat4Value)
        stat1Label             = findViewById(R.id.stat1Label)
        stat2Label             = findViewById(R.id.stat2Label)
        stat3Label             = findViewById(R.id.stat3Label)
        stat4Label             = findViewById(R.id.stat4Label)
        stat1Trend             = findViewById(R.id.stat1Trend)
        stat2Trend             = findViewById(R.id.stat2Trend)
        stat3Trend             = findViewById(R.id.stat3Trend)
        stat4Trend             = findViewById(R.id.stat4Trend)
        recentSectionLabel     = findViewById(R.id.recentSectionLabel)
        appointmentsContainer  = findViewById(R.id.appointmentsContainer)
        emptyState             = findViewById(R.id.emptyState)
        bookAppointmentBtn     = findViewById(R.id.bookAppointmentBtn)
        viewAllAppointments    = findViewById(R.id.viewAllAppointments)
        profileBtn             = findViewById(R.id.profileBtn)
        calendarBtn            = findViewById(R.id.calendarBtn)
        navHome                = findViewById(R.id.navHome)
        navAppointments        = findViewById(R.id.navAppointments)
        navMedicalRecords      = findViewById(R.id.navMedicalRecords)
        navNotifications       = findViewById(R.id.navNotifications)
        navProfile             = findViewById(R.id.navProfile)
        navNotifBadge          = findViewById(R.id.navNotifBadge)
    }

    private fun setupGreeting() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        greetingText.text = when {
            hour < 12 -> "Good Morning,"
            hour < 18 -> "Good Afternoon,"
            else      -> "Good Evening,"
        }
        userNameText.text = intent.getStringExtra("USER_NAME")
            ?: SessionManager.userName(this)
            ?: "User"
    }

    private fun adaptToRole(isAdmin: Boolean) {
        if (isAdmin) {
            heroSubtitleText.text       = "Manage student appointments, time slots, and monitor clinic activity."
            bookAppointmentBtnText.text = "Manage Slots"
            stat1Label.text             = "Total Bookings"
            stat2Label.text             = "Pending"
            stat3Label.text             = "Available Slots"
            stat4Label.text             = "Completed"
            recentSectionLabel.text     = "Recent Appointments"
        }
        // Reset stats to 0 while loading
        stat1Value.text = "0"
        stat2Value.text = "0"
        stat3Value.text = "0"
        stat4Value.text = "0"
    }

    private fun setupNavigation(isAdmin: Boolean) {
        navHome.setOnClickListener { }
        navAppointments.setOnClickListener {
            if (isAdmin) {
                startActivity(Intent(this, AdminAppointmentsActivity::class.java))
            } else {
                startActivity(Intent(this, AppointmentsActivity::class.java))
            }
            overridePendingTransition(0, 0)
        }
        navMedicalRecords.setOnClickListener {
            startActivity(Intent(this, MedicalRecordsActivity::class.java))
            overridePendingTransition(0, 0)
        }
        navNotifications.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
            overridePendingTransition(0, 0)
        }
        navProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            overridePendingTransition(0, 0)
        }

        if (isAdmin) {
            bookAppointmentBtn.setOnClickListener {
                startActivity(Intent(this, AdminAppointmentsActivity::class.java))
                overridePendingTransition(0, 0)
            }
            calendarBtn.setOnClickListener {
                startActivity(Intent(this, AdminAppointmentsActivity::class.java))
                overridePendingTransition(0, 0)
            }
            viewAllAppointments.setOnClickListener {
                startActivity(Intent(this, AdminAppointmentsActivity::class.java))
                overridePendingTransition(0, 0)
            }
        } else {
            bookAppointmentBtn.setOnClickListener {
                val intent = Intent(this, AppointmentsActivity::class.java)
                intent.putExtra("OPEN_BOOKING_MODAL", true)
                startActivity(intent)
            }
            calendarBtn.setOnClickListener {
                val intent = Intent(this, AppointmentsActivity::class.java)
                intent.putExtra("OPEN_BOOKING_MODAL", true)
                startActivity(intent)
            }
            viewAllAppointments.setOnClickListener {
                startActivity(Intent(this, AppointmentsActivity::class.java))
            }
        }

        profileBtn.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            overridePendingTransition(0, 0)
        }
    }

    private fun observeViewModel(isAdmin: Boolean) {
        viewModel.appointments.observe(this) { appointments ->
            if (isAdmin) {
                updateAdminStats(appointments)
            } else {
                updateStudentStats(appointments)
            }
            populateRecentAppointments(appointments.take(5), isAdmin)
        }

        if (isAdmin) {
            viewModel.slots.observe(this) { slots ->
                val available = slots.count { it.isBookable }
                stat3Value.text = available.toString()
                stat3Trend.text = if (available == 0) "None available" else "Bookable"
            }
        }
    }

    private fun updateStudentStats(appointments: List<AppointmentResponse>) {
        val total     = appointments.size
        val scheduled = appointments.count { it.displayStatus == "Scheduled" }
        val completed = appointments.count { it.displayStatus == "Completed" }

        stat1Value.text = total.toString()
        stat2Value.text = scheduled.toString()
        stat3Value.text = completed.toString()
        stat4Value.text = "0"
        stat1Trend.text = if (scheduled > 0) "$scheduled upcoming" else "None upcoming"
        stat2Trend.text = "Scheduled"
        stat3Trend.text = "All time"
        stat4Trend.text = "None"
    }

    private fun updateAdminStats(appointments: List<AppointmentResponse>) {
        val total     = appointments.size
        val pending   = appointments.count { it.displayStatus == "Scheduled" }
        val completed = appointments.count { it.displayStatus == "Completed" }

        stat1Value.text = total.toString()
        stat2Value.text = pending.toString()
        stat4Value.text = completed.toString()
        stat1Trend.text = "All students"
        stat2Trend.text = if (pending > 0) "Needs action" else "All clear"
        stat4Trend.text = "Total"
    }

    private fun populateRecentAppointments(appointments: List<AppointmentResponse>, isAdmin: Boolean) {
        appointmentsContainer.removeAllViews()
        if (appointments.isEmpty()) {
            emptyState.visibility = View.VISIBLE
        } else {
            emptyState.visibility = View.GONE
            appointments.forEach { appt ->
                val secondaryLine = if (isAdmin) {
                    appt.user?.fullName ?: "Student"
                } else {
                    appt.timeSlot?.displayLocation ?: "Main Clinic"
                }
                addAppointmentItem(
                    reason   = appt.reason ?: "—",
                    doctor   = secondaryLine,
                    dateTime = appt.displayDateTime,
                    status   = appt.displayStatus
                )
            }
        }
    }

    private fun addAppointmentItem(reason: String, doctor: String, dateTime: String, status: String) {
        val view = LayoutInflater.from(this).inflate(
            R.layout.item_appointment_dashboard, appointmentsContainer, false
        )
        view.findViewById<TextView>(R.id.appointmentReason).text   = reason
        view.findViewById<TextView>(R.id.appointmentDoctor).text   = doctor
        view.findViewById<TextView>(R.id.appointmentDateTime).text = dateTime

        val statusBadge = view.findViewById<TextView>(R.id.appointmentStatus)
        statusBadge.text = status
        val (bgRes, textColor) = when (status.lowercase()) {
            "scheduled" -> Pair(R.drawable.badge_scheduled_bg, 0xFF2196F3.toInt())
            "completed" -> Pair(R.drawable.badge_completed_bg, 0xFF4CAF50.toInt())
            "cancelled" -> Pair(R.drawable.badge_cancelled_bg, 0xFFF44336.toInt())
            else        -> Pair(R.drawable.badge_pending_bg,   0xFFFF8F00.toInt())
        }
        statusBadge.setBackgroundResource(bgRes)
        statusBadge.setTextColor(textColor)
        appointmentsContainer.addView(view)
    }

    private fun observeNotifBadge() {
        val schoolId = SessionManager.schoolId(this) ?: return
        notifViewModel.fetchUnreadCount(schoolId)
        notifViewModel.unreadCount.observe(this) { count ->
            navNotifBadge.visibility = if (count > 0) View.VISIBLE else View.GONE
        }
    }
}
