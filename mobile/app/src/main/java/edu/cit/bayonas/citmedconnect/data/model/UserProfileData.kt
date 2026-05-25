package edu.cit.bayonas.citmedconnect.data.model

data class UserProfileData(
    val schoolId: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val role: String? = null,
    val gender: String? = null,
    val age: Int = 0
) {
    val fullName: String get() = listOfNotNull(
        firstName?.takeIf { it.isNotBlank() },
        lastName?.takeIf { it.isNotBlank() }
    ).joinToString(" ")

    val initials: String get() {
        val f = firstName?.firstOrNull()?.uppercaseChar()?.toString() ?: ""
        val l = lastName?.firstOrNull()?.uppercaseChar()?.toString() ?: ""
        return (f + l).ifEmpty { fullName.take(2).uppercase() }
    }

    val displayRole: String get() = when (role?.uppercase()) {
        "ADMIN" -> "Admin"
        "STAFF" -> "Staff"
        else    -> "Student"
    }

    val displayPhone: String get() = phone?.takeIf { it.isNotBlank() } ?: "—"
    val displayGender: String get() = gender?.replaceFirstChar { it.uppercase() }?.takeIf { it.isNotBlank() } ?: "—"
    val displayAge: String get() = if (age > 0) "$age" else "—"
}
