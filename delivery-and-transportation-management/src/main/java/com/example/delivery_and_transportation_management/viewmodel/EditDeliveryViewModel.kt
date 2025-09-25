package com.example.delivery_and_transportation_management.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for EditDeliveryScreen. Holds pure UI + form state so rotation/config changes
 * don't reset user input. Business logic (validation + onSave invocation) remains in the UI layer.
 */
class EditDeliveryViewModel(private val state: SavedStateHandle) : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _plateNumber = MutableStateFlow(state.get<String>(K_PLATE) ?: "")
    val plateNumber: StateFlow<String> = _plateNumber

    private val _driverList = MutableStateFlow<List<String>>(emptyList())
    val driverList: StateFlow<List<String>> = _driverList

    private val _selectedDriver = MutableStateFlow(state.get<String>(K_DRIVER) ?: "")
    val selectedDriver: StateFlow<String> = _selectedDriver

    private val _expandedDriver = MutableStateFlow(false)
    val expandedDriver: StateFlow<Boolean> = _expandedDriver

    private val _vehicleTypes = listOf("Car", "Van", "Truck", "Container Truck")
    val vehicleTypes: List<String> = _vehicleTypes

    private val _selectedType = MutableStateFlow(state.get<String>(K_TYPE) ?: "Car")
    val selectedType: StateFlow<String> = _selectedType

    private val _expandedType = MutableStateFlow(false)
    val expandedType: StateFlow<Boolean> = _expandedType

    private var initialized = false
    private var driversLoaded = false

    fun initializeIfNeeded(initialPlate: String, initialDriver: String, initialType: String) {
        if (initialized) return
        setPlateNumberInternal(initialPlate)
        setSelectedDriverInternal(initialDriver)
        setSelectedTypeInternal(if (initialType.isBlank()) "Car" else initialType)
        initialized = true
    }

    fun toggleDriverExpanded() { _expandedDriver.value = !_expandedDriver.value }
    fun dismissDriverDropdown() { _expandedDriver.value = false }

    fun toggleTypeExpanded() { _expandedType.value = !_expandedType.value }
    fun dismissTypeDropdown() { _expandedType.value = false }

    fun setPlateNumber(value: String) = setPlateNumberInternal(value.uppercase())
    fun setSelectedDriver(value: String) = setSelectedDriverInternal(value)
    fun setSelectedType(value: String) = setSelectedTypeInternal(value)

    fun loadDriversIfNeeded() {
        if (driversLoaded) return
        driversLoaded = true
        viewModelScope.launch {
            firestore.collection("users")
                .whereEqualTo("role", "driver")
                .get()
                .addOnSuccessListener { snapshot ->
                    val list = snapshot.documents.mapNotNull { it.getString("name") }
                    val current = _selectedDriver.value
                    _driverList.value = if (current.isNotBlank() && current !in list) listOf(current) + list else list
                }
                .addOnFailureListener {
                    val current = _selectedDriver.value
                    _driverList.value = if (current.isNotBlank()) listOf(current) else emptyList()
                }
        }
    }

    fun isValidPlateNumber(plate: String): Boolean = plate.matches(Regex("^[A-Za-z]{1,3}[0-9]{1,4}$"))

    private fun setPlateNumberInternal(value: String) {
        _plateNumber.value = value
        state[K_PLATE] = value
    }
    private fun setSelectedDriverInternal(value: String) {
        _selectedDriver.value = value
        state[K_DRIVER] = value
    }
    private fun setSelectedTypeInternal(value: String) {
        _selectedType.value = value
        state[K_TYPE] = value
    }

    companion object {
        private const val K_PLATE = "edit_delivery_plate"
        private const val K_DRIVER = "edit_delivery_driver"
        private const val K_TYPE = "edit_delivery_type"
    }
}
