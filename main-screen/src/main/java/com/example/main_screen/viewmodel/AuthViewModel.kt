package com.example.main_screen.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class AuthViewModel : ViewModel(){
    private val auth : FirebaseAuth = FirebaseAuth.getInstance()

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    init {
        checkAuthStatus()
    }

    fun checkAuthStatus(){
        if(auth.currentUser==null){
            _authState.value = AuthState.Unauthenticated
        }else{
            _authState.value = AuthState.Authenticated
        }
    }

    fun login(employeeID: String, password: String) {

        if(employeeID.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Employee ID or Password Cannot be Empty")
            return
        }

        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(employeeID,password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful){
                    _authState.value = AuthState.Authenticated
                }else
                    _authState.value = AuthState.Error(task.exception?.message?:"Something went wrong")
            }
    }

    fun signout() {
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }
}

sealed class AuthState {
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Error(val message: String) : AuthState()
}
