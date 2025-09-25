package com.example.user_management.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AddUserViewModel(private val state: SavedStateHandle) : ViewModel() {
    private val _name = MutableStateFlow(state.get<String>(KEY_NAME) ?: "")
    val name: StateFlow<String> = _name
    private val _phone = MutableStateFlow(state.get<String>(KEY_PHONE) ?: "")
    val phone: StateFlow<String> = _phone
    private val _email = MutableStateFlow(state.get<String>(KEY_EMAIL) ?: "")
    val email: StateFlow<String> = _email
    private val _address = MutableStateFlow(state.get<String>(KEY_ADDRESS) ?: "")
    val address: StateFlow<String> = _address
    private val _postcode = MutableStateFlow(state.get<String>(KEY_POSTCODE) ?: "")
    val postcode: StateFlow<String> = _postcode
    private val _city = MutableStateFlow(state.get<String>(KEY_CITY) ?: "")
    val city: StateFlow<String> = _city
    private val _stateText = MutableStateFlow(state.get<String>(KEY_STATE) ?: "")
    val stateText: StateFlow<String> = _stateText

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _showSuccess = MutableStateFlow(false)
    val showSuccess: StateFlow<Boolean> = _showSuccess

    fun setName(v: String) { _name.value = v; state[KEY_NAME] = v }
    fun setPhone(v: String) { _phone.value = v; state[KEY_PHONE] = v }
    fun setEmail(v: String) { _email.value = v; state[KEY_EMAIL] = v }
    fun setAddress(v: String) { _address.value = v; state[KEY_ADDRESS] = v }
    fun setPostcode(v: String) { _postcode.value = v; state[KEY_POSTCODE] = v }
    fun setCity(v: String) { _city.value = v; state[KEY_CITY] = v }
    fun setStateText(v: String) { _stateText.value = v; state[KEY_STATE] = v }

    fun setSaving(v: Boolean) { _isSaving.value = v }
    fun setError(msg: String?) { _errorMessage.value = msg }
    fun triggerSuccess() { _showSuccess.value = true }
    fun consumeSuccess() { _showSuccess.value = false }

    companion object {
        private const val KEY_NAME = "add_user_name"
        private const val KEY_PHONE = "add_user_phone"
        private const val KEY_EMAIL = "add_user_email"
        private const val KEY_ADDRESS = "add_user_address"
        private const val KEY_POSTCODE = "add_user_postcode"
        private const val KEY_CITY = "add_user_city"
        private const val KEY_STATE = "add_user_state"
    }
}

