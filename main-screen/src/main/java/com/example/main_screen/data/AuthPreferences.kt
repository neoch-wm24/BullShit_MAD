package com.example.main_screen.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("auth_prefs")

class AuthPreferences(private val context: Context) {
    companion object {
        private val KEY_ROLE = stringPreferencesKey("role")
        private val KEY_EMPLOYEE_ID = stringPreferencesKey("employee_id")
    }

    val authData: Flow<Pair<String?, String?>> = context.dataStore.data.map { prefs ->
        val role = prefs[KEY_ROLE]
        val employeeID = prefs[KEY_EMPLOYEE_ID]
        role to employeeID
    }

    suspend fun saveAuth(role: String, employeeID: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ROLE] = role
            prefs[KEY_EMPLOYEE_ID] = employeeID
        }
    }

    suspend fun clearAuth() {
        context.dataStore.edit { it.clear() }
    }
}
