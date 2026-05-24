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
import edu.cit.bayonas.citmedconnect.R

class AppointmentsActivity : AppCompatActivity() {

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

    private val allAppointments = listOf(
        AppointmentItem("gabgab",       "12:41 · May 26, 2026", "Clinic", "Scheduled"),
        AppointmentItem("test",         "10:30 · May 25, 2026", "Clinic", "Completed"),
        AppointmentItem("headache",     "10:00 · May 5, 2026",  "Clinic", "Completed"),
        AppointmentItem("asd",          "13:20 · Apr 8, 2026",  "Clinic", "Cancelled"),
        AppointmentItem("asdasdasdasd", "00:30 · Apr 8, 2026",  "Clinic", "Completed"),
        AppointmentItem("asd",          "00:30 · Apr 8, 2026",  "Clinic", "Cancelled")
    )

    // Sample admin-created time slots. Replace with real API data when available.
    private val availableSlots = listOf(
        TimeSlot("Wednesday, April 8, 2026",  "00:30:00", "Main Clinic"),
        TimeSlot("Wednesday, April 8, 2026",  "13:20:00", "Main Clinic"),
        TimeSlot("Friday, May 8, 2026",       "17:40:00", "Main Clinic"),
        TimeSlot("Monday, May 25, 2026",      "10:30:00", "Main Clinic"),
        TimeSlot("Tuesday, May 26, 2026",     "12:41:00", "Main Clinic")
    )

    private var currentFilter = "All"
    private var currentSearch = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = getColor(R.color.primary_red)
        setContentView(R.layout.activity_appointments)

        bindViews()
        setupFilterChips()
        setupSearch()
        setupNavigation()
        renderAppointments()

        if (intent.getBooleanExtra("OPEN_BOOKING_MODAL", false)) {
            showBookAppointmentModal()
        }
    }

    private fun bindViews() {
        searchInput = findViewById(R.id.searchInput)
        filterAll = findViewById(R.id.filterAll)
        filterScheduled = findViewById(R.id.filterScheduled)
        filterCompleted = findViewById(R.id.filterCompleted)
        filterCancelled = findViewById(R.id.filterCancelled)
        appointmentsListContainer = findViewById(R.id.appointmentsListContainer)
        emptyStateContainer = findViewById(R.id.emptyStateContainer)
        emptySubtitle = findViewById(R.id.emptySubtitle)
        bookAppointmentBtn = findViewById(R.id.bookAppointmentBtn)
        emptyBookBtn = findViewById(R.id.emptyBookBtn)
        navHome = findViewById(R.id.navHome)
        navAppointments = findViewById(R.id.navAppointments)
        navMedicalRecords = findViewById(R.id.navMedicalRecords)
        navNotifications = findViewById(R.id.navNotifications)
        navProfile = findViewById(R.id.navProfile)
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
            Toast.makeText(this, "Profile coming soon", Toast.LENGTH_SHORT).show()
        }
        bookAppointmentBtn.setOnClickListener { showBookAppointmentModal() }
        emptyBookBtn.setOnClickListener { showBookAppointmentModal() }
    }

    private fun renderAppointments() {
        appointmentsListContainer.removeAllViews()

        val filtered = allAppointments.filter { appt ->
            val matchesFilter = currentFilter == "All" ||
                    appt.status.equals(currentFilter, ignoreCase = true)
            val matchesSearch = currentSearch.isEmpty() ||
                    appt.reason.contains(currentSearch, ignoreCase = true) ||
                    appt.location.contains(currentSearch, ignoreCase = true)
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

    private fun addAppointmentCard(item: AppointmentItem) {
        val view = LayoutInflater.from(this).inflate(
            R.layout.item_appointment_full_card, appointmentsListContainer, false
        )

        view.findViewById<TextView>(R.id.appointmentReason).text = item.reason
        view.findViewById<TextView>(R.id.appointmentDateTime).text = item.dateTime
        view.findViewById<TextView>(R.id.appointmentLocation).text = item.location

        val statusBadge = view.findViewById<TextView>(R.id.statusBadge)
        val statusAccent = view.findViewById<View>(R.id.statusAccent)
        val btnEdit = view.findViewById<LinearLayout>(R.id.btnEdit)
        val btnDelete = view.findViewById<LinearLayout>(R.id.btnDelete)

        statusBadge.text = item.status
        when (item.status.lowercase()) {
            "scheduled" -> {
                statusBadge.setBackgroundResource(R.drawable.badge_scheduled_bg)
                statusBadge.setTextColor(0xFF2196F3.toInt())
                statusAccent.setBackgroundColor(0xFF2196F3.toInt())
                btnEdit.visibility = View.VISIBLE
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
            Toast.makeText(this, "Viewing: ${item.reason}", Toast.LENGTH_SHORT).show()
        }
        btnEdit.setOnClickListener {
            Toast.makeText(this, "Edit coming soon", Toast.LENGTH_SHORT).show()
        }
        btnDelete.setOnClickListener {
            Toast.makeText(this, "Cancel appointment coming soon", Toast.LENGTH_SHORT).show()
        }

        appointmentsListContainer.addView(view)
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

        val closeBtn = dialog.findViewById<LinearLayout>(R.id.closeBtn)
        val slotsContainer = dialog.findViewById<LinearLayout>(R.id.slotsContainer)
        val emptySlots = dialog.findViewById<LinearLayout>(R.id.emptySlots)
        val confirmBtn = dialog.findViewById<LinearLayout>(R.id.confirmBtn)
        val confirmBtnText = dialog.findViewById<TextView>(R.id.confirmBtnText)
        val scrollView = dialog.findViewById<ScrollView>(R.id.slotsScrollView)

        // Cap scroll area at 55% of screen height
        scrollView.layoutParams.height =
            (resources.displayMetrics.heightPixels * 0.55).toInt()

        closeBtn.setOnClickListener { dialog.dismiss() }

        var selectedSlot: TimeSlot? = null
        val slotViewMap = mutableMapOf<TimeSlot, LinearLayout>()

        if (availableSlots.isEmpty()) {
            slotsContainer.visibility = View.GONE
            emptySlots.visibility = View.VISIBLE
            confirmBtn.alpha = 0f
        } else {
            populateSlots(slotsContainer, availableSlots, slotViewMap) { slot ->
                selectedSlot = slot
                // Update visual selection states
                slotViewMap.forEach { (_, v) -> setSlotUnselected(v) }
                setSlotSelected(slotViewMap[slot]!!)
                // Enable confirm button
                confirmBtn.animate().alpha(1f).setDuration(160).start()
                confirmBtnText.text = "Book ${slot.time} at ${slot.location}"
            }
        }

        confirmBtn.setOnClickListener {
            if (selectedSlot != null) {
                Toast.makeText(
                    this,
                    "Booking ${selectedSlot!!.time} — full booking form coming soon",
                    Toast.LENGTH_SHORT
                ).show()
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun populateSlots(
        container: LinearLayout,
        slots: List<TimeSlot>,
        viewMap: MutableMap<TimeSlot, LinearLayout>,
        onSelect: (TimeSlot) -> Unit
    ) {
        val density = resources.displayMetrics.density
        val grouped = slots.groupBy { it.date }

        grouped.forEach { (date, dateSlots) ->
            // Date label
            val dateLabelView = TextView(this).apply {
                text = date
                textSize = 13f
                setTextColor(getColor(R.color.primary_red))
                setTypeface(null, android.graphics.Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.bottomMargin = (10 * density).toInt() }
            }
            container.addView(dateLabelView)

            // Thin divider under date label
            val divider = View(this).apply {
                setBackgroundColor(0xFFF0F0F0.toInt())
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, (1 * density).toInt()
                ).also { it.bottomMargin = (12 * density).toInt() }
            }
            container.addView(divider)

            // Horizontal scroll row of slot cards
            val hScroll = HorizontalScrollView(this).apply {
                isHorizontalScrollBarEnabled = false
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.bottomMargin = (20 * density).toInt() }
            }

            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            dateSlots.forEach { slot ->
                val cardView = LayoutInflater.from(this).inflate(
                    R.layout.item_timeslot_card, row, false
                ) as LinearLayout

                cardView.findViewById<TextView>(R.id.slotTime).text = slot.time
                cardView.findViewById<TextView>(R.id.slotLocation).text = slot.location
                viewMap[slot] = cardView

                cardView.setOnClickListener { onSelect(slot) }
                row.addView(cardView)
            }

            hScroll.addView(row)
            container.addView(hScroll)
        }
    }

    private fun setSlotSelected(view: LinearLayout) {
        view.setBackgroundResource(R.drawable.timeslot_card_selected_bg)
        view.findViewById<TextView>(R.id.slotTime).setTextColor(0xFFFFFFFF.toInt())
        view.findViewById<TextView>(R.id.slotLocation).setTextColor(0xCCFFFFFF.toInt())
        view.findViewById<ImageView>(R.id.slotIcon).imageTintList =
            ColorStateList.valueOf(0xFFFFFFFF.toInt())
    }

    private fun setSlotUnselected(view: LinearLayout) {
        view.setBackgroundResource(R.drawable.timeslot_card_normal_bg)
        view.findViewById<TextView>(R.id.slotTime).setTextColor(0xFF212121.toInt())
        view.findViewById<TextView>(R.id.slotLocation).setTextColor(0xFF9E9E9E.toInt())
        view.findViewById<ImageView>(R.id.slotIcon).imageTintList =
            ColorStateList.valueOf(0xFF757575.toInt())
    }

    data class AppointmentItem(
        val reason: String, val dateTime: String,
        val location: String, val status: String
    )

    data class TimeSlot(val date: String, val time: String, val location: String)
}
