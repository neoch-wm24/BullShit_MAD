package com.example.delivery_and_transportation_management.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Holds UI state for DeliveryScheduleScreen so selections persist across rotation.
 * Does not change business logic.
 */
class DeliveryScheduleViewModel(private val state: SavedStateHandle) : ViewModel() {
    private val _selectedDate = MutableStateFlow(state.get<String>(KEY_DATE) ?: "")
    val selectedDate: StateFlow<String> = _selectedDate

    private val _selectedTransportations = MutableStateFlow(state.get<Set<String>>(KEY_SELECTED) ?: emptySet())
    val selectedTransportations: StateFlow<Set<String>> = _selectedTransportations

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
        state[KEY_DATE] = date
    }

    fun toggleTransportation(id: String) {
        val current = _selectedTransportations.value
        val updated = if (id in current) current - id else current + id
        _selectedTransportations.value = updated
        state[KEY_SELECTED] = updated
    }

    fun setTransportationChecked(id: String, checked: Boolean) {
        val current = _selectedTransportations.value.toMutableSet()
        if (checked) current.add(id) else current.remove(id)
        _selectedTransportations.value = current
        state[KEY_SELECTED] = current
    }

    fun clearSelection() {
        _selectedTransportations.value = emptySet()
        state[KEY_SELECTED] = emptySet<String>()
    }

    companion object {
        private const val KEY_DATE = "delivery_schedule_date"
        private const val KEY_SELECTED = "delivery_schedule_selected_ids"
    }
}

