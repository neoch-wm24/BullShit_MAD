package com.example.user_management.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * UI state holder for SearchUserScreen. Keeps search/filter/multi-select state across
 * configuration changes using SavedStateHandle.
 */
class SearchUserViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _searchQuery = MutableStateFlow(savedStateHandle.get<String>(KEY_QUERY) ?: "")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedFilter = MutableStateFlow(savedStateHandle.get<String>(KEY_FILTER) ?: DEFAULT_FILTER)
    val selectedFilter: StateFlow<String> = _selectedFilter

    private val _isMultiSelectMode = MutableStateFlow(savedStateHandle.get<Boolean>(KEY_MULTI) ?: false)
    val isMultiSelectMode: StateFlow<Boolean> = _isMultiSelectMode

    private val _selectedIds = MutableStateFlow(savedStateHandle.get<Set<String>>(KEY_SELECTED_IDS) ?: emptySet())
    val selectedIds: StateFlow<Set<String>> = _selectedIds

    fun setSearchQuery(value: String) {
        _searchQuery.value = value
        savedStateHandle[KEY_QUERY] = value
    }

    fun setSelectedFilter(value: String) {
        _selectedFilter.value = value
        savedStateHandle[KEY_FILTER] = value
    }

    fun enterMultiSelect() {
        if (!_isMultiSelectMode.value) {
            _isMultiSelectMode.value = true
            savedStateHandle[KEY_MULTI] = true
            clearSelection()
        }
    }

    fun exitMultiSelect() {
        if (_isMultiSelectMode.value) {
            _isMultiSelectMode.value = false
            savedStateHandle[KEY_MULTI] = false
            clearSelection()
        }
    }

    fun toggleItem(id: String) {
        val newSet = _selectedIds.value.let { current ->
            if (id in current) current - id else current + id
        }
        _selectedIds.value = newSet
        savedStateHandle[KEY_SELECTED_IDS] = newSet
    }

    fun setItemChecked(id: String, checked: Boolean) {
        val newSet = if (checked) _selectedIds.value + id else _selectedIds.value - id
        _selectedIds.value = newSet
        savedStateHandle[KEY_SELECTED_IDS] = newSet
    }

    fun clearSelection() {
        _selectedIds.value = emptySet()
        savedStateHandle[KEY_SELECTED_IDS] = emptySet<String>()
    }

    companion object {
        private const val KEY_QUERY = "search_query"
        private const val KEY_FILTER = "search_filter"
        private const val KEY_MULTI = "search_multi_mode"
        private const val KEY_SELECTED_IDS = "search_selected_ids"
        private const val DEFAULT_FILTER = "name (A~Z)"
    }
}

