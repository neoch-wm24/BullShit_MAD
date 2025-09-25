package com.example.main_screen.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * ViewModel for ReportPage to preserve UI state across configuration changes
 * without altering existing business logic.
 */
class ReportViewModel(private val state: SavedStateHandle) : ViewModel() {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _selectedTab = MutableStateFlow(state.get<String>(KEY_TAB) ?: "Order")
    val selectedTab: StateFlow<String> = _selectedTab

    private val _selectedPeriod = MutableStateFlow(state.get<String>(KEY_PERIOD) ?: "Year")
    val selectedPeriod: StateFlow<String> = _selectedPeriod

    private val _chartEntries = MutableStateFlow<List<Pair<Float, Float>>>(emptyList())
    val chartEntries: StateFlow<List<Pair<Float, Float>>> = _chartEntries

    private val _xLabels = MutableStateFlow<List<String>>(emptyList())
    val xLabels: StateFlow<List<String>> = _xLabels

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _noData = MutableStateFlow(false)
    val noData: StateFlow<Boolean> = _noData

    private var loadedOnce = false

    init {
        refresh() // initial load
    }

    fun setSelectedTab(tab: String) {
        if (tab == _selectedTab.value) return
        _selectedTab.value = tab
        state[KEY_TAB] = tab
        refresh()
    }

    fun setSelectedPeriod(period: String) {
        if (period == _selectedPeriod.value) return
        _selectedPeriod.value = period
        state[KEY_PERIOD] = period
        refresh()
    }

    fun ensureLoaded() {
        if (!loadedOnce) refresh()
    }

    private fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            _noData.value = false
            try {
                val entries: List<Pair<Float, Float>>
                val labels: List<String>
                if (_selectedTab.value == "Order") {
                    if (_selectedPeriod.value == "Year") {
                        val now = Calendar.getInstance()
                        val currentYear = now.get(Calendar.YEAR)
                        val years = (currentYear - 4..currentYear).toList()
                        val temp = mutableListOf<Pair<Float, Float>>()
                        val lab = mutableListOf<String>()
                        years.forEachIndexed { idx, y ->
                            val count = getOrderCountForYear(db, y)
                            temp.add(idx.toFloat() to count.toFloat())
                            lab.add(y.toString())
                        }
                        entries = temp
                        labels = lab
                    } else {
                        val months = (1..12).toList()
                        entries = months.mapIndexed { idx, m ->
                            idx.toFloat() to getOrderCountForMonth(db, m).toFloat()
                        }
                        labels = months.map { monthNumberToLabel(it) }
                    }
                } else { // Sales
                    if (_selectedPeriod.value == "Year") {
                        val now = Calendar.getInstance()
                        val currentYear = now.get(Calendar.YEAR)
                        val years = (currentYear - 4..currentYear).toList()
                        val temp = mutableListOf<Pair<Float, Float>>()
                        val lab = mutableListOf<String>()
                        years.forEachIndexed { idx, y ->
                            val total = getSalesTotalForYear(db, y)
                            temp.add(idx.toFloat() to total.toFloat())
                            lab.add(y.toString())
                        }
                        entries = temp
                        labels = lab
                    } else {
                        val months = (1..12).toList()
                        entries = months.mapIndexed { idx, m ->
                            idx.toFloat() to getSalesTotalForMonth(db, m).toFloat()
                        }
                        labels = months.map { monthNumberToLabel(it) }
                    }
                }
                _chartEntries.value = entries
                _xLabels.value = labels
                _noData.value = entries.all { it.second == 0f }
                loadedOnce = true
            } catch (e: Exception) {
                // Keep previous data; mark as noData only if we have nothing
                if (_chartEntries.value.isEmpty()) _noData.value = true
            } finally {
                _isLoading.value = false
            }
        }
    }

    companion object {
        private const val KEY_TAB = "report_selected_tab"
        private const val KEY_PERIOD = "report_selected_period"
    }
}

// We import top-level helper suspend functions from ReportPage.kt via same package.
// Ensure those functions remain top-level in the ui file or move them to a shared util if needed.
