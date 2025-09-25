package com.example.delivery_and_transportation_management.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Holds AddTransportationScreen UI & form state so it survives configuration changes.
 * Business logic (validation + save triggers) stays in the Composable.
 */
class AddTransportationViewModel(private val state: SavedStateHandle) : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val _plateNumber = MutableStateFlow(state.get<String>(K_PLATE) ?: "")
    val plateNumber: StateFlow<String> = _plateNumber

    // Pair<DriverName, EmployeeID>
    private val _driverList = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val driverList: StateFlow<List<Pair<String, String>>> = _driverList

    private val _selectedDriverName = MutableStateFlow(state.get<String>(K_DRIVER_NAME) ?: "")
    val selectedDriverName: StateFlow<String> = _selectedDriverName

    private val _selectedEmployeeID = MutableStateFlow(state.get<String>(K_EMP_ID) ?: "")
    val selectedEmployeeID: StateFlow<String> = _selectedEmployeeID

    private val _expandedDriver = MutableStateFlow(false)
    val expandedDriver: StateFlow<Boolean> = _expandedDriver

    private val _selectedType = MutableStateFlow(state.get<String>(K_TYPE) ?: "Car")
    val selectedType: StateFlow<String> = _selectedType

    private val _expandedType = MutableStateFlow(false)
    val expandedType: StateFlow<Boolean> = _expandedType

    val vehicleTypes = listOf("Car", "Van", "Truck", "Container Truck")

    private var driversLoaded = false

    fun loadDrivers() {
        if (driversLoaded) return
        driversLoaded = true
        viewModelScope.launch {
            firestore.collection("users")
                .whereEqualTo("role", "driver")
                .get()
                .addOnSuccessListener { snapshot ->
                    _driverList.value = snapshot.documents.mapNotNull { doc ->
                        val name = doc.getString("name") ?: return@mapNotNull null
                        val employeeID = doc.getString("employeeID") ?: return@mapNotNull null
                        name to employeeID
                    }
                }
                .addOnFailureListener { _driverList.value = emptyList() }
        }
    }

    fun setPlateNumber(value: String) { _plateNumber.value = value.uppercase(); state[K_PLATE] = _plateNumber.value }
    fun toggleDriverExpanded() { _expandedDriver.value = !_expandedDriver.value }
    fun dismissDriverDropdown() { _expandedDriver.value = false }
    fun selectDriver(name: String, id: String) {
        _selectedDriverName.value = name
        _selectedEmployeeID.value = id
        state[K_DRIVER_NAME] = name
        state[K_EMP_ID] = id
    }

    fun toggleTypeExpanded() { _expandedType.value = !_expandedType.value }
    fun dismissTypeDropdown() { _expandedType.value = false }
    fun selectType(type: String) { _selectedType.value = type; state[K_TYPE] = type }

    fun isValidPlateNumber(plate: String): Boolean = plate.matches(Regex("^[A-Za-z]{1,3}[0-9]{1,4}$"))

    companion object {
        private const val K_PLATE = "add_transport_plate"
        private const val K_DRIVER_NAME = "add_transport_driver_name"
        private const val K_EMP_ID = "add_transport_employee_id"
        private const val K_TYPE = "add_transport_type"
    }
}
