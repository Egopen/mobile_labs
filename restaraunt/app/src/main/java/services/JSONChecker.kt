package services

import android.content.Context
import services.DBService
import org.json.JSONObject

class JSONChecker(private val dbService: DBService) {

    suspend fun isUserAuthorized(context: Context): Boolean {
        val json = getUserJson(context)
        val email = json.optString("email", "")
        val password = json.optString("password", "")

        if (email.isEmpty() || password.isEmpty()) return false

        return dbService.checkUser(email, password)
    }

    fun getUserJson(context: Context): JSONObject {
        val prefs = context.getSharedPreferences("user", Context.MODE_PRIVATE)
        val email = prefs.getString("email", "") ?: ""
        val password = prefs.getString("password", "") ?: ""

        return JSONObject().apply {
            put("email", email)
            put("password", password)
        }
    }

    fun saveUserJson(context: Context, email: String, password: String) {
        val prefs = context.getSharedPreferences("user", Context.MODE_PRIVATE)
        prefs.edit().putString("email", email).putString("password", password).apply()
    }

    fun removeUserJson(context: Context) {
        val prefs = context.getSharedPreferences("user", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}
