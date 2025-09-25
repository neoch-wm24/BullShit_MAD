package com.example.user_management.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel to preserve EditUserScreen UI + form state across configuration changes.
 * Does not change business logic, only moves state holders out of the Composable.
 */
class EditUserViewModel(private val state: SavedStateHandle) : ViewModel() {

    private val _name = MutableStateFlow(state.get<String>(K_NAME) ?: "")
    val name: StateFlow<String> = _name
    private val _phone = MutableStateFlow(state.get<String>(K_PHONE) ?: "")
    val phone: StateFlow<String> = _phone
    private val _email = MutableStateFlow(state.get<String>(K_EMAIL) ?: "")
    val email: StateFlow<String> = _email
    private val _address = MutableStateFlow(state.get<String>(K_ADDRESS) ?: "")
    val address: StateFlow<String> = _address
    private val _postcode = MutableStateFlow(state.get<String>(K_POSTCODE) ?: "")
    val postcode: StateFlow<String> = _postcode
    private val _city = MutableStateFlow(state.get<String>(K_CITY) ?: "")
    val city: StateFlow<String> = _city
    private val _stateText = MutableStateFlow(state.get<String>(K_STATE) ?: "")
    val stateText: StateFlow<String> = _stateText

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage
    private val _showSuccess = MutableStateFlow(false)
    val showSuccess: StateFlow<Boolean> = _showSuccess

    private val _loadedCustomerId = MutableStateFlow<String?>(null)

    fun ensureLoad(customerId: String, loader: suspend (String) -> CustomerData?) {
        if (_loadedCustomerId.value == customerId && !_isLoading.value) return
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val data = loader(customerId)
                if (data != null) {
                    setNameInternal(data.name)
                    setPhoneInternal(data.phone)
                    setEmailInternal(data.email)
                    setAddressInternal(data.address)
                    setPostcodeInternal(data.postcode)
                    setCityInternal(data.city)
                    setStateTextInternal(data.state)
                    _loadedCustomerId.value = customerId
                } else {
                    _errorMessage.value = "Customer does not exist"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Loading failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Public setters (persist)
    fun setName(v: String) = setNameInternal(v)
    fun setPhone(v: String) = setPhoneInternal(v)
    fun setEmail(v: String) = setEmailInternal(v)
    fun setAddress(v: String) = setAddressInternal(v)
    fun setPostcode(v: String) = setPostcodeInternal(v)
    fun setCity(v: String) = setCityInternal(v)
    fun setStateText(v: String) = setStateTextInternal(v)

    fun setSaving(v: Boolean) { _isSaving.value = v }
    fun setError(msg: String?) { _errorMessage.value = msg }
    fun triggerSuccess() { _showSuccess.value = true }
    fun consumeSuccess() { _showSuccess.value = false }

    private fun setNameInternal(v: String) { _name.value = v; state[K_NAME] = v }
    private fun setPhoneInternal(v: String) { _phone.value = v; state[K_PHONE] = v }
    private fun setEmailInternal(v: String) { _email.value = v; state[K_EMAIL] = v }
    private fun setAddressInternal(v: String) { _address.value = v; state[K_ADDRESS] = v }
    private fun setPostcodeInternal(v: String) { _postcode.value = v; state[K_POSTCODE] = v }
    private fun setCityInternal(v: String) { _city.value = v; state[K_CITY] = v }
    private fun setStateTextInternal(v: String) { _stateText.value = v; state[K_STATE] = v }

    data class CustomerData(
        val name: String,
        val phone: String,
        val email: String,
        val address: String,
        val postcode: String,
        val city: String,
        val state: String
    )

    companion object {
        private const val K_NAME = "edit_user_name"
        private const val K_PHONE = "edit_user_phone"
        private const val K_EMAIL = "edit_user_email"
        private const val K_ADDRESS = "edit_user_address"
        private const val K_POSTCODE = "edit_user_postcode"
        private const val K_CITY = "edit_user_city"
        private const val K_STATE = "edit_user_state"
    }
}

