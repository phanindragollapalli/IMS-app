package com.example.ims_app.data

import android.content.Context

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    init {
        ensureSeedUsers()
    }

    fun login(username: String, password: String): Boolean {
        if (username.isBlank() || password.isBlank()) return false
        val user = getUsers().firstOrNull {
            it.username.equals(username.trim(), ignoreCase = true) && it.password == password
        } ?: return false

        prefs.edit()
            .putLong(KEY_LOGIN_TIME, System.currentTimeMillis())
            .putString(KEY_SESSION_USERNAME, user.username)
            .apply()
        return true
    }

    fun logout() {
        prefs.edit()
            .remove(KEY_LOGIN_TIME)
            .remove(KEY_SESSION_USERNAME)
            .apply()
    }

    fun isSessionValid(): Boolean {
        val loginTime = prefs.getLong(KEY_LOGIN_TIME, 0L)
        val username = prefs.getString(KEY_SESSION_USERNAME, null)
        if (loginTime <= 0L) return false
        if (username.isNullOrBlank()) return false

        val stillValid = System.currentTimeMillis() - loginTime < SESSION_DURATION_MS
        if (!stillValid) {
            logout()
        }
        return stillValid
    }

    fun currentSessionUser(): SessionUser? {
        if (!isSessionValid()) return null
        val username = prefs.getString(KEY_SESSION_USERNAME, null) ?: return null
        val user = getUsers().firstOrNull { it.username == username } ?: return null
        return SessionUser(
            username = user.username,
            displayName = user.displayName,
            role = user.role,
            batch = user.batch,
            rollNo = user.rollNo,
        )
    }

    fun saveUserLocalizationSettings(username: String, settings: UserLocalizationSettings) {
        if (username.isBlank()) return
        prefs.edit()
            .putString(localizationKey(username, KEY_LOC_LANGUAGE), settings.language.code)
            .putString(localizationKey(username, KEY_LOC_COUNTRY), settings.country)
            .putString(localizationKey(username, KEY_LOC_CURRENCY), settings.currency.code)
            .putString(localizationKey(username, KEY_LOC_TIME_ZONE), settings.timeZone.id)
            .apply()
    }

    fun loadUserLocalizationSettings(username: String): UserLocalizationSettings {
        val defaults = UserLocalizationSettings()
        if (username.isBlank()) return defaults

        val languageCode = prefs.getString(localizationKey(username, KEY_LOC_LANGUAGE), defaults.language.code)
        val country = prefs.getString(localizationKey(username, KEY_LOC_COUNTRY), defaults.country)
            .orEmpty()
            .ifBlank { defaults.country }
        val currencyCode = prefs.getString(localizationKey(username, KEY_LOC_CURRENCY), defaults.currency.code)
        val timeZoneId = prefs.getString(localizationKey(username, KEY_LOC_TIME_ZONE), defaults.timeZone.id)

        val language = AppLanguage.values().firstOrNull { it.code == languageCode } ?: defaults.language
        val currency = AppCurrency.values().firstOrNull { it.code == currencyCode } ?: defaults.currency
        val timeZone = AppTimeZone.values().firstOrNull { it.id == timeZoneId } ?: defaults.timeZone

        return UserLocalizationSettings(
            language = language,
            country = country,
            currency = currency,
            timeZone = timeZone,
        )
    }

    private fun ensureSeedUsers() {
        if (!prefs.getString(KEY_USERS, null).isNullOrBlank()) return
        val seeded = listOf(
            UserAccount("admin", "admin123", UserRole.Admin, "System Admin"),
            UserAccount("faculty1", "faculty123", UserRole.Faculty, "Dr. Rao"),
            UserAccount("student1", "student123", UserRole.Student, "Aarav Singh", batch = "B.Tech CSE - Sem 4", rollNo = "IMS2401")
        )
        prefs.edit().putString(KEY_USERS, serializeUsers(seeded)).apply()
    }

    private fun getUsers(): List<UserAccount> {
        val raw = prefs.getString(KEY_USERS, null).orEmpty()
        if (raw.isBlank()) return emptyList()
        return raw.split(";;").mapNotNull { row ->
            val parts = row.split('|')
            if (parts.size < 4) return@mapNotNull null
            val role = runCatching { UserRole.valueOf(parts[2]) }.getOrNull() ?: return@mapNotNull null
            UserAccount(
                username = parts[0],
                password = parts[1],
                role = role,
                displayName = parts[3],
                batch = parts.getOrNull(4)?.ifBlank { null },
                rollNo = parts.getOrNull(5)?.ifBlank { null },
            )
        }
    }

    private fun serializeUsers(users: List<UserAccount>): String {
        return users.joinToString(";;") { user ->
            listOf(
                user.username,
                user.password,
                user.role.name,
                user.displayName,
                user.batch.orEmpty(),
                user.rollNo.orEmpty(),
            ).joinToString("|")
        }
    }

    private fun localizationKey(username: String, key: String): String {
        return "${KEY_LOCALIZATION_PREFIX}_${username.trim().lowercase()}_$key"
    }

    companion object {
        private const val PREF_NAME = "ims_session"
        private const val KEY_LOGIN_TIME = "login_time"
        private const val KEY_SESSION_USERNAME = "session_username"
        private const val KEY_USERS = "users"
        private const val KEY_LOCALIZATION_PREFIX = "localization"
        private const val KEY_LOC_LANGUAGE = "language"
        private const val KEY_LOC_COUNTRY = "country"
        private const val KEY_LOC_CURRENCY = "currency"
        private const val KEY_LOC_TIME_ZONE = "time_zone"
        private const val SESSION_DURATION_MS = 3L * 24L * 60L * 60L * 1000L
    }
}

