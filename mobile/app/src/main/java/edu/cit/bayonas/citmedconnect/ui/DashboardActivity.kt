package edu.cit.bayonas.citmedconnect.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import edu.cit.bayonas.citmedconnect.R
import java.util.Calendar

class DashboardActivity : AppCompatActivity() {

    private lateinit var greetingText: TextView
    private lateinit var userNameText: TextView
    private lateinit var stat1Value: TextView
    private lateinit var stat2Value: TextView
    private lateinit var stat3Value: TextView
    private lateinit var stat4Value: TextView
    private lateinit var stat1Trend: TextView
    private lateinit var stat2Trend: TextView
    private lateinit var stat4Trend: TextView
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = getColor(R.color.primary_red)
        setContentView(R.layout.activity_dashboard)

        bindViews()
        setupGreeting()
        setupNavigation()
        loadDashboardData()
    }

    private fun bindViews() {
        greetingText = findViewById(R.id.greetingText)
        userNameText = findViewById(R.id.userNameText)
        stat1Value = findViewById(R.id.stat1Value)
        stat2Value = findViewById(R.id.stat2Value)
        stat3Value = findViewById(R.id.stat3Value)
        stat4Value = findViewById(R.id.stat4Value)
        stat1Trend = findViewById(R.id.stat1Trend)
        stat2Trend = findViewById(R.id.stat2Trend)
        stat4Trend = findViewById(R.id.stat4Trend)
        appointmentsContainer = findViewById(R.id.appointmentsContainer)
        emptyState = findViewById(R.id.emptyState)
        bookAppointmentBtn = findViewById(R.id.bookAppointmentBtn)
        viewAllAppointments = findViewById(R.id.viewAllAppointments)
        profileBtn = findViewById(R.id.profileBtn)
        calendarBtn = findViewById(R.id.calendarBtn)
        navHome = findViewById(R.id.navHome)
        navAppointments = findViewById(R.id.navAppointments)
        navMedicalRecords = findViewById(R.id.navMedicalRecords)
        navNotifications = findViewById(R.id.navNotifications)
        navProfile = findViewById(R.id.navProfile)
    }

    private fun setupGreeting() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        greetingText.text = when {
            hour < 12 -> "Good Morning,"
            hour < 18 -> "Good Afternoon,"
            else -> "Good Evening,"
        }
        val userName = intent.getStringExtra("USER_NAME") ?: "User"
        userNameText.text = userName
    }

    private fun setupNavigation() {
        navHome.setOnClickListener { }

        navAppointments.setOnClickListener {
            startActivity(Intent(this, AppointmentsActivity::class.java))
        }

        navMedicalRecords.setOnClickListener {
            Toast.makeText(this, "Medical Records coming soon", Toast.LENGTH_SHORT).show()
        }

        navNotifications.setOnClickListener {
            Toast.makeText(this, "Notifications coming soon", Toast.LENGTH_SHORT).show()
        }

        navProfile.setOnClickListener {
            Toast.makeText(this, "Profile coming soon", Toast.LENGTH_SHORT).show()
        }

        bookAppointmentBtn.setOnClickListener {
            val intent = Intent(this, AppointmentsActivity::class.java)
            intent.putExtra("OPEN_BOOKING_MODAL", true)
            startActivity(intent)
        }

        profileBtn.setOnClickListener {
            Toast.makeText(this, "Profile coming soon", Toast.LENGTH_SHORT).show()
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

    private fun loadDashboardData() {
        val sampleAppointments = listOf(
            AppointmentItem("General Consultation", "Dr. Santos", "May 26, 2026 at 12:41", "Scheduled"),
            AppointmentItem("Medical Check-up", "Dr. Santos", "May 28, 2026 at 10:00", "Pending")
        )

        val total = sampleAppointments.size
        val upcoming = sampleAppointments.count { it.status.equals("Scheduled", ignoreCase = true) || it.status.equals("Pending", ignoreCase = true) }
        val completed = 3

        stat1Value.text = total.toString()
        stat2Value.text = upcoming.toString()
        stat3Value.text = completed.toString()
        stat4Value.text = "0"
        stat1Trend.text = "$upcoming upcoming"
        stat2Trend.text = "This week"
        stat4Trend.text = "None"

        if (sampleAppointments.isEmpty()) {
            emptyState.visibility = View.VISIBLE
        } else {
            sampleAppointments.forEach { addAppointmentItem(it) }
        }
    }

    private fun addAppointmentItem(item: AppointmentItem) {
        val view = LayoutInflater.from(this).inflate(
            R.layout.item_appointment_dashboard,
            appointmentsContainer,
            false
        )

        view.findViewById<TextView>(R.id.appointmentReason).text = item.reason
        view.findViewById<TextView>(R.id.appointmentDoctor).text = item.doctor
        view.findViewById<TextView>(R.id.appointmentDateTime).text = item.dateTime

        val statusBadge = view.findViewById<TextView>(R.id.appointmentStatus)
        statusBadge.text = item.status

        val (bgRes, textColor) = when (item.status.lowercase()) {
            "scheduled" -> Pair(R.drawable.badge_scheduled_bg, 0xFF2196F3.toInt())
            "completed" -> Pair(R.drawable.badge_completed_bg, 0xFF4CAF50.toInt())
            "cancelled" -> Pair(R.drawable.badge_cancelled_bg, 0xFFF44336.toInt())
            else -> Pair(R.drawable.badge_pending_bg, 0xFFFF8F00.toInt())
        }
        statusBadge.setBackgroundResource(bgRes)
        statusBadge.setTextColor(textColor)

        appointmentsContainer.addView(view)
    }

    data class AppointmentItem(
        val reason: String,
        val doctor: String,
        val dateTime: String,
        val status: String
    )
}
