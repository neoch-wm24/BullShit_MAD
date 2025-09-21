package com.example.main_screen.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    init {
        checkAuthStatus()
    }

    fun checkAuthStatus() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _authState.value = AuthState.Unauthenticated
        } else {
            firestore.collection("users")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    val role = document.getString("role") ?: "employee"
                    val employeeID = document.getString("employeeID") ?: ""
                    _authState.value = AuthState.Authenticated(role, employeeID)
                }
                .addOnFailureListener {
                    _authState.value = AuthState.Authenticated("employee", "") // 默认
                }
        }
    }

    fun loginWithEmployeeID(employeeID: String, password: String) {
        if (employeeID.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Employee ID or Password cannot be empty")
            return
        }

        _authState.value = AuthState.Loading

        firestore.collection("users") // 假设集合叫 users
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

    fun signout() {
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }
}

sealed class AuthState {
    data class Authenticated(val role: String, val employeeID: String) : AuthState()
    object Loading : AuthState()
    data class Error(val message: String) : AuthState()
    object Unauthenticated : AuthState()
}