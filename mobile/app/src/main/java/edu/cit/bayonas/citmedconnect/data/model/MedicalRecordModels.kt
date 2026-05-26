package edu.cit.bayonas.citmedconnect.data.model

data class MedicalRecordResponse(
    val recordId: Long? = null,
    val userId: String? = null,
    val userName: String? = null,
    val appointmentId: Long? = null,
    val diagnosis: String? = null,
    val symptoms: String? = null,
    val treatment: String? = null,
    val prescription: String? = null,
    val vitalSigns: String? = null,
    val allergies: String? = null,
    val medicalHistory: String? = null,
    val notes: String? = null,
    val recordDate: String? = null,
    val createdBy: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    val displayDate: String get() =
        recordDate?.take(10) ?: createdAt?.take(10) ?: "—"

    val displayUserName: String get() =
        userName?.trim()?.ifEmpty { null } ?: "Unknown Patient"

    val parsedVitals: Map<String, String> get() {
        val raw = vitalSigns ?: return emptyMap()
        return try {
            val obj = org.json.JSONObject(raw)
            buildMap {
                listOf("bloodPressure", "heartRate", "temperature", "weight").forEach { key ->
                    val v = obj.optString(key, "")
                    if (v.isNotEmpty()) put(key, v)
                }
            }
        } catch (_: Exception) { emptyMap() }
    }

    val activePrescriptions: Int get() =
        prescription?.trim()?.split(",")?.count { it.isNotBlank() } ?: 0
}

data class CreateMedicalRecordRequest(
    val userId: String,
    val appointmentId: Long? = null,
    val diagnosis: String,
    val symptoms: String? = null,
    val treatment: String? = null,
    val prescription: String? = null,
    val vitalSigns: String? = null,
    val allergies: String? = null,
    val medicalHistory: String? = null,
    val notes: String? = null
)
