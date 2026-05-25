package edu.cit.bayonas.citmedconnect.data

import android.content.Context
import android.content.SharedPreferences

object SessionManager {
    private const val PREFS = "citmed_session"
    private const val KEY_TOKEN     = "token"
    private const val KEY_USER_ID   = "userId"
    private const val KEY_USER_NAME = "userName"
    private const val KEY_ROLE      = "role"
    private const val KEY_SCHOOL_ID = "schoolId"
    private const val KEY_EMAIL     = "email"

    private fun prefs(ctx: Context): SharedPreferences =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun save(ctx: Context, token: String, userId: String, userName: String, role: String, schoolId: String = "", email: String = "") {
        prefs(ctx).edit()
            .putString(KEY_TOKEN,     token)
            .putString(KEY_USER_ID,   userId)
            .putString(KEY_USER_NAME, userName)
            .putString(KEY_ROLE,      role)
            .putString(KEY_SCHOOL_ID, schoolId)
            .putString(KEY_EMAIL,     email)
            .apply()
    }

    fun token(ctx: Context): String?    = prefs(ctx).getString(KEY_TOKEN,     null)
    fun userId(ctx: Context): String?   = prefs(ctx).getString(KEY_USER_ID,   null)
    fun userName(ctx: Context): String? = prefs(ctx).getString(KEY_USER_NAME, null)
    fun role(ctx: Context): String?     = prefs(ctx).getString(KEY_ROLE,      null)
    fun schoolId(ctx: Context): String? = prefs(ctx).getString(KEY_SCHOOL_ID, null)
    fun email(ctx: Context): String?    = prefs(ctx).getString(KEY_EMAIL,     null)

    fun isAdmin(ctx: Context): Boolean {
        val r = role(ctx)?.lowercase() ?: return false
        return r == "admin" || r == "staff"
    }

    fun isLoggedIn(ctx: Context): Boolean = token(ctx) != null

    fun clear(ctx: Context) = prefs(ctx).edit().clear().apply()
}
