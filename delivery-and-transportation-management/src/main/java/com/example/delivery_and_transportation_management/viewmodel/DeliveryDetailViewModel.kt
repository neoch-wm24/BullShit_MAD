package com.example.delivery_and_transportation_management.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle

/**
 * DeliveryDetailViewModel: placeholder to retain UI-related state (e.g. future expansion flags)
 * across configuration changes. Currently no business logic is moved to keep app behavior identical.
 */
class DeliveryDetailViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    // Example future expandable state (not yet used in UI to avoid logic change)
    // private val _isExpanded = MutableStateFlow(savedStateHandle.get<Boolean>("expanded") ?: true)
    // val isExpanded: StateFlow<Boolean> = _isExpanded
    // fun toggleExpanded() { val v = !_isExpanded.value; _isExpanded.value = v; savedStateHandle["expanded"] = v }
}

