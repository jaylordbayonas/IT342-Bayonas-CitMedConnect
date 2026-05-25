package edu.cit.bayonas.citmedconnect.ui

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import java.util.Calendar
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import edu.cit.bayonas.citmedconnect.R
import edu.cit.bayonas.citmedconnect.data.SessionManager
import edu.cit.bayonas.citmedconnect.data.model.AppointmentResponse
import edu.cit.bayonas.citmedconnect.data.model.TimeSlotData
import edu.cit.bayonas.citmedconnect.ui.viewmodel.AppointmentViewModel

class AdminAppointmentsActivity : AppCompatActivity() {

    private lateinit var viewModel: AppointmentViewModel
    private lateinit var searchInput: EditText
    private lateinit var filterAll: TextView
    private lateinit var filterScheduled: TextView
    private lateinit var filterCompleted: TextView
    private lateinit var filterSuccess: TextView
    private lateinit var filterCancelled: TextView
    private lateinit var appointmentsListContainer: LinearLayout
    private lateinit var emptyStateContainer: LinearLayout
    private lateinit var emptySubtitle: TextView
    private lateinit var manageSlotsBtn: LinearLayout
    private lateinit var navHome: LinearLayout
    private lateinit var navAppointments: LinearLayout
    private lateinit var navMedicalRecords: LinearLayout
    private lateinit var navNotifications: LinearLayout
    private lateinit var navProfile: LinearLayout

    private var currentFilter = "All"
    private var currentSearch = ""
    private var currentAppointments: List<AppointmentResponse> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = getColor(R.color.primary_red)
        setContentView(R.layout.activity_admin_appointments)

        viewModel = ViewModelProvider(this).get(AppointmentViewModel::class.java)

        bindViews()
        setupFilterChips()
        setupSearch()
        setupNavigation()
        observeViewModel()
        viewModel.fetchAllAppointments()
    }

    private fun bindViews() {
        searchInput               = findViewById(R.id.searchInput)
        filterAll                 = findViewById(R.id.filterAll)
        filterScheduled           = findViewById(R.id.filterScheduled)
        filterCompleted           = findViewById(R.id.filterCompleted)
        filterSuccess             = findViewById(R.id.filterSuccess)
        filterCancelled           = findViewById(R.id.filterCancelled)
        appointmentsListContainer = findViewById(R.id.appointmentsListContainer)
        emptyStateContainer       = findViewById(R.id.emptyStateContainer)
        emptySubtitle             = findViewById(R.id.emptySubtitle)
        manageSlotsBtn            = findViewById(R.id.manageSlotsBtn)
        navHome                   = findViewById(R.id.navHome)
        navAppointments           = findViewById(R.id.navAppointments)
        navMedicalRecords         = findViewById(R.id.navMedicalRecords)
        navNotifications          = findViewById(R.id.navNotifications)
        navProfile                = findViewById(R.id.navProfile)
    }

    private fun observeViewModel() {
        viewModel.appointments.observe(this) { list ->
            currentAppointments = list
            renderAppointments()
        }
        viewModel.actionSuccess.observe(this) { msg ->
            if (msg != null) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                viewModel.clearActionSuccess()
            }
        }
        viewModel.error.observe(this) { err ->
            if (err != null) {
                Toast.makeText(this, err, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    private fun setupFilterChips() {
        val chips = listOf(
            filterAll to "All",
            filterScheduled to "Scheduled",
            filterCompleted to "Completed",
            filterSuccess to "Success",
            filterCancelled to "Cancelled"
        )
        chips.forEach { (chip, label) ->
            chip.setOnClickListener {
                currentFilter = label
                updateChipStyles(chip)
                renderAppointments()
            }
        }
    }

    private fun updateChipStyles(activeChip: TextView) {
        listOf(filterAll, filterScheduled, filterCompleted, filterSuccess, filterCancelled).forEach { chip ->
            if (chip == activeChip) {
                chip.setBackgroundResource(R.drawable.filter_chip_active_bg)
                chip.setTextColor(0xFFFFFFFF.toInt())
                chip.setTypeface(null, android.graphics.Typeface.BOLD)
            } else {
                chip.setBackgroundResource(R.drawable.filter_chip_inactive_bg)
                chip.setTextColor(0xFF616161.toInt())
                chip.setTypeface(null, android.graphics.Typeface.NORMAL)
            }
        }
    }

    private fun setupSearch() {
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                currentSearch = s?.toString()?.trim() ?: ""
                renderAppointments()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupNavigation() {
        navHome.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }
        navAppointments.setOnClickListener { }
        navMedicalRecords.setOnClickListener {
            Toast.makeText(this, "Medical Records coming soon", Toast.LENGTH_SHORT).show()
        }
        navNotifications.setOnClickListener {
            Toast.makeText(this, "Notifications coming soon", Toast.LENGTH_SHORT).show()
        }
        navProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            overridePendingTransition(0, 0)
        }
        manageSlotsBtn.setOnClickListener { showManageSlotsDialog() }
    }

    private fun renderAppointments() {
        appointmentsListContainer.removeAllViews()

        val filtered = currentAppointments.filter { appt ->
            val matchesFilter = currentFilter == "All" ||
                    appt.displayStatus.equals(currentFilter, ignoreCase = true)
            val matchesSearch = currentSearch.isEmpty() ||
                    (appt.reason?.contains(currentSearch, ignoreCase = true) == true) ||
                    (appt.user?.schoolId?.contains(currentSearch, ignoreCase = true) == true) ||
                    (appt.user?.fullName?.contains(currentSearch, ignoreCase = true) == true) ||
                    (appt.timeSlot?.displayLocation?.contains(currentSearch, ignoreCase = true) == true)
            matchesFilter && matchesSearch
        }

        if (filtered.isEmpty()) {
            appointmentsListContainer.visibility = View.GONE
            emptyStateContainer.visibility = View.VISIBLE
            emptySubtitle.text = if (currentSearch.isNotEmpty() || currentFilter != "All") {
                "No appointments match your current filter or search."
            } else {
                "No appointments have been booked yet."
            }
        } else {
            appointmentsListContainer.visibility = View.VISIBLE
            emptyStateContainer.visibility = View.GONE
            filtered.forEach { addAppointmentCard(it) }
        }
    }

    private fun addAppointmentCard(item: AppointmentResponse) {
        val view = LayoutInflater.from(this).inflate(
            R.layout.item_admin_appointment_card, appointmentsListContainer, false
        )

        view.findViewById<TextView>(R.id.appointmentReason).text   = item.reason ?: "—"
        view.findViewById<TextView>(R.id.appointmentUserId).text   = item.user?.schoolId ?: "—"
        view.findViewById<TextView>(R.id.appointmentUserName).text  = item.user?.fullName ?: "—"
        view.findViewById<TextView>(R.id.appointmentDateTime).text  = item.displayDateTime
        view.findViewById<TextView>(R.id.appointmentLocation).text  = item.timeSlot?.displayLocation ?: "Main Clinic"

        val statusBadge  = view.findViewById<TextView>(R.id.statusBadge)
        val statusAccent = view.findViewById<View>(R.id.statusAccent)
        val btnComplete  = view.findViewById<LinearLayout>(R.id.btnComplete)
        val btnReschedule = view.findViewById<LinearLayout>(R.id.btnReschedule)
        val btnCancel    = view.findViewById<LinearLayout>(R.id.btnCancel)

        statusBadge.text = item.displayStatus
        when (item.displayStatus.lowercase()) {
            "scheduled" -> {
                statusBadge.setBackgroundResource(R.drawable.badge_scheduled_bg)
                statusBadge.setTextColor(0xFF2196F3.toInt())
                statusAccent.setBackgroundColor(0xFF2196F3.toInt())
                btnComplete.visibility   = View.VISIBLE
                btnReschedule.visibility = View.VISIBLE
                btnCancel.visibility     = View.VISIBLE
            }
            "completed" -> {
                statusBadge.setBackgroundResource(R.drawable.badge_completed_bg)
                statusBadge.setTextColor(0xFF4CAF50.toInt())
                statusAccent.setBackgroundColor(0xFF4CAF50.toInt())
            }
            "success" -> {
                statusBadge.setBackgroundResource(R.drawable.badge_success_bg)
                statusBadge.setTextColor(0xFF00897B.toInt())
                statusAccent.setBackgroundColor(0xFF00897B.toInt())
            }
            "cancelled" -> {
                statusBadge.setBackgroundResource(R.drawable.badge_cancelled_bg)
                statusBadge.setTextColor(0xFFF44336.toInt())
                statusAccent.setBackgroundColor(0xFFF44336.toInt())
            }
            else -> {
                statusBadge.setBackgroundResource(R.drawable.badge_pending_bg)
                statusBadge.setTextColor(0xFFFF8F00.toInt())
                statusAccent.setBackgroundColor(0xFFFF8F00.toInt())
            }
        }

        view.findViewById<LinearLayout>(R.id.btnView).setOnClickListener {
            showAppointmentDetailsDialog(item)
        }
        btnComplete.setOnClickListener {
            item.appointmentId?.let { id ->
                showConfirmDialog(
                    title        = "Mark as Complete",
                    message      = "Mark \"${item.reason}\" by ${item.user?.fullName} as completed?",
                    confirmLabel = "Mark Complete",
                    onConfirm    = { viewModel.completeAppointment(id, isAdmin = true) }
                )
            }
        }
        btnReschedule.setOnClickListener {
            Toast.makeText(this, "Reschedule coming soon", Toast.LENGTH_SHORT).show()
        }
        btnCancel.setOnClickListener {
            item.appointmentId?.let { id ->
                showConfirmDialog(
                    title        = "Cancel Appointment",
                    message      = "Cancel \"${item.reason}\" by ${item.user?.fullName}? This cannot be undone.",
                    confirmLabel = "Cancel Appointment",
                    onConfirm    = { viewModel.cancelAppointment(id, isAdmin = true) }
                )
            }
        }

        appointmentsListContainer.addView(view)
    }

    // ================================================================
    // APPOINTMENT DETAILS DIALOG
    // ================================================================

    private fun showAppointmentDetailsDialog(item: AppointmentResponse) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            setLayout(
                (resources.displayMetrics.widthPixels * 0.88).toInt(),
                android.view.WindowManager.LayoutParams.WRAP_CONTENT
            )
            setGravity(Gravity.CENTER)
        }

        val dp = resources.displayMetrics.density
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.dialog_rounded_bg)
            setPadding((20*dp).toInt(), (20*dp).toInt(), (20*dp).toInt(), (20*dp).toInt())
        }

        fun row(label: String, value: String) = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.bottomMargin = (12*dp).toInt() }
            addView(TextView(context).apply { text = label; textSize = 11f; setTextColor(0xFF9E9E9E.toInt()) })
            addView(TextView(context).apply {
                text = value; textSize = 14f; setTextColor(0xFF212121.toInt())
                setTypeface(null, android.graphics.Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.topMargin = (2*dp).toInt() }
            })
        }

        val titleRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.bottomMargin = (16*dp).toInt() }
            addView(TextView(context).apply {
                text = "Appointment Details"; textSize = 17f; setTextColor(0xFF212121.toInt())
                setTypeface(null, android.graphics.Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            })
            addView(LinearLayout(context).apply {
                layoutParams = LinearLayout.LayoutParams((34*dp).toInt(), (34*dp).toInt())
                setBackgroundResource(R.drawable.close_btn_bg); gravity = android.view.Gravity.CENTER
                setOnClickListener { dialog.dismiss() }
                addView(android.widget.ImageView(context).apply {
                    layoutParams = LinearLayout.LayoutParams((17*dp).toInt(), (17*dp).toInt())
                    setImageResource(R.drawable.ic_close)
                    imageTintList = android.content.res.ColorStateList.valueOf(0xFF757575.toInt())
                })
            })
        }

        root.addView(titleRow)
        root.addView(View(this).apply {
            setBackgroundColor(0xFFF0F0F0.toInt())
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (1*dp).toInt())
                .also { it.bottomMargin = (16*dp).toInt() }
        })
        root.addView(row("Reason / Symptoms", item.reason ?: "—"))
        root.addView(row("Student ID", item.user?.schoolId ?: "—"))
        root.addView(row("Student Name", item.user?.fullName ?: "—"))
        root.addView(row("Date & Time", item.displayDateTime))
        root.addView(row("Location", item.timeSlot?.displayLocation ?: "Main Clinic"))
        if (!item.notes.isNullOrBlank()) root.addView(row("Notes", item.notes))
        root.addView(row("Status", item.displayStatus))

        dialog.setContentView(root)
        dialog.setCanceledOnTouchOutside(true)
        dialog.show()
    }

    // ================================================================
    // GENERIC CONFIRM DIALOG
    // ================================================================

    private fun showConfirmDialog(title: String, message: String, confirmLabel: String, onConfirm: () -> Unit) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            setLayout((resources.displayMetrics.widthPixels * 0.88).toInt(), android.view.WindowManager.LayoutParams.WRAP_CONTENT)
            setGravity(Gravity.CENTER)
        }
        val dp = resources.displayMetrics.density
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.dialog_rounded_bg)
            setPadding((20*dp).toInt(), (20*dp).toInt(), (20*dp).toInt(), (20*dp).toInt())
        }
        root.addView(TextView(this).apply {
            text = title; textSize = 17f; setTextColor(0xFF212121.toInt())
            setTypeface(null, android.graphics.Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).also { it.bottomMargin = (10*dp).toInt() }
        })
        root.addView(TextView(this).apply {
            text = message; textSize = 13f; setTextColor(0xFF616161.toInt()); setLineSpacing(3*dp, 1f)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).also { it.bottomMargin = (20*dp).toInt() }
        })
        val btnRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        btnRow.addView(LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, (44*dp).toInt(), 1f).also { it.marginEnd = (8*dp).toInt() }
            setBackgroundResource(R.drawable.filter_chip_inactive_bg); gravity = android.view.Gravity.CENTER
            setOnClickListener { dialog.dismiss() }
            addView(TextView(context).apply { text = "Go Back"; textSize = 13f; setTextColor(0xFF616161.toInt()); setTypeface(null, android.graphics.Typeface.BOLD) })
        })
        btnRow.addView(LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, (44*dp).toInt(), 1f)
            setBackgroundResource(R.drawable.button_primary_pill); gravity = android.view.Gravity.CENTER
            setOnClickListener { dialog.dismiss(); onConfirm() }
            addView(TextView(context).apply { text = confirmLabel; textSize = 13f; setTextColor(0xFFFFFFFF.toInt()); setTypeface(null, android.graphics.Typeface.BOLD) })
        })
        root.addView(btnRow)
        dialog.setContentView(root)
        dialog.setCanceledOnTouchOutside(true)
        dialog.show()
    }

    // ================================================================
    // MANAGE SLOTS DIALOG
    // ================================================================

    private fun showManageSlotsDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_manage_slots)
        dialog.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            setLayout(
                (resources.displayMetrics.widthPixels * 0.92).toInt(),
                android.view.WindowManager.LayoutParams.WRAP_CONTENT
            )
            setGravity(Gravity.CENTER)
        }
        dialog.setCanceledOnTouchOutside(true)

        val scrollView           = dialog.findViewById<ScrollView>(R.id.manageSlotsScrollView)
        scrollView.layoutParams.height = (resources.displayMetrics.heightPixels * 0.75).toInt()

        val closeBtn             = dialog.findViewById<LinearLayout>(R.id.closeBtn)
        val slotDateInput        = dialog.findViewById<EditText>(R.id.slotDateInput)
        val slotStartTimeInput   = dialog.findViewById<EditText>(R.id.slotStartTimeInput)
        val slotEndTimeInput     = dialog.findViewById<EditText>(R.id.slotEndTimeInput)
        val slotMaxBookingsInput = dialog.findViewById<EditText>(R.id.slotMaxBookingsInput)
        val addSlotBtn           = dialog.findViewById<LinearLayout>(R.id.addSlotBtn)
        val manageSlotsContainer = dialog.findViewById<LinearLayout>(R.id.manageSlotsContainer)
        val emptyManagedSlots    = dialog.findViewById<LinearLayout>(R.id.emptyManagedSlots)
        val slotCountText        = dialog.findViewById<TextView>(R.id.slotCountText)

        closeBtn.setOnClickListener { dialog.dismiss() }

        slotDateInput.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                slotDateInput.setText(String.format("%04d-%02d-%02d", year, month + 1, day))
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }
        slotStartTimeInput.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(this, { _, hour, minute ->
                slotStartTimeInput.setText(String.format("%02d:%02d", hour, minute))
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }
        slotEndTimeInput.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(this, { _, hour, minute ->
                slotEndTimeInput.setText(String.format("%02d:%02d", hour, minute))
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }

        fun refreshSlotsList(slots: List<TimeSlotData>) {
            manageSlotsContainer.removeAllViews()
            slotCountText.text = "Available Slots (${slots.size})"
            if (slots.isEmpty()) {
                emptyManagedSlots.visibility    = View.VISIBLE
                manageSlotsContainer.visibility = View.GONE
            } else {
                emptyManagedSlots.visibility    = View.GONE
                manageSlotsContainer.visibility = View.VISIBLE
                slots.forEach { slot -> addManagedSlotItem(manageSlotsContainer, slot) }
            }
        }

        val slotsObserver = Observer<List<TimeSlotData>> { slots ->
            if (dialog.isShowing) refreshSlotsList(slots)
        }
        viewModel.slots.observe(this, slotsObserver)
        viewModel.fetchAllSlots()

        dialog.setOnDismissListener { viewModel.slots.removeObserver(slotsObserver) }

        addSlotBtn.setOnClickListener {
            val date        = slotDateInput.text.toString().trim()
            val startTime   = slotStartTimeInput.text.toString().trim()
            val endTime     = slotEndTimeInput.text.toString().trim()
            val maxBookings = slotMaxBookingsInput.text.toString().trim().toIntOrNull() ?: 1

            if (date.isEmpty() || startTime.isEmpty()) {
                Toast.makeText(this, "Date and Start Time are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val staffId = SessionManager.userId(this) ?: ""
            viewModel.createTimeSlot(date, startTime, endTime, maxBookings, staffId)
            slotDateInput.text.clear()
            slotStartTimeInput.text.clear()
            slotEndTimeInput.text.clear()
            slotMaxBookingsInput.setText("1")
        }

        dialog.show()
    }

    private fun addManagedSlotItem(container: LinearLayout, slot: TimeSlotData) {
        val view = LayoutInflater.from(this).inflate(R.layout.item_slot_manage_card, container, false)

        val endDisplay = if (!slot.endTime.isNullOrEmpty()) " – ${slot.endTime.take(5)}" else ""
        val timeDisplay = "${slot.slotDate} · ${slot.startTime?.take(5) ?: ""}$endDisplay"
        val bookedStatus = slot.bookedStatus
        val statusDisplay = "${slot.displayLocation} · $bookedStatus (${slot.currentBookings ?: 0}/${slot.maxBookings ?: 1})"

        view.findViewById<TextView>(R.id.slotManageDateTime).text = timeDisplay
        view.findViewById<TextView>(R.id.slotManageStatus).text   = statusDisplay

        view.findViewById<LinearLayout>(R.id.btnSlotEdit).setOnClickListener {
            Toast.makeText(this, "Edit slot coming soon", Toast.LENGTH_SHORT).show()
        }
        view.findViewById<LinearLayout>(R.id.btnSlotDelete).setOnClickListener {
            slot.timeSlotId?.let { id ->
                showConfirmDialog(
                    title        = "Delete Time Slot",
                    message      = "Delete the slot on ${slot.slotDate} at ${slot.startTime?.take(5)}? Students won't be able to book it.",
                    confirmLabel = "Delete Slot",
                    onConfirm    = { viewModel.deleteTimeSlot(id) }
                )
            }
        }

        container.addView(view)
    }
}
