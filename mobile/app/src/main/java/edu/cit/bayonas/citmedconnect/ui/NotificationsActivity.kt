package edu.cit.bayonas.citmedconnect.ui

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import edu.cit.bayonas.citmedconnect.R
import edu.cit.bayonas.citmedconnect.data.SessionManager
import edu.cit.bayonas.citmedconnect.data.model.NotificationResponse
import edu.cit.bayonas.citmedconnect.ui.viewmodel.NotificationViewModel

class NotificationsActivity : AppCompatActivity() {

    private lateinit var viewModel: NotificationViewModel

    private lateinit var headerSubtitle: TextView
    private lateinit var sendNotifBtn: LinearLayout
    private lateinit var tabAll: TextView
    private lateinit var tabUnread: TextView
    private lateinit var tabRead: TextView
    private lateinit var loadingState: LinearLayout
    private lateinit var emptyState: LinearLayout
    private lateinit var emptySubtitle: TextView
    private lateinit var notificationsContainer: LinearLayout
    private lateinit var navHome: LinearLayout
    private lateinit var navAppointments: LinearLayout
    private lateinit var navMedicalRecords: LinearLayout
    private lateinit var navNotifications: LinearLayout
    private lateinit var navProfile: LinearLayout

    private var isAdmin = false
    private var currentTab = "All"
    private var allNotifications: List<NotificationResponse> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = getColor(R.color.primary_red)
        setContentView(R.layout.activity_notifications)

        isAdmin = SessionManager.isAdmin(this)
        viewModel = ViewModelProvider(this)[NotificationViewModel::class.java]

        bindViews()
        adaptToRole()
        setupTabs()
        setupNavigation()
        observeViewModel()
        loadNotifications()
    }

    private fun bindViews() {
        headerSubtitle         = findViewById(R.id.headerSubtitle)
        sendNotifBtn           = findViewById(R.id.sendNotifBtn)
        tabAll                 = findViewById(R.id.tabAll)
        tabUnread              = findViewById(R.id.tabUnread)
        tabRead                = findViewById(R.id.tabRead)
        loadingState           = findViewById(R.id.loadingState)
        emptyState             = findViewById(R.id.emptyState)
        emptySubtitle          = findViewById(R.id.emptySubtitle)
        notificationsContainer = findViewById(R.id.notificationsContainer)
        navHome                = findViewById(R.id.navHome)
        navAppointments        = findViewById(R.id.navAppointments)
        navMedicalRecords      = findViewById(R.id.navMedicalRecords)
        navNotifications       = findViewById(R.id.navNotifications)
        navProfile             = findViewById(R.id.navProfile)
    }

    private fun adaptToRole() {
        if (isAdmin) {
            headerSubtitle.text = "Manage and send notifications to users"
            sendNotifBtn.visibility = View.VISIBLE
            sendNotifBtn.setOnClickListener { showSendDialog() }
        } else {
            headerSubtitle.text = "Your latest updates and alerts"
            sendNotifBtn.visibility = View.GONE
        }
    }

    private fun setupTabs() {
        tabAll.setOnClickListener    { selectTab("All") }
        tabUnread.setOnClickListener { selectTab("Unread") }
        tabRead.setOnClickListener   { selectTab("Read") }
    }

    private fun selectTab(tab: String) {
        currentTab = tab

        val activeText  = 0xFFFFFFFF.toInt()
        val inactiveText = 0xFF616161.toInt()

        tabAll.setBackgroundResource(if (tab == "All") R.drawable.filter_chip_active_bg else R.drawable.filter_chip_inactive_bg)
        tabAll.setTextColor(if (tab == "All") activeText else inactiveText)

        tabUnread.setBackgroundResource(if (tab == "Unread") R.drawable.filter_chip_active_bg else R.drawable.filter_chip_inactive_bg)
        tabUnread.setTextColor(if (tab == "Unread") activeText else inactiveText)

        tabRead.setBackgroundResource(if (tab == "Read") R.drawable.filter_chip_active_bg else R.drawable.filter_chip_inactive_bg)
        tabRead.setTextColor(if (tab == "Read") activeText else inactiveText)

        renderNotifications(allNotifications)
    }

    private fun setupNavigation() {
        navHome.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }
        navAppointments.setOnClickListener {
            val target = if (isAdmin) AdminAppointmentsActivity::class.java else AppointmentsActivity::class.java
            startActivity(Intent(this, target))
            overridePendingTransition(0, 0)
            finish()
        }
        navMedicalRecords.setOnClickListener {
            startActivity(Intent(this, MedicalRecordsActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }
        navNotifications.setOnClickListener { /* already here */ }
        navProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { loading ->
            loadingState.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.notifications.observe(this) { list ->
            allNotifications = list
            renderNotifications(list)
        }

        viewModel.error.observe(this) { msg ->
            if (!msg.isNullOrBlank()) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }

        viewModel.actionSuccess.observe(this) { msg ->
            if (!msg.isNullOrBlank()) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                viewModel.clearActionSuccess()
            }
        }
    }

    private fun loadNotifications() {
        val schoolId = SessionManager.schoolId(this) ?: return
        val role     = SessionManager.role(this) ?: "STUDENT"
        viewModel.fetchNotifications(schoolId, role)
    }

    private fun renderNotifications(list: List<NotificationResponse>) {
        val filtered = when (currentTab) {
            "Unread" -> list.filter { !it.read }
            "Read"   -> list.filter { it.read }
            else     -> list
        }

        notificationsContainer.removeAllViews()

        if (filtered.isEmpty()) {
            emptyState.visibility = View.VISIBLE
            emptySubtitle.text = when (currentTab) {
                "Unread" -> "No unread notifications."
                "Read"   -> "No read notifications yet."
                else     -> "You're all caught up! No new notifications."
            }
        } else {
            emptyState.visibility = View.GONE
            filtered.forEach { notif -> inflateCard(notif) }
        }
    }

    private fun inflateCard(notif: NotificationResponse) {
        val view = LayoutInflater.from(this).inflate(
            R.layout.item_notification_card, notificationsContainer, false
        )

        val iconContainer = view.findViewById<LinearLayout>(R.id.iconContainer)
        val notifIcon     = view.findViewById<android.widget.ImageView>(R.id.notifIcon)
        val notifTitle    = view.findViewById<TextView>(R.id.notifTitle)
        val unreadDot     = view.findViewById<View>(R.id.unreadDot)
        val notifMessage  = view.findViewById<TextView>(R.id.notifMessage)
        val notifTime     = view.findViewById<TextView>(R.id.notifTime)
        val btnDelete     = view.findViewById<LinearLayout>(R.id.btnDelete)

        notifTitle.text   = notif.title ?: "Notification"
        notifMessage.text = notif.message ?: ""
        notifTime.text    = notif.displayTime

        unreadDot.visibility = if (!notif.read) View.VISIBLE else View.GONE

        // Icon and color by type
        val (iconRes, iconColorHex, bgRes) = when (notif.notificationType?.lowercase()) {
            "appointment" -> Triple(R.drawable.ic_appointment, 0xFF2196F3.toInt(), R.drawable.stat_icon_bg_blue)
            "medical_record" -> Triple(R.drawable.ic_records, 0xFF4CAF50.toInt(), R.drawable.stat_icon_bg_green)
            "student_broadcast", "staff_broadcast", "global_broadcast" ->
                Triple(R.drawable.ic_bell, 0xFFFF8F00.toInt(), R.drawable.stat_icon_bg_amber)
            else -> Triple(R.drawable.ic_bell, 0xFF2196F3.toInt(), R.drawable.stat_icon_bg_blue)
        }
        notifIcon.setImageResource(iconRes)
        notifIcon.setColorFilter(iconColorHex)
        iconContainer.setBackgroundResource(bgRes)

        // Dim card if read
        view.alpha = if (notif.read) 0.7f else 1.0f

        // Mark as read on tap
        view.setOnClickListener {
            if (!notif.read) {
                val id = notif.notificationId ?: return@setOnClickListener
                viewModel.markAsRead(id)
                unreadDot.visibility = View.GONE
                view.alpha = 0.7f
            }
        }

        // Delete button for admin
        if (isAdmin) {
            btnDelete.visibility = View.VISIBLE
            btnDelete.setOnClickListener {
                val id = notif.notificationId ?: return@setOnClickListener
                viewModel.delete(id)
            }
        }

        notificationsContainer.addView(view)
    }

    private fun showSendDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_send_notification, null)
        dialog.setContentView(dialogView)
        dialog.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            setLayout(
                (resources.displayMetrics.widthPixels * 0.92).toInt(),
                android.view.WindowManager.LayoutParams.WRAP_CONTENT
            )
        }

        val audienceGroup   = dialogView.findViewById<RadioGroup>(R.id.audienceGroup)
        val titleInput      = dialogView.findViewById<EditText>(R.id.titleInput)
        val messageInput    = dialogView.findViewById<EditText>(R.id.messageInput)
        val typeSpinner     = dialogView.findViewById<Spinner>(R.id.typeSpinner)
        val typeContainer   = dialogView.findViewById<FrameLayout>(R.id.typeSpinnerContainer)
        val closeBtn        = dialogView.findViewById<LinearLayout>(R.id.closeBtn)
        val cancelBtn       = dialogView.findViewById<LinearLayout>(R.id.cancelBtn)
        val sendBtn         = dialogView.findViewById<LinearLayout>(R.id.sendBtn)
        val sendBtnText     = dialogView.findViewById<TextView>(R.id.sendBtnText)

        // Type spinner
        val types = listOf("Info", "Appointment", "Announcement")
        typeSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, types).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        typeContainer.setOnClickListener { typeSpinner.performClick() }

        closeBtn.setOnClickListener  { dialog.dismiss() }
        cancelBtn.setOnClickListener { dialog.dismiss() }

        sendBtn.setOnClickListener {
            val title   = titleInput.text.toString().trim()
            val message = messageInput.text.toString().trim()
            val type    = types[typeSpinner.selectedItemPosition].lowercase()

            if (title.isBlank()) {
                titleInput.error = "Title is required"
                return@setOnClickListener
            }
            if (message.isBlank()) {
                messageInput.error = "Message is required"
                return@setOnClickListener
            }

            val audience = when (audienceGroup.checkedRadioButtonId) {
                R.id.radioStudents -> "students"
                R.id.radioStaff    -> "staff"
                else               -> "all"
            }

            sendBtnText.text = "Sending..."
            sendBtn.isEnabled = false

            viewModel.sendBroadcast(audience, title, message, type)

            viewModel.actionSuccess.observe(this) { msg ->
                if (!msg.isNullOrBlank()) {
                    dialog.dismiss()
                }
            }
            viewModel.error.observe(this) { err ->
                if (!err.isNullOrBlank()) {
                    sendBtnText.text = "Send Notification"
                    sendBtn.isEnabled = true
                }
            }
        }

        dialog.show()
    }
}
