package edu.cit.bayonas.citmedconnect.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import edu.cit.bayonas.citmedconnect.R
import edu.cit.bayonas.citmedconnect.data.SessionManager
import edu.cit.bayonas.citmedconnect.data.model.AppointmentResponse
import edu.cit.bayonas.citmedconnect.data.model.CreateMedicalRecordRequest
import edu.cit.bayonas.citmedconnect.data.model.MedicalRecordResponse
import edu.cit.bayonas.citmedconnect.ui.viewmodel.AppointmentViewModel
import edu.cit.bayonas.citmedconnect.ui.viewmodel.MedicalRecordViewModel
import edu.cit.bayonas.citmedconnect.ui.viewmodel.NotificationViewModel
import org.json.JSONObject
import android.widget.FrameLayout

class MedicalRecordsActivity : AppCompatActivity() {

    private lateinit var medVm: MedicalRecordViewModel
    private lateinit var apptVm: AppointmentViewModel
    private lateinit var notifViewModel: NotificationViewModel
    private var isAdmin = false

    // Views
    private lateinit var headerSubtitle: TextView
    private lateinit var searchBarContainer: LinearLayout
    private lateinit var searchInput: EditText
    private lateinit var sectionLabel: TextView
    private lateinit var createRecordBtn: LinearLayout
    private lateinit var loadingState: LinearLayout
    private lateinit var emptyState: LinearLayout
    private lateinit var emptySubtitle: TextView
    private lateinit var recordsContainer: LinearLayout
    private lateinit var statTotalRecords: TextView
    private lateinit var statLastBP: TextView
    private lateinit var statLastHR: TextView
    private lateinit var statPrescriptions: TextView
    private lateinit var navHome: LinearLayout
    private lateinit var navAppointments: LinearLayout
    private lateinit var navMedicalRecords: LinearLayout
    private lateinit var navNotifications: LinearLayout
    private lateinit var navProfile: LinearLayout
    private lateinit var navNotifBadge: View

    private var allRecords: List<MedicalRecordResponse> = emptyList()
    private var studentAppointments: List<AppointmentResponse> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = getColor(R.color.primary_red)
        setContentView(R.layout.activity_medical_records)

        isAdmin        = SessionManager.isAdmin(this)
        medVm          = ViewModelProvider(this)[MedicalRecordViewModel::class.java]
        apptVm         = ViewModelProvider(this)[AppointmentViewModel::class.java]
        notifViewModel = ViewModelProvider(this)[NotificationViewModel::class.java]

        bindViews()
        adaptToRole()
        setupNavigation()
        setupSearch()
        observeViewModels()
        observeNotifBadge()
        loadData()
    }

    private fun bindViews() {
        headerSubtitle     = findViewById(R.id.headerSubtitle)
        searchBarContainer = findViewById(R.id.searchBarContainer)
        searchInput        = findViewById(R.id.searchInput)
        sectionLabel       = findViewById(R.id.sectionLabel)
        createRecordBtn    = findViewById(R.id.createRecordBtn)
        loadingState       = findViewById(R.id.loadingState)
        emptyState         = findViewById(R.id.emptyState)
        emptySubtitle      = findViewById(R.id.emptySubtitle)
        recordsContainer   = findViewById(R.id.recordsContainer)
        statTotalRecords   = findViewById(R.id.statTotalRecords)
        statLastBP         = findViewById(R.id.statLastBP)
        statLastHR         = findViewById(R.id.statLastHR)
        statPrescriptions  = findViewById(R.id.statPrescriptions)
        navHome            = findViewById(R.id.navHome)
        navAppointments    = findViewById(R.id.navAppointments)
        navMedicalRecords  = findViewById(R.id.navMedicalRecords)
        navNotifications   = findViewById(R.id.navNotifications)
        navProfile         = findViewById(R.id.navProfile)
        navNotifBadge      = findViewById(R.id.navNotifBadge)
    }

    private fun adaptToRole() {
        if (isAdmin) {
            headerSubtitle.text = "Manage student medical records and health information"
            sectionLabel.text   = "All Medical Records"
            searchBarContainer.visibility = View.VISIBLE
            createRecordBtn.visibility    = View.VISIBLE
            emptySubtitle.text  = "No records yet. Create one after a student completes an appointment."
        } else {
            headerSubtitle.text = "View your complete medical history and records"
            sectionLabel.text   = "Medical History"
            searchBarContainer.visibility = View.GONE
            createRecordBtn.visibility    = View.GONE
            emptySubtitle.text  = "Your medical records will appear here after an appointment is completed."
        }
    }

    private fun setupNavigation() {
        navHome.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }
        navAppointments.setOnClickListener {
            val target = if (isAdmin) AdminAppointmentsActivity::class.java
                         else AppointmentsActivity::class.java
            startActivity(Intent(this, target))
            overridePendingTransition(0, 0)
            finish()
        }
        navMedicalRecords.setOnClickListener { /* already here */ }
        navNotifications.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }
        navProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            overridePendingTransition(0, 0)
            finish()
        }

        createRecordBtn.setOnClickListener { showCreateDialog() }
    }

    private fun setupSearch() {
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterAndRender(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun observeViewModels() {
        medVm.isLoading.observe(this) { loading ->
            loadingState.visibility = if (loading) View.VISIBLE else View.GONE
        }

        medVm.records.observe(this) { records ->
            allRecords = records
            updateStats(records)
            filterAndRender(searchInput.text.toString())
        }

        medVm.error.observe(this) { err ->
            if (!err.isNullOrBlank()) {
                Toast.makeText(this, err, Toast.LENGTH_LONG).show()
                medVm.clearError()
            }
        }

        medVm.actionSuccess.observe(this) { msg ->
            if (!msg.isNullOrBlank()) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                medVm.clearActionSuccess()
            }
        }

        apptVm.appointments.observe(this) { appointments ->
            // Keep all appointments so the spinner always shows every student,
            // regardless of whether their appointment status is completed yet.
            studentAppointments = appointments
        }
    }

    private fun loadData() {
        if (isAdmin) {
            medVm.fetchAllRecords()
            apptVm.fetchAllAppointments()
        } else {
            val schoolId = SessionManager.schoolId(this) ?: return
            medVm.fetchMyRecords(schoolId)
        }
    }

    private fun updateStats(records: List<MedicalRecordResponse>) {
        statTotalRecords.text = records.size.toString()

        val latest = records.firstOrNull()
        val vitals = latest?.parsedVitals ?: emptyMap()
        statLastBP.text = vitals["bloodPressure"]?.let { "$it mmHg" } ?: "—"
        statLastHR.text = vitals["heartRate"]?.let { "$it bpm" } ?: "—"

        val totalRx = records.sumOf { it.activePrescriptions }
        statPrescriptions.text = totalRx.toString()
    }

    private fun filterAndRender(query: String) {
        val filtered = if (query.isBlank()) allRecords
        else allRecords.filter { r ->
            r.diagnosis?.contains(query, ignoreCase = true) == true ||
            r.userName?.contains(query, ignoreCase = true) == true ||
            r.userId?.contains(query, ignoreCase = true) == true
        }

        recordsContainer.removeAllViews()

        if (filtered.isEmpty() && loadingState.visibility != View.VISIBLE) {
            emptyState.visibility = View.VISIBLE
        } else {
            emptyState.visibility = View.GONE
            filtered.forEach { record -> addRecordCard(record) }
        }
    }

    private fun addRecordCard(record: MedicalRecordResponse) {
        val view = LayoutInflater.from(this).inflate(
            R.layout.item_medical_record_card, recordsContainer, false
        )

        // Header fields
        view.findViewById<TextView>(R.id.recordDiagnosis).text =
            record.diagnosis ?: "—"
        view.findViewById<TextView>(R.id.recordDate).text =
            formatDate(record.displayDate)

        val studentNameRow = view.findViewById<LinearLayout>(R.id.studentNameRow)
        val studentNameTv  = view.findViewById<TextView>(R.id.recordStudentName)
        if (isAdmin) {
            studentNameRow.visibility = View.VISIBLE
            studentNameTv.text = "${record.displayUserName} · ${record.userId ?: ""}"
        } else {
            studentNameRow.visibility = View.GONE
        }

        // Detail fields
        val vitals = record.parsedVitals
        view.findViewById<TextView>(R.id.recordSymptoms).text    = record.symptoms?.ifBlank { "—" } ?: "—"
        view.findViewById<TextView>(R.id.recordTreatment).text   = record.treatment?.ifBlank { "—" } ?: "—"
        view.findViewById<TextView>(R.id.recordPrescription).text = record.prescription?.ifBlank { "—" } ?: "—"
        view.findViewById<TextView>(R.id.recordAllergies).text   = record.allergies?.ifBlank { "—" } ?: "—"
        view.findViewById<TextView>(R.id.recordMedHistory).text  = record.medicalHistory?.ifBlank { "—" } ?: "—"
        view.findViewById<TextView>(R.id.recordNotes).text       = record.notes?.ifBlank { "—" } ?: "—"
        view.findViewById<TextView>(R.id.vitalBPValue).text      = vitals["bloodPressure"] ?: "—"
        view.findViewById<TextView>(R.id.vitalHRValue).text      = vitals["heartRate"] ?: "—"
        view.findViewById<TextView>(R.id.vitalTempValue).text    = vitals["temperature"] ?: "—"
        view.findViewById<TextView>(R.id.vitalWeightValue).text  = vitals["weight"] ?: "—"
        view.findViewById<TextView>(R.id.recordIdText).text      = "Record ID: ${record.recordId ?: "—"}"
        view.findViewById<TextView>(R.id.recordCreatedAt).text   = "Created: ${formatDate(record.createdAt?.take(10) ?: "—")}"

        // Delete button (admin only)
        val deleteBtn = view.findViewById<LinearLayout>(R.id.btnDelete)
        if (isAdmin) {
            deleteBtn.visibility = View.VISIBLE
            deleteBtn.setOnClickListener {
                confirmDelete(record)
            }
        } else {
            deleteBtn.visibility = View.GONE
        }

        // Expand / Collapse
        val detailSection = view.findViewById<LinearLayout>(R.id.detailSection)
        val expandArrow   = view.findViewById<ImageView>(R.id.expandArrow)
        val cardHeader    = view.findViewById<LinearLayout>(R.id.cardHeader)

        cardHeader.setOnClickListener {
            toggleExpand(detailSection, expandArrow)
        }

        recordsContainer.addView(view)
    }

    private fun toggleExpand(detailSection: LinearLayout, arrow: ImageView) {
        if (detailSection.visibility == View.GONE) {
            detailSection.visibility = View.VISIBLE
            detailSection.alpha = 0f
            detailSection.animate().alpha(1f).setDuration(220).start()
            arrow.animate().rotation(270f).setDuration(220).start()
        } else {
            detailSection.animate().alpha(0f).setDuration(180).withEndAction {
                detailSection.visibility = View.GONE
            }.start()
            arrow.animate().rotation(90f).setDuration(180).start()
        }
    }

    private fun confirmDelete(record: MedicalRecordResponse) {
        AlertDialog.Builder(this)
            .setTitle("Delete Record")
            .setMessage("Delete the record for \"${record.diagnosis}\"? This cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                record.recordId?.let { medVm.deleteRecord(it) }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ── Create Record Dialog ────────────────────────────────────────────────

    private fun showCreateDialog() {
        val dialogView = LayoutInflater.from(this).inflate(
            R.layout.dialog_create_medical_record, null
        )
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val studentSpinner    = dialogView.findViewById<Spinner>(R.id.studentSpinner)
        val diagnosisInput    = dialogView.findViewById<EditText>(R.id.diagnosisInput)
        val symptomsInput     = dialogView.findViewById<EditText>(R.id.symptomsInput)
        val treatmentInput    = dialogView.findViewById<EditText>(R.id.treatmentInput)
        val prescriptionInput = dialogView.findViewById<EditText>(R.id.prescriptionInput)
        val bpInput           = dialogView.findViewById<EditText>(R.id.bpInput)
        val hrInput           = dialogView.findViewById<EditText>(R.id.hrInput)
        val createBtnText     = dialogView.findViewById<TextView>(R.id.createBtnText)

        dialogView.findViewById<LinearLayout>(R.id.closeBtn).setOnClickListener { dialog.dismiss() }
        dialogView.findViewById<LinearLayout>(R.id.cancelBtn).setOnClickListener { dialog.dismiss() }

        // Build spinner: one entry per unique student (deduped by schoolId, most recent first)
        val studentMap = linkedMapOf<String, AppointmentResponse>()
        studentAppointments
            .sortedByDescending { it.appointmentId ?: 0L }
            .forEach { appt ->
                val id = appt.user?.schoolId ?: return@forEach
                if (!studentMap.containsKey(id)) studentMap[id] = appt
            }

        if (studentMap.isEmpty()) {
            Toast.makeText(this, "No students with appointments found", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
            return
        }

        val spinnerItems = mutableListOf("Select student...")
        val spinnerKeys  = mutableListOf<String?>(null)
        studentMap.forEach { (schoolId, appt) ->
            val name = appt.user?.fullName?.ifBlank { schoolId } ?: schoolId
            spinnerItems.add("$name · $schoolId")
            spinnerKeys.add(schoolId)
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerItems)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        studentSpinner.adapter = adapter

        // Forward taps anywhere on the container (including the arrow icon) to the spinner
        dialogView.findViewById<FrameLayout>(R.id.studentSpinnerContainer)
            .setOnClickListener { studentSpinner.performClick() }

        // Track selected appointment ID internally
        var selectedApptId: Long? = null
        studentSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                val schoolId = spinnerKeys.getOrNull(pos) ?: return
                selectedApptId = studentMap[schoolId]?.appointmentId
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        dialogView.findViewById<LinearLayout>(R.id.createBtn).setOnClickListener {
            val selectedPos  = studentSpinner.selectedItemPosition
            val selectedId   = spinnerKeys.getOrNull(selectedPos)
            val diagnosisStr = diagnosisInput.text.toString().trim()

            if (selectedId == null) {
                Toast.makeText(this, "Please select a student", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (diagnosisStr.isEmpty()) {
                diagnosisInput.error = "Diagnosis is required"
                return@setOnClickListener
            }

            val vitalSigns = buildVitalsJson(
                bpInput.text.toString().trim(),
                hrInput.text.toString().trim()
            )

            createBtnText.text = "Creating..."
            medVm.createRecord(
                CreateMedicalRecordRequest(
                    userId        = selectedId,
                    appointmentId = selectedApptId,
                    diagnosis     = diagnosisStr,
                    symptoms      = symptomsInput.text.toString().trim().ifEmpty { null },
                    treatment     = treatmentInput.text.toString().trim().ifEmpty { null },
                    prescription  = prescriptionInput.text.toString().trim().ifEmpty { null },
                    vitalSigns    = vitalSigns
                )
            )
            dialog.dismiss()
        }

        dialog.show()
        // Height = 85 % of screen so ScrollView has room and footer is always visible
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.93).toInt(),
            (resources.displayMetrics.heightPixels * 0.85).toInt()
        )
    }

    private fun buildVitalsJson(bp: String, hr: String): String? {
        val obj = JSONObject()
        if (bp.isNotEmpty()) obj.put("bloodPressure", bp)
        if (hr.isNotEmpty()) obj.put("heartRate", hr)
        return if (obj.length() > 0) obj.toString() else null
    }

    private fun formatDate(raw: String): String {
        if (raw == "—" || raw.length < 10) return raw
        return try {
            val parts = raw.split("-")
            val months = listOf("","Jan","Feb","Mar","Apr","May","Jun",
                                "Jul","Aug","Sep","Oct","Nov","Dec")
            val m = parts[1].toIntOrNull() ?: return raw
            "${months.getOrElse(m) { parts[1] }} ${parts[2]}, ${parts[0]}"
        } catch (_: Exception) { raw }
    }

    private fun observeNotifBadge() {
        val schoolId = SessionManager.schoolId(this) ?: return
        notifViewModel.fetchUnreadCount(schoolId)
        notifViewModel.unreadCount.observe(this) { count ->
            navNotifBadge.visibility = if (count > 0) View.VISIBLE else View.GONE
        }
    }
}
