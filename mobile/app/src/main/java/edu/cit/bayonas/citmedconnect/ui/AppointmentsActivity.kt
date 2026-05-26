package edu.cit.bayonas.citmedconnect.ui

import android.app.Dialog
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.ImageView
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
import edu.cit.bayonas.citmedconnect.ui.viewmodel.NotificationViewModel

class AppointmentsActivity : AppCompatActivity() {

    private lateinit var viewModel: AppointmentViewModel
    private lateinit var notifViewModel: NotificationViewModel
    private lateinit var searchInput: EditText
    private lateinit var filterAll: TextView
    private lateinit var filterScheduled: TextView
    private lateinit var filterCompleted: TextView
    private lateinit var filterCancelled: TextView
    private lateinit var appointmentsListContainer: LinearLayout
    private lateinit var emptyStateContainer: LinearLayout
    private lateinit var emptySubtitle: TextView
    private lateinit var bookAppointmentBtn: LinearLayout
    private lateinit var emptyBookBtn: LinearLayout
    private lateinit var navHome: LinearLayout
    private lateinit var navAppointments: LinearLayout
    private lateinit var navMedicalRecords: LinearLayout
    private lateinit var navNotifications: LinearLayout
    private lateinit var navProfile: LinearLayout
    private lateinit var navNotifBadge: View

    private var currentFilter = "All"
    private var currentSearch = ""
    private var currentAppointments: List<AppointmentResponse> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = getColor(R.color.primary_red)
        setContentView(R.layout.activity_appointments)

        viewModel      = ViewModelProvider(this).get(AppointmentViewModel::class.java)
        notifViewModel = ViewModelProvider(this)[NotificationViewModel::class.java]

        bindViews()
        setupFilterChips()
        setupSearch()
        setupNavigation()
        observeViewModel()
        observeNotifBadge()
        viewModel.fetchMyAppointments()

        if (intent.getBooleanExtra("OPEN_BOOKING_MODAL", false)) {
            showBookAppointmentModal()
        }
    }

    private fun bindViews() {
        searchInput              = findViewById(R.id.searchInput)
        filterAll                = findViewById(R.id.filterAll)
        filterScheduled          = findViewById(R.id.filterScheduled)
        filterCompleted          = findViewById(R.id.filterCompleted)
        filterCancelled          = findViewById(R.id.filterCancelled)
        appointmentsListContainer = findViewById(R.id.appointmentsListContainer)
        emptyStateContainer      = findViewById(R.id.emptyStateContainer)
        emptySubtitle            = findViewById(R.id.emptySubtitle)
        bookAppointmentBtn       = findViewById(R.id.bookAppointmentBtn)
        emptyBookBtn             = findViewById(R.id.emptyBookBtn)
        navHome                  = findViewById(R.id.navHome)
        navAppointments          = findViewById(R.id.navAppointments)
        navMedicalRecords        = findViewById(R.id.navMedicalRecords)
        navNotifications         = findViewById(R.id.navNotifications)
        navProfile               = findViewById(R.id.navProfile)
        navNotifBadge            = findViewById(R.id.navNotifBadge)
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
            filterAll to "All", filterScheduled to "Scheduled",
            filterCompleted to "Completed", filterCancelled to "Cancelled"
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
        listOf(filterAll, filterScheduled, filterCompleted, filterCancelled).forEach { chip ->
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
            startActivity(Intent(this, MedicalRecordsActivity::class.java))
            overridePendingTransition(0, 0)
        }
        navNotifications.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }
        navProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            overridePendingTransition(0, 0)
        }
        bookAppointmentBtn.setOnClickListener { showBookAppointmentModal() }
        emptyBookBtn.setOnClickListener { showBookAppointmentModal() }
    }

    private fun renderAppointments() {
        appointmentsListContainer.removeAllViews()

        val filtered = currentAppointments.filter { appt ->
            val matchesFilter = currentFilter == "All" ||
                    appt.displayStatus.equals(currentFilter, ignoreCase = true)
            val matchesSearch = currentSearch.isEmpty() ||
                    (appt.reason?.contains(currentSearch, ignoreCase = true) == true) ||
                    (appt.timeSlot?.displayLocation?.contains(currentSearch, ignoreCase = true) == true)
            matchesFilter && matchesSearch
        }

        if (filtered.isEmpty()) {
            appointmentsListContainer.visibility = View.GONE
            emptyStateContainer.visibility = View.VISIBLE
            emptySubtitle.text = if (currentSearch.isNotEmpty() || currentFilter != "All") {
                "No appointments match your current filter or search."
            } else {
                "Book an appointment to get started with your healthcare journey."
            }
        } else {
            appointmentsListContainer.visibility = View.VISIBLE
            emptyStateContainer.visibility = View.GONE
            filtered.forEach { addAppointmentCard(it) }
        }
    }

    private fun addAppointmentCard(item: AppointmentResponse) {
        val view = LayoutInflater.from(this).inflate(
            R.layout.item_appointment_full_card, appointmentsListContainer, false
        )

        view.findViewById<TextView>(R.id.appointmentReason).text   = item.reason ?: "—"
        view.findViewById<TextView>(R.id.appointmentDateTime).text  = item.displayDateTime
        view.findViewById<TextView>(R.id.appointmentLocation).text  = item.timeSlot?.displayLocation ?: "Main Clinic"

        val statusBadge  = view.findViewById<TextView>(R.id.statusBadge)
        val statusAccent = view.findViewById<View>(R.id.statusAccent)
        val btnEdit      = view.findViewById<LinearLayout>(R.id.btnEdit)
        val btnDelete    = view.findViewById<LinearLayout>(R.id.btnDelete)

        statusBadge.text = item.displayStatus
        when (item.displayStatus.lowercase()) {
            "scheduled" -> {
                statusBadge.setBackgroundResource(R.drawable.badge_scheduled_bg)
                statusBadge.setTextColor(0xFF2196F3.toInt())
                statusAccent.setBackgroundColor(0xFF2196F3.toInt())
                btnEdit.visibility   = View.VISIBLE
                btnDelete.visibility = View.VISIBLE
            }
            "completed" -> {
                statusBadge.setBackgroundResource(R.drawable.badge_completed_bg)
                statusBadge.setTextColor(0xFF4CAF50.toInt())
                statusAccent.setBackgroundColor(0xFF4CAF50.toInt())
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
            showDetailsDialog(item)
        }
        btnEdit.setOnClickListener {
            Toast.makeText(this, "Reschedule coming soon", Toast.LENGTH_SHORT).show()
        }
        btnDelete.setOnClickListener {
            item.appointmentId?.let { id ->
                showConfirmDialog(
                    title        = "Cancel Appointment",
                    message      = "Cancel \"${item.reason}\"? This cannot be undone.",
                    confirmLabel = "Cancel Appointment",
                    onConfirm    = { viewModel.cancelAppointment(id) }
                )
            }
        }

        appointmentsListContainer.addView(view)
    }

    // ================================================================
    // DETAILS DIALOG
    // ================================================================

    private fun showDetailsDialog(item: AppointmentResponse) {
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
        root.addView(row("Date & Time", item.displayDateTime))
        root.addView(row("Location", item.timeSlot?.displayLocation ?: "Main Clinic"))
        if (!item.notes.isNullOrBlank()) root.addView(row("Notes", item.notes))
        root.addView(row("Status", item.displayStatus))

        dialog.setContentView(root)
        dialog.setCanceledOnTouchOutside(true)
        dialog.show()
    }

    // ================================================================
    // CONFIRM DIALOG
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
    // BOOK APPOINTMENT MODAL
    // ================================================================

    private fun showBookAppointmentModal() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_book_appointment)
        dialog.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            setLayout(
                (resources.displayMetrics.widthPixels * 0.92).toInt(),
                android.view.WindowManager.LayoutParams.WRAP_CONTENT
            )
            setGravity(Gravity.CENTER)
        }
        dialog.setCanceledOnTouchOutside(true)

        val closeBtn       = dialog.findViewById<LinearLayout>(R.id.closeBtn)
        val slotsContainer = dialog.findViewById<LinearLayout>(R.id.slotsContainer)
        val emptySlots     = dialog.findViewById<LinearLayout>(R.id.emptySlots)
        val confirmBtn     = dialog.findViewById<LinearLayout>(R.id.confirmBtn)
        val confirmBtnText = dialog.findViewById<TextView>(R.id.confirmBtnText)
        val scrollView     = dialog.findViewById<ScrollView>(R.id.slotsScrollView)
        val reasonInput    = dialog.findViewById<EditText>(R.id.reasonInput)
        val symptomsInput  = dialog.findViewById<EditText>(R.id.symptomsInput)

        scrollView.layoutParams.height = (resources.displayMetrics.heightPixels * 0.42).toInt()

        closeBtn.setOnClickListener { dialog.dismiss() }

        var selectedSlot: TimeSlotData? = null
        val slotViewMap = mutableMapOf<TimeSlotData, LinearLayout>()

        fun refreshSlots(slots: List<TimeSlotData>) {
            slotsContainer.removeAllViews()
            slotViewMap.clear()
            val today = java.time.LocalDate.now()
            val bookable = slots.filter { slot ->
                if (!slot.isBookable) return@filter false
                val date = runCatching { java.time.LocalDate.parse(slot.slotDate ?: "") }.getOrNull()
                date == null || !date.isBefore(today)
            }
            if (bookable.isEmpty()) {
                slotsContainer.visibility = View.GONE
                emptySlots.visibility     = View.VISIBLE
                confirmBtn.alpha    = 0.45f
                confirmBtn.isEnabled = false
            } else {
                slotsContainer.visibility = View.VISIBLE
                emptySlots.visibility     = View.GONE
                populateSlots(slotsContainer, bookable, slotViewMap) { slot ->
                    selectedSlot = slot
                    slotViewMap.forEach { (_, v) -> setSlotUnselected(v) }
                    setSlotSelected(slotViewMap[slot]!!)
                    confirmBtn.animate().alpha(1f).setDuration(160).start()
                    confirmBtnText.text = "Review Booking →"
                }
            }
        }

        val slotsObserver = Observer<List<TimeSlotData>> { slots ->
            if (dialog.isShowing) refreshSlots(slots)
        }
        viewModel.slots.observe(this, slotsObserver)
        viewModel.fetchAvailableSlots()

        dialog.setOnDismissListener { viewModel.slots.removeObserver(slotsObserver) }

        confirmBtn.setOnClickListener {
            val slot = selectedSlot ?: return@setOnClickListener
            val reason = reasonInput.text?.toString()?.trim() ?: ""
            if (reason.isEmpty()) {
                reasonInput.error = "Please describe your reason for visiting"
                return@setOnClickListener
            }
            val symptoms = symptomsInput.text?.toString()?.trim() ?: ""
            showBookingConfirmationDialog(dialog, slot, reason, symptoms)
        }

        dialog.show()
    }

    private fun showBookingConfirmationDialog(
        bookingDialog: Dialog,
        slot: TimeSlotData,
        reason: String,
        symptoms: String
    ) {
        val confirmDialog = Dialog(this)
        confirmDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        confirmDialog.setContentView(R.layout.dialog_booking_confirmation)
        confirmDialog.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            setLayout(
                (resources.displayMetrics.widthPixels * 0.92).toInt(),
                android.view.WindowManager.LayoutParams.WRAP_CONTENT
            )
            setGravity(Gravity.CENTER)
        }
        confirmDialog.setCanceledOnTouchOutside(false)

        confirmDialog.findViewById<LinearLayout>(R.id.closeBtn).setOnClickListener { confirmDialog.dismiss() }
        confirmDialog.findViewById<TextView>(R.id.confirmDate).text = formatSlotDate(slot.slotDate ?: "")
        confirmDialog.findViewById<TextView>(R.id.confirmTime).text = slot.displayTime
        confirmDialog.findViewById<TextView>(R.id.confirmLocation).text = slot.displayLocation
        confirmDialog.findViewById<TextView>(R.id.confirmReason).text = reason
        confirmDialog.findViewById<TextView>(R.id.confirmSymptoms).text =
            if (symptoms.isEmpty()) "None specified" else symptoms

        confirmDialog.findViewById<LinearLayout>(R.id.backBtn).setOnClickListener {
            confirmDialog.dismiss()
        }
        confirmDialog.findViewById<LinearLayout>(R.id.confirmBookingBtn).setOnClickListener {
            val studentId = SessionManager.schoolId(this) ?: SessionManager.userId(this) ?: ""
            slot.timeSlotId?.let { id ->
                viewModel.bookAppointment(id, studentId, reason, symptoms)
                confirmDialog.dismiss()
                bookingDialog.dismiss()
            }
        }

        confirmDialog.show()
    }

    private fun populateSlots(
        container: LinearLayout,
        slots: List<TimeSlotData>,
        viewMap: MutableMap<TimeSlotData, LinearLayout>,
        onSelect: (TimeSlotData) -> Unit
    ) {
        val dp = resources.displayMetrics.density
        slots.groupBy { it.slotDate ?: "" }.forEach { (date, dateSlots) ->
            container.addView(TextView(this).apply {
                text = formatSlotDate(date); textSize = 13f
                setTextColor(getColor(R.color.primary_red))
                setTypeface(null, android.graphics.Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.bottomMargin = (10*dp).toInt() }
            })
            container.addView(View(this).apply {
                setBackgroundColor(0xFFF0F0F0.toInt())
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, (1*dp).toInt()
                ).also { it.bottomMargin = (12*dp).toInt() }
            })

            val hScroll = HorizontalScrollView(this).apply {
                isHorizontalScrollBarEnabled = false
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.bottomMargin = (20*dp).toInt() }
            }
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            dateSlots.forEach { slot ->
                val card = LayoutInflater.from(this).inflate(R.layout.item_timeslot_card, row, false) as LinearLayout
                card.findViewById<TextView>(R.id.slotTime).text     = slot.displayTime
                card.findViewById<TextView>(R.id.slotLocation).text = slot.displayLocation
                viewMap[slot] = card
                card.setOnClickListener { onSelect(slot) }
                row.addView(card)
            }
            hScroll.addView(row)
            container.addView(hScroll)
        }
    }

    private fun formatSlotDate(dateStr: String): String {
        return try {
            val date = java.time.LocalDate.parse(dateStr)
            date.format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"))
        } catch (e: Exception) { dateStr }
    }

    private fun setSlotSelected(view: LinearLayout) {
        view.setBackgroundResource(R.drawable.timeslot_card_selected_bg)
        view.findViewById<TextView>(R.id.slotTime).setTextColor(0xFFFFFFFF.toInt())
        view.findViewById<TextView>(R.id.slotLocation).setTextColor(0xCCFFFFFF.toInt())
        view.findViewById<ImageView>(R.id.slotIcon).imageTintList = ColorStateList.valueOf(0xFFFFFFFF.toInt())
    }

    private fun setSlotUnselected(view: LinearLayout) {
        view.setBackgroundResource(R.drawable.timeslot_card_normal_bg)
        view.findViewById<TextView>(R.id.slotTime).setTextColor(0xFF212121.toInt())
        view.findViewById<TextView>(R.id.slotLocation).setTextColor(0xFF9E9E9E.toInt())
        view.findViewById<ImageView>(R.id.slotIcon).imageTintList = ColorStateList.valueOf(0xFF757575.toInt())
    }

    private fun observeNotifBadge() {
        val schoolId = SessionManager.schoolId(this) ?: return
        notifViewModel.fetchUnreadCount(schoolId)
        notifViewModel.unreadCount.observe(this) { count ->
            navNotifBadge.visibility = if (count > 0) View.VISIBLE else View.GONE
        }
    }
}
