package com.example.main_screen.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

// --- DataStore 配置 ---
private val Application.dataStore by preferencesDataStore("auth_prefs")
private val KEY_ROLE = stringPreferencesKey("role")
private val KEY_EMPLOYEE_ID = stringPreferencesKey("employee_id")

sealed class AuthState {
    data class Authenticated(val role: String, val employeeID: String) : AuthState()
    object Loading : AuthState()
    data class Error(val message: String) : AuthState()
    object Unauthenticated : AuthState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    init {
        restoreAuthState()
    }

    /**
     * ✅ App 启动时尝试恢复登录状态
     */
    private fun restoreAuthState() {
        viewModelScope.launch {
            val prefs = getApplication<Application>().dataStore.data.first()
            val role = prefs[KEY_ROLE]
            val employeeID = prefs[KEY_EMPLOYEE_ID]

            val currentUser = auth.currentUser
            if (currentUser != null && role != null && employeeID != null) {
                _authState.value = AuthState.Authenticated(role, employeeID)
            } else {
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    /**
     * ✅ Firebase + Firestore 登录
     */
    fun loginWithEmployeeID(employeeID: String, password: String) {
        if (employeeID.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Employee ID or Password cannot be empty")
            return
        }

        _authState.value = AuthState.Loading

        firestore.collection("users")
            .whereEqualTo("employeeID", employeeID)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val document = documents.documents[0]
                    val email = document.getString("email")
                    val role = document.getString("role") ?: "employee"
                    val employeeIDFromDoc = document.getString("employeeID") ?: ""

                    if (email.isNullOrEmpty()) {
                        _authState.value = AuthState.Error("No email found for this Employee ID")
                        return@addOnSuccessListener
                    }

                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                _authState.value = AuthState.Authenticated(role, employeeIDFromDoc)

                                // ✅ 保存到 DataStore
                                viewModelScope.launch {
                                    getApplication<Application>().dataStore.edit { prefs ->
                                        prefs[KEY_ROLE] = role
                                        prefs[KEY_EMPLOYEE_ID] = employeeIDFromDoc
                                    }
                                }
                            } else {
                                _authState.value = AuthState.Error(task.exception?.message ?: "Login failed")
                            }
                        }
                } else {
                    _authState.value = AuthState.Error("Employee ID not found")
                }
            }
            .addOnFailureListener { e ->
                _authState.value = AuthState.Error("Error fetching user: ${e.message}")
            }
    }

    /**
     * ✅ 登出：清理 Firebase + DataStore
     */
    fun signout() {
        auth.signOut()
        viewModelScope.launch {
            getApplication<Application>().dataStore.edit { it.clear() }
        }
        _authState.value = AuthState.Unauthenticated
    }
}
